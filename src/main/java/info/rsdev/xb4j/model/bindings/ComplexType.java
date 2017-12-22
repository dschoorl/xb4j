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

import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.FieldAccessor;
import info.rsdev.xb4j.model.java.constructor.NullCreator;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * <p>
 * This binding can be used as an anonymous type in a RootBinding hierarchy, or it can be registered as a type with a
 * {@link BindingModel}, so that the definition can be reused. Reuse is accomplished by adding a {@link Reference} into the
 * RootBinding hierarchy, that references the ComplexTypeBinding.</p>
 *
 * @see Reference
 * @author Dave Schoorl
 */
public class ComplexType extends AbstractSingleBinding implements IModelAware {

    private String identifier = null;   //only needed when registered with BindingModel

    private String namespaceUri = null; //only needed when registered with BindingModel

    private BindingModel model = null;  //this is set on ComplexTypeBindings that are registered with the BindingModel

    /**
     * Flag that indicates whether this {@link ComplexType} can be changed or not. A complex type is made immutable, when it is
     * linked to a {@link Reference} type (usually the first time it is used to marshall/unmarshall, so that it can be used in a
     * thread safe manner. When a {@link #copy()} is made, the copy is mutable again.
     */
    private final AtomicBoolean isImmutable = new AtomicBoolean(false);

    /**
     * Create a ComplexTypeReference for an anonymous ComplexType (not registered with {@link BindingModel}
     *
     * @param element
     * @param parent
     * @param fieldName
     * @param isOptional
     * @param options
     */
    @SafeVarargs
    public ComplexType(QName element, IBinding parent, String fieldName, boolean isOptional, Enum<? extends BindOption>... options){
        super(new DefaultElementFetchStrategy(element), NullCreator.INSTANCE, isOptional, options);
        if (parent == null) {
            throw new NullPointerException("Parent IBinding cannot be null");
        }
        Reference reference = new Reference(element, this, isOptional);
        if (parent instanceof ISingleBinding) {
            ((ISingleBinding) parent).setChild(reference);
        } else if (parent instanceof IContainerBinding) {
            ((IContainerBinding) parent).add(reference);
        }

        //In the case of anonymous ComplexType, the setter must be on the ComplexType
        FieldAccessor provider = new FieldAccessor(fieldName);
        setGetter(provider);
        setSetter(provider);
    }

    /**
     * Create a new {@link ComplexType} with the purpose to be referenced by a {@link Reference}
     *
     * @param identifier
     * @param namespaceUri
     * @param isOptional
     * @param options
     */
    @SafeVarargs
    public ComplexType(String identifier, String namespaceUri, boolean isOptional, Enum<? extends BindOption>... options) {
        super(NoElementFetchStrategy.INSTANCE, NullCreator.INSTANCE, isOptional, options);
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        //the element fetch strategy will be replaced by a real one when this 'type' is copied into a binding hierarchy
    }

    /**
     * Copy constructor that creates a copy of ComplexTypeBinding with the given {@link Reference parent} as it's parent
     */
    private ComplexType(ComplexType original) {
        super(original, NoElementFetchStrategy.INSTANCE);	//dirty hack, I want to do: new FetchFromParentStrategy(this), but cannot pass on 'this' in super contructor call
        this.identifier = original.identifier;
        this.namespaceUri = original.namespaceUri;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    private void setIdentifier(String newIdentifer) {
        if (newIdentifer == null) {
            throw new NullPointerException("Identifier cannot be null");
        }

        getSemaphore().lock();
        try {
            validateMutability();
            this.identifier = newIdentifer;
        } finally {
            getSemaphore().unlock();
        }
    }

    public String getNamespace() {
        return this.namespaceUri;
    }

    private void setNamespaceUri(String newNamespaceUri) {
        if (newNamespaceUri == null) {
            newNamespaceUri = XMLConstants.NULL_NS_URI;
        }

        getSemaphore().lock();
        try {
            validateMutability();
            this.namespaceUri = newNamespaceUri;
        } finally {
            getSemaphore().unlock();
        }
    }

    @Override
    public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();
        if (expectedElement != null) {
            if (!staxReader.isNextAnElementStart(expectedElement)) {
                if (isOptional()) {
                    return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
                }
                return UnmarshallResult.newMissingElement(this);
            }
        }
        /* A ComplexType is linked to a Reference type and we don't know where a new context object is created, or which
		 * binding has the getter / setter to set the unmarshalled value in the Java object tree.
         */
        JavaContext newJavaContext = newInstance(staxReader, javaContext);
        attributesToJava(staxReader, select(javaContext, newJavaContext));

        if (isNil(staxReader)) {
            return handleNil(staxReader);
        } else {
            UnmarshallResult result = getChildBinding().toJava(staxReader, select(javaContext, newJavaContext));
            if (!result.isUnmarshallSuccessful()) {
                return result;
            }

            //before processing the result of the unmarshalling, first check if the xml is well-formed
            if ((expectedElement != null) && !staxReader.isNextAnElementEnd(expectedElement)) {
                String encountered = (staxReader.isAtElement() ? String.format("(%s)", staxReader.getName()) : "");
                throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered %s %s", expectedElement,
                        staxReader.getEventName(), encountered), this);
            }

