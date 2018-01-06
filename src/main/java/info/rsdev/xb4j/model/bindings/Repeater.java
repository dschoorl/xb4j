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
import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.MethodSetter;
import info.rsdev.xb4j.model.java.accessor.MimicSetter;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.java.constructor.NullCreator;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import java.util.Collection;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class is called an {@link Repeater} and not a {@link Collection}, in order to avoid type name collission with the Java
 * Collections interface.
 *
 * @author Dave Schoorl
 */
public class Repeater extends AbstractBinding {

    public static final int UNBOUNDED = Integer.MAX_VALUE;

    private IBinding itemBinding = null;

    private int maxOccurs = UNBOUNDED;

    /**
     * Create a {@link Repeater} where the collection instance is passed on from the parent binding in the binding tree. The main
     * use case is to support the {@link Root} xml element to contain a {@link Collection} without an additional container element.
     * @param isOptional
     * @param options
     */
    @SafeVarargs
    public Repeater(boolean isOptional, Enum<? extends BindOption>... options) {
        super(NoElementFetchStrategy.INSTANCE, NullCreator.INSTANCE, isOptional, options);
        setSetter(MimicSetter.INSTANCE);
    }

    /**
     * Create a {@link Repeater} where the underlying collection is of the specified type. The type must be a concrete class, so it
     * can be created during unmarshalling process (xml to java).
     *
     * @param collectionType
     * @param isOptional
     * @param options
     */
    @SafeVarargs
    public Repeater(Class<?> collectionType, boolean isOptional, Enum<? extends BindOption>... options) {
        super(NoElementFetchStrategy.INSTANCE, new DefaultConstructor(collectionType), isOptional, options);
    }

    @SafeVarargs
    public Repeater(QName element, Class<?> collectionType, boolean isOptional, Enum<? extends BindOption>... options) {
        super(new DefaultElementFetchStrategy(element), new DefaultConstructor(collectionType), isOptional, options);
    }

