/* Copyright 2012 Red Star Development / Dave Schoorl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.exceptions.Xb4jMutabilityException;
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.bindings.action.ActionManager;
import info.rsdev.xb4j.model.bindings.action.IPhasedAction;
import info.rsdev.xb4j.model.bindings.action.IPhasedAction.ExecutionPhase;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.FieldAccessor;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.java.constructor.IJavaArgument;
import info.rsdev.xb4j.model.java.constructor.NullCreator;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader.ParseEventData;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Schoorl
 */
public abstract class AbstractBinding implements IBinding {

    static final QName NIL_ATTRIBUTE = new QName("http://www.w3.org/2001/XMLSchema-instance", "nil", "xsi");

    private final Logger logger = LoggerFactory.getLogger(AbstractBinding.class);

    private ActionManager actionManager = null;

    private IElementFetchStrategy elementFetcher = null;

    /**
     * An implementation of {@link ICreator} that knows which Java class instance must be created for this binding. When this
     * binding does not create a new Java instance, the implementation should be a {@link NullCreator}
     */
    private ICreator objectCreator = null;

    private IGetter getter = null;

    private ISetter setter = null;

    private IBinding parent = null;

    private List<IAttribute> attributes = null;

    private final boolean isOptional;
    
    private final Enum<? extends BindOption>[] options;
    
    protected AbstractBinding(IElementFetchStrategy elementFetcher, ICreator objectCreator, boolean isOptional, Enum<? extends BindOption>... options) {
        setElementFetchStrategy(elementFetcher);
        setObjectCreator(objectCreator);
        this.getter = NoGetter.INSTANCE;
        this.setter = NoSetter.INSTANCE;
        this.actionManager = new ActionManager();
        this.isOptional = isOptional;
        this.options = Arrays.copyOf(options, options.length);
    }

    /**
     * Copy constructor that copies the properties of the original binding in a
     *
     * @param original
     * @param isOptional
     */
    protected AbstractBinding(AbstractBinding original, boolean isOptional) {
        this.isOptional = isOptional;
        this.options = Arrays.copyOf(original.options, original.options.length);
        copyFields(original, original.elementFetcher);
    }

    /**
     * Copy constructor that copies the properties of the original binding in a
     *
     * @param original
     * @param elementFetcher
     */
    protected AbstractBinding(AbstractBinding original, IElementFetchStrategy elementFetcher) {
        this.isOptional = original.isOptional;
        this.options = Arrays.copyOf(original.options, original.options.length);
        copyFields(original, elementFetcher);
    }

    private void copyFields(AbstractBinding original, IElementFetchStrategy elementFetcher) {
        this.actionManager = original.actionManager;
        this.elementFetcher = elementFetcher;
        this.objectCreator = original.objectCreator;
        this.getter = original.getter;
        this.setter = original.setter;
        if (original.attributes != null) {
            this.attributes = new LinkedList<>();
            original.attributes.forEach((originalAttribute) -> {
                this.attributes.add(originalAttribute.copy(this));
            });
        }
        this.parent = null; // clear parent, so that copy can be used in another binding hierarchy
    }

    @Override
    public IBinding addAttribute(IAttribute attribute, String fieldName) {
        if (fieldName == null) {
            throw new NullPointerException("Fieldname cannot be null");
        }
        getSemaphore().lock();
        try {
            validateMutability();
            FieldAccessor fieldAccessor = new FieldAccessor(fieldName);
            return addAttribute(attribute, fieldAccessor, fieldAccessor);
        } finally {
            getSemaphore().unlock();
        }
    }

