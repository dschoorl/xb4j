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

import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.converter.IValueConverter;
import info.rsdev.xb4j.model.converter.NOPConverter;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.constructor.NullCreator;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * <p>
 * Translates a text-only element to a Java field and vice versa. The Java field is expected to be a String. Other types will need a
 * converter to convert the field to and from a String.</p>
 *
 * TODO: add support for fixed / default values in the xml world? TODO: simple type cannot be an empty element??
 *
 * @author Dave Schoorl
 */
public class SimpleType extends AbstractBinding {

    private IValueConverter converter = NOPConverter.INSTANCE;

    /**
     * Create a new {@link SimpleType} with a {@link DefaultElementFetchStrategy}
     *
     * @param element the element
     * @param isOptional
     * @param options
     */
    @SafeVarargs
    public SimpleType(QName element, boolean isOptional, Enum<? extends BindOption>... options) {
        super(new DefaultElementFetchStrategy(element), NullCreator.INSTANCE, isOptional, options);
    }

    @SafeVarargs
    public SimpleType(QName element, IValueConverter converter, boolean isOptional, Enum<? extends BindOption>... options) {
        super(new DefaultElementFetchStrategy(element), NullCreator.INSTANCE, isOptional, options);
        setConverter(converter);
    }

    @Override
    public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) 
            throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();	//should never be null for a SimpleType
        boolean startTagFound = false;
        if (expectedElement != null) {
            if (!staxReader.isCurrentAnElementStart(expectedElement) && !staxReader.isNextAnElementStart(expectedElement)) {
                if (isOptional()) {
                    return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
                } else {
                    return UnmarshallResult.newMissingElement(this);
                }
            } else {
                startTagFound = true;
            }
        }

        attributesToJava(staxReader, javaContext);

        Object value = this.converter.toObject(javaContext, staxReader.getElementText());
        boolean isValueHandled = setProperty(javaContext, value);

        if ((expectedElement != null) && !staxReader.isNextAnElementEnd(expectedElement) && startTagFound) {    //this also consumes the end element
            String encountered = (staxReader.isAtElement() ? String.format("(%s)", staxReader.getName()) : "");
            throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", 
                    expectedElement, staxReader.getEventName(), encountered), this);
        }

        return new UnmarshallResult(value, isValueHandled);
    }

    @Override
    public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        if (generatesOutput(javaContext) == OutputState.NO_OUTPUT) {
            return;
        }

        QName element = getElement();
        javaContext = getProperty(javaContext);
        if ((javaContext.getContextObject() == null) && !isOptional()) {	//TODO: check if element is nillable and output nill value for this element
            throw new Xb4jMarshallException(String.format("No content for mandatory element %s", element), this);	//this does not support an empty element
        }

        String value = this.converter.toText(javaContext, javaContext.getContextObject());
        boolean isEmptyElement = (value == null);
        if (!isOptional() || !isEmptyElement) {
            staxWriter.writeElement(element, isEmptyElement);	//suppress empty optional elements
            attributesToXml(staxWriter, javaContext);
        }

        if (!isEmptyElement) {
            staxWriter.writeContent(value);
        }
        staxWriter.closeElement(element, isEmptyElement);
    }

    @Override
    public OutputState generatesOutput(JavaContext javaContext) {
        javaContext = getProperty(javaContext);
        if (javaContext.getContextObject() != null) {
            //when a binding has data for output, it should always be generated
            return OutputState.HAS_OUTPUT;
        }

        if (attributesGenerateOutput(javaContext) == OutputState.HAS_OUTPUT) {
            return OutputState.HAS_OUTPUT;
        }

        //when there is nothing to output
        return isOptional() ? OutputState.NO_OUTPUT : OutputState.COLLABORATE;
    }

    private void setConverter(IValueConverter converter) {
        if (converter == null) {
            throw new NullPointerException("IValueConverter cannot be null");
        }
        this.converter = converter;
    }

    @Override
    public Class<?> getJavaType() {
        return this.converter.getJavaType();
    }

    @Override
    public String toString() {
        return String.format("SimpleType[path=%s]", getPath());
    }

    @Override
    public void resolveReferences() {
        //there are no child bindings to resolve references for... nothing to do
    }

}