    public <T extends IBinding> T setItem(T itemBinding) {
        if (itemBinding == null) {
            throw new NullPointerException("Binding for collection items cannot be null");
        }

        getSemaphore().lock();
        try {
            validateMutability();
            this.itemBinding = itemBinding;
            this.itemBinding.setParent(this);
            itemBinding.setSetter(new MethodSetter("add")); // default add method for Collection interface;
            return itemBinding;
        } finally {
            getSemaphore().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext)
            throws XMLStreamException {
        JavaContext javaCollectionContext = select(javaContext, newInstance(staxReader, javaContext));
        Object contextObject = javaCollectionContext.getContextObject();
        if (!(contextObject instanceof Collection<?>)) {
            throw new Xb4jUnmarshallException(String.format("Not a Collection: %s", contextObject), this);
        }
        Collection<Object> collection = (Collection<Object>) contextObject;

        // read enclosing collection element (if defined)
        QName collectionElement = getElement();
        if (collectionElement != null) {
            if (!staxReader.isNextAnElementStart(collectionElement)) {
                if (isOptional()) {
                    return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
                }
                return UnmarshallResult.newMissingElement(this);
            }
        }

        attributesToJava(staxReader, javaCollectionContext);

        if (isNil(staxReader)) {
            return handleNil(staxReader);
        } else {
            int occurences = 0;
            UnmarshallResult result = null;

            /*
             * We use a previous UnmarshallResult to detect when we are getting into an endless loop: in that case, the previous and
             * current result will be equal to each other.
             */
            boolean proceed = true;
            while (proceed) {
                result = itemBinding.toJava(staxReader, javaCollectionContext);

                /* Proceed in the following circumstances:
                 * - A binding returned a value (either handled or unhandled), except for Ignore...?
                 * - There are no errors
                 * - All yielded values of a sequence have been handled
                 *
                 * Do NOT proceed, when
                 * - A choice returned NO_RESULT (none of the optional values were found)
                 */
                proceed = !result.isError();
                proceed = proceed && result.hasUnmarshalledObject();
                proceed = proceed && !result.mustHandleUnmarshalledObject();
                if (proceed) {
                    if (result.mustHandleUnmarshalledObject()) {
                        // the itemBinding has been given the add-method setter when it was set on this Repeater binding
                        throw new Xb4jException("ItemBinding unexpectedly did not add unmarshalled object to Collection");
                    }
                    occurences++;
                    if ((maxOccurs != UNBOUNDED) && (occurences > maxOccurs)) {
                        throw new Xb4jUnmarshallException(
                                String.format("Found %d occurences, but no more than %d are allowed", occurences, maxOccurs), this);
                    }
                }
            }

            // determine if the childBinding has no more occurences or whether the xml fragment of the childBinding is incomplete
            if ((occurences == 0) && !isOptional()) {
                if (ErrorCodes.MISSING_MANDATORY_ERROR.equals(result.getErrorCode())) {
                    return result;
                }
                return new UnmarshallResult(UnmarshallResult.MISSING_MANDATORY_ERROR,
                        String.format("Mandatory %s has no content: %s", this, staxReader.getLocation()), this);
            }

            // read end of enclosing collection element (if defined)
            if ((collectionElement != null) && !staxReader.isNextAnElementEnd(collectionElement)) {
                if (ErrorCodes.MISSING_MANDATORY_ERROR.equals(result.getErrorCode())) {
                    return result;
                }
                String encountered = (staxReader.isAtElement() ? String.format("(%s)", staxReader.getName()) : "");
                throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s",
                        collectionElement, staxReader.getEventName(), encountered), this);
            }

            boolean isHandled = false;
            if (javaContext.getContextObject() != null) {
                isHandled = setProperty(javaContext, collection);
            }
            return new UnmarshallResult(collection, isHandled);
        }
    }

    /**
     * Get the binding for the items in this collection. Normally, this is {@link #itemBinding} defined on this {@link Repeater},
     * but that could be a {@link Reference}, in which case we need to look a little further to find the real {@link IBinding} for
     * the collection items.
     *
     * @return the {@link IBinding} for the collection items
     */
    private IBinding resolveItemBinding(IBinding itemBinding) {
        if (!(itemBinding instanceof Reference)) {
            return itemBinding;
        }

        if (itemBinding.getElement() != null) {
            return itemBinding; // the element name is defined on the Reference
        }

        Reference reference = (Reference) itemBinding;
        itemBinding = reference.getChildBinding(); // itemBinding now is a ComplexType
        if (itemBinding.getElement() != null) {
            return itemBinding; // the element name is defined in the ComplexType
        }

        itemBinding = ((ComplexType) itemBinding).getChildBinding();
        if (itemBinding instanceof Reference) {
            if (reference.equals(itemBinding)) {
                throw new Xb4jException(
                        "Encountered cirsular reference; Reference pointing to itself: ".concat(reference.toString()));
            }
            return resolveItemBinding(itemBinding); // silly situation: a Reference pointing to a Reference, but support it anyway
        }
        return itemBinding;
    }

    @Override
    public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        JavaContext nextJavaContext = getProperty(javaContext);
        if ((nextJavaContext.getContextObject()  == null) && isNillable()) {
            nilToXml(staxWriter, nextJavaContext);
        } else {
            if (generatesOutput(javaContext) == OutputState.NO_OUTPUT) {
                return;
            }

            Object collection = nextJavaContext.getContextObject();
            if ((collection != null) && (!(collection instanceof Collection<?>))) {
                throw new Xb4jMarshallException(String.format("Not a Collection: %s", collection), this);
            }

            if ((collection == null) || ((Collection<?>) collection).isEmpty()) {
                if (isOptional()) {
                    return;
                } else {
                    throw new Xb4jMarshallException(String.format("This collection is not optional: %s", this), this);
                }
            }

            // when this Binding must not output an element, the getElement() method should return null
            QName element = getElement();
            boolean isEmptyElement = (itemBinding == null) || (javaContext == null);
            if (element != null) {
                staxWriter.writeElement(element, isEmptyElement);
                attributesToXml(staxWriter, javaContext);
            }

            if (itemBinding != null) {
                int index = 0;
                for (Object item : (Collection<?>) collection) {
                    itemBinding.toXml(staxWriter, javaContext.newContext(item, index));
                    index++;
                }
            }

            if (element != null) {
                staxWriter.closeElement(element, isEmptyElement);
            }
        }
    }

    @Override
    public OutputState generatesOutput(JavaContext javaContext) {
        Object collection = getProperty(javaContext).getContextObject();
        if (collection != null) {
            if (!(collection instanceof Collection<?>)) {
                throw new Xb4jMarshallException(String.format("Not a Collection type: %s", collection.getClass()), this);
            }
            if (!((Collection<?>) collection).isEmpty()) {
                for (Object item : (Collection<?>) collection) {
                    if (itemBinding.generatesOutput(javaContext.newContext(item)) == OutputState.HAS_OUTPUT) {
                        return OutputState.HAS_OUTPUT;
                    }
                }
            }
        }

        // At this point, the we established that the itemBinding will not output content
        if ((getElement() != null) && (hasAttributes() || !isOptional())) {	//suppress optional empty elements (empty means: no content and no attributes)
            return OutputState.HAS_OUTPUT;
        }
        return OutputState.NO_OUTPUT;
    }

    public Repeater setMaxOccurs(int newMaxOccurs) {
        if (newMaxOccurs < 1) {
            throw new Xb4jException("maxOccurs must be 1 or higher: " + newMaxOccurs);
        }
        getSemaphore().lock();
        try {
            validateMutability();
            this.maxOccurs = newMaxOccurs;
            return this;
        } finally {
            getSemaphore().unlock();
        }
    }

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
            sb.append(separator).append("collectionType=").append(collectionType.getName());
            separator = ",";
        }
        IBinding item = resolveItemBinding(itemBinding);
        if (item != null) {
            sb.append(separator).append("item=").append(item.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void resolveReferences() {
        IBinding branch = resolveItemBinding(itemBinding);
        if ((branch != null) && (!(branch instanceof ComplexType))) {
            branch.resolveReferences();
        }
    }

    @Override
    public void validateContextObject(JavaContext javaContext) throws Xb4jException {
        Object collection = getProperty(javaContext).getContextObject();
        if (collection != null) {
            if (!(collection instanceof Collection<?>)) {
                throw new Xb4jMarshallException(String.format("Not a Collection type: %s", collection.getClass()), this);
            }
        }
    }
}