    @Override
    public IBinding addAttribute(IAttribute attribute, IGetter getter, ISetter setter) {
        if (attribute == null) {
            throw new NullPointerException(String.format("Attribute cannot be null (binding=%s)", this));
        }
        if (getElement() == null) {
            throw new Xb4jException(String.format("No element defined to bind attributes to (binding=%s)", this));
        }
        getSemaphore().lock();
        try {
            validateMutability();
            if ((this.attributes == null) && (getElement() != null)) {
                // only create new collection, when there is an element to bind them to
                this.attributes = new LinkedList<>();
            }
            if (attributes.contains(attribute)) {
                throw new Xb4jException(String.format("Attribute %s already defined (binding=%s)", attribute, this));
            }
            attribute.setGetter(getter);
            attribute.setSetter(setter);
            attributes.add(attribute);
            if (attribute instanceof AbstractAttribute) {
                ((AbstractAttribute) attribute).attachToBinding(this);
            }
        } finally {
            getSemaphore().unlock();
        }
        return this;
    }

    public Collection<IAttribute> getAttributes() {
        if (this.attributes == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(this.attributes);
    }

    boolean hasAttributes() {
        return (this.attributes != null) && !this.attributes.isEmpty();
    }

    @Override
    public QName getElement() {
        if (elementFetcher != null) {
            return elementFetcher.getElement();
        }
        return null;
    }

    @Override
    public Class<?> getJavaType() {
        return objectCreator.getJavaType();
    }

    @Override
    public JavaContext newInstance(RecordAndPlaybackXMLStreamReader staxReader, JavaContext currentContext) {
        JavaContext newContext = currentContext.newContext(objectCreator.newInstance(this, staxReader));
        newContext = this.actionManager.executeActions(ExecutionPhase.AFTER_OBJECT_CREATION, newContext);
        return newContext;
    }

    /**
     * Select a non-null context (if possible), where the newJavaContext takes precedence over the javaContext, when both of them
     * are not null.
     *
     * @param javaContext
     * @param newJavaContext
     * @return either the javaContext or the newJavaContext
     */
    protected JavaContext select(JavaContext javaContext, JavaContext newJavaContext) {
        if (newJavaContext.getContextObject() != null) {
            return newJavaContext;
        }
        return javaContext;
    }

    private void setElementFetchStrategy(IElementFetchStrategy elementFetcher) {
        if (elementFetcher == null) {
            throw new NullPointerException("IElementFetchStrategy cannot be null");
        }
        if ((this.elementFetcher != null) && !this.elementFetcher.equals(elementFetcher)) {
            throw new Xb4jException("Once set, an IElementFetchStrategy cannot be changed: ".concat(this.toString()));
        }
        // called via the constructor only: no need to validate mutability
        this.elementFetcher = elementFetcher;
    }

    protected IElementFetchStrategy getElementFetchStrategy() {
        return this.elementFetcher;
    }

    private void setObjectCreator(ICreator objectCreator) {
        if (objectCreator == null) {
            throw new NullPointerException("ICreator cannot be null. Use NullCreator instance when neccesary.");
        }
        if ((this.objectCreator != null) && !this.objectCreator.equals(objectCreator)) {
            throw new Xb4jException("Once set, an ICreator cannot be changed: ".concat(this.toString()));
        }
        // called via the constructor only: no need to validate mutability
        this.objectCreator = objectCreator;
    }

    @Override
    public IBinding setGetter(IGetter getter) {
        if (getter == null) {
            throw new NullPointerException("IGetter cannot be null. Use NoGetter instance when neccesary.");
        }
        getSemaphore().lock();
        try {
            validateMutability();
            this.getter = getter;
        } finally {
            getSemaphore().unlock();
        }
        return this;
    }

    @Override
    public IBinding setSetter(ISetter setter) {
        if (setter == null) {
            throw new NullPointerException("ISetter cannot be null. Use NoSetter instance when neccesary.");
        }
        getSemaphore().lock();
        try {
            validateMutability();
            this.setter = setter;
        } finally {
            getSemaphore().unlock();
        }
        return this;
    }

    @Override
    public IBinding addAction(IPhasedAction action) {
        if (action == null) {
            throw new NullPointerException("You must provide an IPhasedAction implementation");
        }

        getSemaphore().lock();
        try {
            validateMutability();
            this.actionManager.addAction(action);
        } finally {
            getSemaphore().unlock();
        }
        return this;
    }

    public boolean hasSetter() {
        return (this.setter != null) && !(this.setter instanceof NoSetter);
    }

    @Override
    public void setParent(IBinding parent) {
        if (parent == null) {
            throw new NullPointerException("Parent IBinding cannot be null");
        }
        if ((this.parent != null) && !this.parent.equals(parent)) {
            throw new IllegalArgumentException(String.format("This binding '%s' is already part of a binding tree.", this));
        }

        ISemaphore topLevelElement = getSemaphore();
        topLevelElement.lock();
        try {
            validateMutability();
            this.parent = parent;
        } finally {
            topLevelElement.unlock();
        }
    }

    @Override
    public IBinding getParent() {
        return this.parent;
    }

    @Override
    public ISemaphore getSemaphore() {
        IBinding semaphoreBinding = this;
        while (semaphoreBinding.getParent() != null) {
            semaphoreBinding = semaphoreBinding.getParent();
        }

        if (!(semaphoreBinding instanceof ISemaphore)) {
            return NullSafeSemaphore.INSTANCE; // provide nullsafe lock/unlock utility for cases where the binding is not yet part
            // of a full tree
        }
        return (ISemaphore) semaphoreBinding;
    }

    @Override
    public IModelAware getModelAware() {
        IBinding modelAwareBinding = this;
        while (modelAwareBinding.getParent() != null) {
            modelAwareBinding = modelAwareBinding.getParent();
        }

        if (!(modelAwareBinding instanceof IModelAware)) {
            return NullSafeModelAware.INSTANCE; // provide nullsafe utility for cases where the binding is not yet part of a full
            // tree
        }
        return (IModelAware) modelAwareBinding;
    }

    @Override
    public boolean setProperty(JavaContext javaContext, Object propertyValue) {
        if (logger.isTraceEnabled()) {
            logger.trace("Call  " + this.setter.getClass().getSimpleName() + " from " + getPath());
        }
        return this.setter.set(javaContext, propertyValue);
    }

    @Override
    public JavaContext getProperty(JavaContext javaContext) {
        if (javaContext.getContextObject() == null) {
            return javaContext;
        }
        return this.getter.get(javaContext);
    }

    public boolean isExpected(QName element) {
        if (element == null) {
            throw new NullPointerException("QName cannot be null");
        }
        return element.equals(getElement());
    }

    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    /**
     * Write an empty element to the xml stream with the xsi:nil attribute set to true.
     * 
     * @param staxWriter the xml stream
     * @param javaContext the Java context with context object appropriate for this binding
     * @throws XMLStreamException 
     */
    void nilToXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        staxWriter.writeElement(getElement(), true);
        attributesToXml(staxWriter, javaContext);
        staxWriter.writeAttribute(getElement(), NIL_ATTRIBUTE, "true");
        staxWriter.closeElement(getElement(), true);        
    }