            // When this binding has created a new JavaContextObject, it must be passed on back to it's parent binding. When
            if (result.mustHandleUnmarshalledObject()) {
                //process the result from the unmarshall step by the childbinding
                if (!setProperty(select(javaContext, newJavaContext), result.getUnmarshalledObject())) {
                    if (newJavaContext.getContextObject() == null) {
                        return result;
                    } else {
                        throw new Xb4jUnmarshallException(String.format("Unmarshalled value '%s' is not set in the java context %s and will be "
                                + "lost. Please check your bindings: %s", result.getUnmarshalledObject(), javaContext, this), this);
                    }
                }
            }

            /* The unmarshall result of the childbinding is handled. It is set on the newly created context object, if it is not null,
             * otherwise it is set on the existing context object. However, if the new context object is not null, it could be that
             * it must be set on the existing context object or being handled by the parent binding
             */
            if (setProperty(javaContext, newJavaContext.getContextObject())) {
                return new UnmarshallResult(newJavaContext.getContextObject(), true);
            }

            return new UnmarshallResult(newJavaContext.getContextObject());
        }
    }

    @Override
    public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        //mixed content is not yet supported -- there are either child elements or there is content
        JavaContext nextJavaContext = getProperty(javaContext);
        if ((nextJavaContext.getContextObject()  == null) && isNillable()) {
            nilToXml(staxWriter, nextJavaContext);
        } else {
            if (generatesOutput(javaContext) == OutputState.NO_OUTPUT) {
                return;
            }

            IBinding child = getChildBinding();
            QName element = getElement();
            boolean isEmpty = (child == null) || (child.generatesOutput(javaContext) == OutputState.NO_OUTPUT);
            boolean mustOutputElement = ((element != null) && (!isOptional() || !isEmpty));
            if (mustOutputElement) {
                staxWriter.writeElement(element, isEmpty);
                attributesToXml(staxWriter, nextJavaContext);
            }

            if (!isEmpty) {
                child.toXml(staxWriter, nextJavaContext);
            }

            if (mustOutputElement) {
                staxWriter.closeElement(element, isEmpty);
            }
        }
    }

    @Override
    public OutputState generatesOutput(JavaContext javaContext) {
        javaContext = getProperty(javaContext);
        if (javaContext.getContextObject() != null) {
            IBinding child = getChildBinding();
            if ((child != null) && (child.generatesOutput(javaContext) == OutputState.HAS_OUTPUT)) {
                return OutputState.HAS_OUTPUT;
            }
        }

        //At this point, the childBinding will have no output
        if ((getElement() != null) && (hasAttributes() || !isOptional())) {	//suppress optional empty elements (empty means: no content and no attributes)
            return OutputState.HAS_OUTPUT;
        }
        return OutputState.NO_OUTPUT;
    }

    /**
     * Copy the ComplexTypeHierarchy and place it as a child under the supplied {@link Reference parent}
     *
     * @return a copy of this {@link ComplexType}
     */
    ComplexType copy() {
        return new ComplexType(this);
    }

    @Override
    public void setModel(BindingModel model) {
        if (model == null) {
            throw new NullPointerException("BindingModel cannot be null");
        }
        if ((this.model != null) && !this.model.equals(model)) {
            throw new IllegalArgumentException("It is currently not supported that a ComplexTypeBinding is added to multiple BindingModels");
        }

        getSemaphore().lock();
        try {
            validateMutability();
            this.model = model;
        } finally {
            getSemaphore().unlock();
        }
    }

    @Override
    public BindingModel getModel() {
        return this.model;
    }

    @Override
    public boolean isImmutable() {
        getSemaphore().lock();
        try {
            return this.isImmutable.get();
        } finally {
            getSemaphore().unlock();
        }
    }

    /**
     * Resolve all references to {@link ComplexType}s
     *
     * @see info.rsdev.xb4j.model.bindings.IModelAware#makeImmutable()
     */
    @Override
    public void makeImmutable() {
        getSemaphore().lock();
        try {
            if (!isImmutable.compareAndSet(false, true)) {
                //TODO: log that the ComplexType was already immutable?
            }
        } finally {
            getSemaphore().unlock();
        }
    }

}
