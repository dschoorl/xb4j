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
import info.rsdev.xb4j.model.java.accessor.BeanPropertyAccessor;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class adds support for marshalling and unmarshalling recursive elements.
 *
 * @author dschoorl
 */
public class Recursor extends AbstractSingleBinding {

    public static final int UNBOUNDED = Integer.MAX_VALUE;

    private IGetter recursiveGetter = null;

    private ISetter recursiveSetter = null;

    private int maxOccurs = UNBOUNDED;

    /**
     * Create a {@link Recursor} where the recursive type is the specified type.
     *
     * @param element
     * @param recursiveType
     * @param propertyName
     */
    public Recursor(QName element, Class<?> recursiveType, String propertyName) {
        this(element, recursiveType, propertyName, false);
    }

    public Recursor(QName element, Class<?> recursiveType, String propertyName, boolean isOptional) {
        super(new DefaultElementFetchStrategy(element), new DefaultConstructor(recursiveType), isOptional);
        BeanPropertyAccessor accessor = new BeanPropertyAccessor(propertyName);
        this.recursiveGetter = accessor;
        this.recursiveSetter = accessor;
    }

    @Override
    public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        UnmarshallResult result = unmarshall(staxReader, javaContext, 0);
        if (result.mustHandleUnmarshalledObject()) {
            if (setProperty(javaContext, result.getUnmarshalledObject())) {
                result.setHandled();
            }
        }
        return result;
    }

    private UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext, int recurrenceCount) throws XMLStreamException {

        QName element = getElement();	//can this be null for a Recursor? - No!
        if (element == null) {
            throw new Xb4jUnmarshallException("A recursive property must always have an xml element representation", this);
        }

        if (!staxReader.isNextAnElementStart(element)) {
            if (recurrenceCount == 0) {
                if (isOptional()) {
                    return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
                } else {
                    return UnmarshallResult.newMissingElement(this);
                }
            } else {
                return UnmarshallResult.NO_RESULT;
            }
        }

        JavaContext nestedObjectContext = newInstance(staxReader, javaContext);
        attributesToJava(staxReader, nestedObjectContext);

        if (getChildBinding() != null) {
            UnmarshallResult result = getChildBinding().toJava(staxReader, nestedObjectContext);
            if (!result.isUnmarshallSuccessful()) {
                return result;
            }
            if (result.mustHandleUnmarshalledObject()) {
                //we expect that the contentBinding has set it's constructed artifacts in the object tree... so, throw an exception
                throw new Xb4jUnmarshallException("Unmarshalled result should have been handled by the ContentBinding", this);
            }
        }

        recurrenceCount++;
        if ((maxOccurs != UNBOUNDED) && (recurrenceCount > maxOccurs)) {
            throw new Xb4jUnmarshallException(String.format("Found %d nested occurences of element <%s>, but no mare than %d "
                    + "levels are allowed", recurrenceCount, element, maxOccurs), this);
        }

        //Recurse -- create one level deeper nested object
        UnmarshallResult result = unmarshall(staxReader, nestedObjectContext, recurrenceCount);
        if (!result.isUnmarshallSuccessful()) {
            return result;
        }

        //handle nested child object -- add to object tree
        if (result.mustHandleUnmarshalledObject()) {
            if (!setChild(nestedObjectContext, result.getUnmarshalledObject())) {
                throw new Xb4jUnmarshallException("Cannot set nested element into it's parent object", this);
            }
        }

        //check that element close is encountered and return unmarshalled result if appropriate
        if (!staxReader.isNextAnElementEnd(element)) {
            String encountered = (staxReader.isAtElement() ? String.format("(%s)", staxReader.getName()) : "");
            throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered %s %s", element,
                    staxReader.getEventName(), encountered), this);
        }

        return new UnmarshallResult(nestedObjectContext.getContextObject());
    }

    @Override
    public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        Object contextObject = getProperty(javaContext).getContextObject();
        marshallRecursor(staxWriter, javaContext.newContext(contextObject, 0));
    }

    private void marshallRecursor(SimplifiedXMLStreamWriter staxWriter, JavaContext recurringObject) throws XMLStreamException {
        int recurrenceCount = recurringObject.getIndexInCollection();
        if ((generatesOutput(recurringObject, recurrenceCount)) == OutputState.NO_OUTPUT) {
            return;
        }

        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        boolean isEmptyElement = (generatesOutput(getChild(recurringObject), recurrenceCount + 1) == OutputState.NO_OUTPUT);
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
            attributesToXml(staxWriter, recurringObject);
        }

        if (getChildBinding() != null) {
            getChildBinding().toXml(staxWriter, recurringObject);
        }

        recurrenceCount++;
        if (recurrenceCount > maxOccurs) {
            throw new Xb4jMarshallException(String.format("More recurring instances (%d) encountered than allowed: %d",
                    recurrenceCount, maxOccurs), this);
        }

        marshallRecursor(staxWriter, getChild(recurringObject));

        if (element != null) {
            staxWriter.closeElement(element, isEmptyElement);
        }

    }

    @Override
    public OutputState generatesOutput(JavaContext javaContext) {
        return generatesOutput(getProperty(javaContext), 0);
    }

    public OutputState generatesOutput(JavaContext recurringObject, int recurrenceCount) {
        if ((recurringObject != null) && (getChildBinding() != null)) {
            if ((getChildBinding().generatesOutput(recurringObject)) == OutputState.HAS_OUTPUT) {
                return OutputState.HAS_OUTPUT;
            }
        }

        //At this point, we established that the contentBinding will not output content
        if (((recurringObject != null) && (recurringObject.getContextObject() != null))
                && (getElement() != null) && (hasAttributes() || !isOptional())) {	//suppress optional empty elements (empty means: no content and no attributes)
            return OutputState.HAS_OUTPUT;
        }
        return OutputState.NO_OUTPUT;
    }

    public Recursor setMaxOccurs(int newMaxOccurs) {
        if (newMaxOccurs <= 1) {
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

    private JavaContext getChild(JavaContext recurringObject) {
        int nextOccurrenceCount = recurringObject.getIndexInCollection() + 1;
        if (recurringObject.getContextObject() == null) {
            return recurringObject.newContext(null, nextOccurrenceCount);
        }
        JavaContext newContext = this.recursiveGetter.get(recurringObject);
        return recurringObject.newContext(newContext.getContextObject(), nextOccurrenceCount);
    }

    private boolean setChild(JavaContext recurringObject, Object propertyValue) {
        return this.recursiveSetter.set(recurringObject, propertyValue);
    }

    @Override
    public Recursor setOptional(boolean isOptional) {
        super.setOptional(isOptional);
        return this;
    }

}