    void attributesToXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        if ((attributes != null) && !attributes.isEmpty()) {
            for (IAttribute attribute : this.attributes) {
                attribute.toXml(staxWriter, javaContext, getElement());
            }
        }
    }
    
    void attributesToJava(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        Collection<IAttribute> expectedAttributes = getAttributes();
        if ((expectedAttributes != null) && !expectedAttributes.isEmpty()) {
            Map<QName, String> actualAttributes = staxReader.getAttributes();
            if (actualAttributes != null) {
                actualAttributes = new HashMap<>(actualAttributes);
                for (IAttribute attribute : expectedAttributes) {
                    if (!actualAttributes.containsKey(attribute.getAttributeName()) && attribute.isRequired()) {
                        throw new Xb4jException(String.format("%s is required but not found in xml for %s", attribute, this));
                    }
                    String value = actualAttributes.get(attribute.getAttributeName());
                    attribute.toJava(value, javaContext);
                }
            }
        }
    }

    @Override
    public UnmarshallResult toJava(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        if (logger.isTraceEnabled()) {
            logger.trace("{Unmarshalling} " + getPath());
        }
        javaContext = this.actionManager.executeActions(ExecutionPhase.BEFORE_UNMARSHALLING, javaContext);

        UnmarshallResult result = unmarshall(staxReader, javaContext);

        if (this.actionManager.hasActionsForPhase(ExecutionPhase.AFTER_UNMARSHALLING)) {
            JavaContext actionContext = javaContext.getContextObject() == null
                    ? javaContext.newContext(result.getUnmarshalledObject()) : javaContext;
            this.actionManager.executeActions(ExecutionPhase.AFTER_UNMARSHALLING, actionContext);
        }
        return result;
    }

    /**
     * The implementation of the Xml to Java routine
     *
     * @param staxReader
     * @param javaContext
     * @return
     * @throws XMLStreamException
     */
    public abstract UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext)
            throws XMLStreamException;

    @Override
    public void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        if (logger.isTraceEnabled()) {
            logger.trace("{Marshalling} " + getPath());
        }
        javaContext = this.actionManager.executeActions(ExecutionPhase.BEFORE_MARSHALLING, javaContext);
        validateContextObject(javaContext);
        marshall(staxWriter, javaContext);
        this.actionManager.executeActions(ExecutionPhase.AFTER_MARSHALLING, javaContext);
    }

    /**
     * The implementation of the Java to Xml routine
     *
     * @param staxWriter
     * @param javaContext
     * @throws XMLStreamException
     */
    public abstract void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("[");
        String separator = "";
        QName element = getElement();
        if (element != null) {
            sb.append(separator).append("element=");
            sb.append(element.toString());
            separator = ",";
        }
        Class<?> collectionType = getJavaType();
        if (collectionType != null) {
            sb.append(separator).append("javaType=").append(collectionType.getName());
            separator = ",";
        }

        sb.append(separator).append("path=").append(getPath()).append("]");
        return sb.toString();
    }

    @Override
    public String getPath() {
        List<String> pathToRoot = new ArrayList<>();
        IBinding binding = this;
        while (binding != null) {
            String bindingType = binding.getClass().getSimpleName();
            if (binding.getElement() != null) {
                bindingType = bindingType.concat("<").concat(binding.getElement().getLocalPart()).concat(">");
            }
            pathToRoot.add(bindingType);
            binding = binding.getParent();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = pathToRoot.size() - 1; i >= 0; i--) {
            sb.append("/").append(pathToRoot.get(i));
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.elementFetcher == null) ? 0 : this.elementFetcher.hashCode());
        result = prime * result + ((this.getter == null) ? 0 : this.getter.hashCode());
        result = prime * result + (this.isOptional ? 1231 : 1237);
        result = prime * result + ((this.objectCreator == null) ? 0 : this.objectCreator.hashCode());
        result = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
        result = prime * result + ((this.setter == null) ? 0 : this.setter.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractBinding other = (AbstractBinding) obj;
        if (this.elementFetcher == null) {
            if (other.elementFetcher != null) {
                return false;
            }
        } else if (!this.elementFetcher.equals(other.elementFetcher)) {
            return false;
        }
        if (this.getter == null) {
            if (other.getter != null) {
                return false;
            }
        } else if (!this.getter.equals(other.getter)) {
            return false;
        }
        if (this.isOptional != other.isOptional) {
            return false;
        }
        if (this.objectCreator == null) {
            if (other.objectCreator != null) {
                return false;
            }
        } else if (!this.objectCreator.equals(other.objectCreator)) {
            return false;
        }
        if (this.parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!this.parent.equals(other.parent)) {
            return false;
        }
        if (this.setter == null) {
            if (other.setter != null) {
                return false;
            }
        } else if (!this.setter.equals(other.setter)) {
            return false;
        }
        return true;
    }

    @Override
    public void validateMutability() {
        ISemaphore semaphore = getSemaphore();
        semaphore.lock();
        try {
            IModelAware topLevel = getModelAware();
            if (topLevel.isImmutable()) {
                throw new Xb4jMutabilityException(String.format("Cannot change (parts of the) immutable binding %s", semaphore));
            }
        } finally {
            semaphore.unlock();
        }
    }

    @Override
    public IJavaArgument findArgumentBindingOrAttribute(QName argumentQName) {
        // this implementation looks only at itself (including it's attributes)
        if (argumentQName.equals(getElement()) && (this instanceof IJavaArgument)) {
            return (IJavaArgument) this;
        }

        for (IAttribute attribute : getAttributes()) {
            if (argumentQName.equals(attribute.getAttributeName()) && (attribute instanceof IJavaArgument)) {
                return (IJavaArgument) attribute;
            }
        }

        return null;
    }

    @Override
    public OutputState attributesGenerateOutput(JavaContext javaContext) {
        if (hasAttributes()) {
            for (IAttribute attribute : getAttributes()) {
                OutputState attributeOutputState = attribute.generatesOutput(javaContext);
                if ((attributeOutputState == OutputState.HAS_OUTPUT) ||
                        ((attributeOutputState == OutputState.COLLABORATE) && !isOptional())) {
                    return OutputState.HAS_OUTPUT;
                }
            }
        }
        return OutputState.NO_OUTPUT;
    }

    @Override
    public boolean hasOption(Enum<?> option) {
        if (this.options != null) {
            for (Enum<?> candidate: this.options) {
                if (candidate == option) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean containsNil(Map<QName, String> attributes) {
        if (attributes == null) {
            return false;
        }
        return Boolean.parseBoolean(attributes.get(NIL_ATTRIBUTE));
    }

    /**
     * Check if the element is a valid nil-element,meaning, it has no content, and return {@linkplain UnmarshallResult#NO_RESULT}
     *
     * @param staxReader the xml stream positioned directly after this element's open tag.
     * @return
     * @throws XMLStreamException propagate exceptions that may occur while reading the xml stream
     * @throws Xb4jUnmarshallException when the nil-element contains content
     */
    UnmarshallResult handleNil(RecordAndPlaybackXMLStreamReader staxReader) throws XMLStreamException {
        QName expectedElement = getElement();

        // The end-element is included in the list of skippedEvents
        List<ParseEventData> skippedEvents = staxReader.skipToElementEnd();
        if (!skippedEvents.isEmpty()) {
            throw new Xb4jUnmarshallException(String.format("Nil element <%s> cannot contain content", expectedElement), this);
        }
        return UnmarshallResult.NO_RESULT;
    }

    /**
     * Check if the xml element for this binding has a xsi:nil attribute set to true and if we must honor this, meaning if 
     * it {@link #isNillable() }
     *
     * @param staxReader the staReader that has just read this binding's start element tag.
     * @return true if the element from this stream representing this binding is nil.
     * @throws XMLStreamException propagate exceptions that may occur while reading the xml stream
     */
    boolean isNil(RecordAndPlaybackXMLStreamReader staxReader) throws XMLStreamException {
        if (containsNil(staxReader.getAttributes())) {
            if (!isNillable()) {
                throw new Xb4jUnmarshallException(String.format("Found unexpected nil-attribute on xml element <%s>. Consider "
                        + "adding the NILLABLE option to the binding", getElement()), this);
            }
            return true;
        }
        return false;
    }

    /**
     * Determine if the xml for this binding can contain the nil-attribute
     *
     * @return true if this binding supports the nil-attribute, false otherwise
     */
    boolean isNillable() {
        return (getElement() != null) && hasOption(SchemaOptions.NILLABLE);
    }

}
