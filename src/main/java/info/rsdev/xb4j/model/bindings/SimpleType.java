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
import info.rsdev.xb4j.model.java.converter.IValueConverter;
import info.rsdev.xb4j.model.java.converter.NOPConverter;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * <p>Translates a text-only element to a Java field and vice versa. The Java field is expected to be a String.
 * Other types will need a converter to convert the field to and from a String.</p>
 * 
 * TODO: add support for fixed / default values in the xml world?
 * TODO: simple type cannot be an empty element??
 * 
 * @author Dave Schoorl
 */
public class SimpleType extends AbstractBinding {
	
	private IValueConverter converter = NOPConverter.INSTANCE;
    
    /**
     * Create a new {@link SimpleType} with a {@link DefaultElementFetchStrategy}
     * @param element the element 
     */
    public SimpleType(QName element) {
    	super(new DefaultElementFetchStrategy(element), null);
    }

    public SimpleType(QName element, IValueConverter converter) {
    	super(new DefaultElementFetchStrategy(element), null);
    	setConverter(converter);
    }

    @Override
    public IUnmarshallResponse toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();
    	boolean startTagFound = false;
    	if (expectedElement != null) {
    		if (!staxReader.isAtElementStart(expectedElement)) {
	    		if (isOptional()) {
                    return DefaultResponse.MISSING_OPTIONAL_ELEMENT;
	    		} else {
                    return DefaultResponse.newMissingElement(expectedElement);
	    		}
    		} else {
    			startTagFound = true;
    		}
    	}
        
        Object value = this.converter.toObject(staxReader.getElementText());	//this also reads the end element
        boolean isValueHandled = setProperty(javaContext, value);
        if (startTagFound && staxReader.isAtElement()) {
        	if (!expectedElement.equals(staxReader.getName())) {
        		String encountered =  (staxReader.isAtElement()?String.format("(%s)", staxReader.getName()):"");
        		throw new Xb4jException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", expectedElement,
        				staxReader.getEventName(), encountered));
        	}
        }
        
        return new DefaultResponse(value, isValueHandled);
    }
    
    @Override
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        QName element = getElement();
        
        Object elementValue = getProperty(javaContext);
        if ((elementValue == null) && (!isOptional())) {
        	throw new Xb4jException(String.format("No text for mandatory element %s", element));
        }
        
        boolean isEmpty = (elementValue == null);
        if (!isOptional() || !isEmpty) {
        	staxWriter.writeElement(element, isEmpty);	//suppress empty optional elements
        }
        
        if (!isEmpty) {
            staxWriter.writeContent(this.converter.toText(elementValue));
            staxWriter.closeElement(element);
        }
        
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
        return String.format("SimpleType[element=%s]", getElement());
    }
    
}