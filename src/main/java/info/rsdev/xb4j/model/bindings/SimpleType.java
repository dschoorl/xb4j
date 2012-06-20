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
    public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();	//should never be null for a SimpleType
    	boolean startTagFound = false;
    	if (expectedElement != null) {
    		if (!staxReader.isAtElementStart(expectedElement)) {
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

        Object value = this.converter.toObject(staxReader.getElementText());	//this also consumes the end element
        boolean isValueHandled = setProperty(javaContext, value);
        
    	if ((expectedElement != null) && !staxReader.isAtElementEnd(expectedElement) && startTagFound) {
    		String encountered =  (staxReader.isAtElement()?String.format("(%s)", staxReader.getName()):"");
    		throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", expectedElement,
    				staxReader.getEventName(), encountered), this);
        }
        
        return new UnmarshallResult(value, isValueHandled);
    }
    
    @Override
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
    	if (!generatesOutput(javaContext)) { return; }
    			
        QName element = getElement();
        javaContext = getProperty(javaContext);
        boolean isEmpty = (javaContext == null);
        if (isEmpty && !isOptional()) {	//TODO: check if element is nillable and output nill value for this element
        	throw new Xb4jMarshallException(String.format("No content for mandatory element %s", element), this);	//this does not support an empty element
        }
        
        if (!isOptional() || !isEmpty) {
        	staxWriter.writeElement(element, isEmpty);	//suppress empty optional elements
            attributesToXml(staxWriter, javaContext);
        }
        
        if (!isEmpty) {
            staxWriter.writeContent(this.converter.toText(javaContext));
            staxWriter.closeElement(element);
        }
    }
    
    @Override
    public boolean generatesOutput(Object javaContext) {
    	javaContext = getProperty(javaContext);
    	if (javaContext != null) {
    		return true;
    	}
		return (getElement() != null) && (hasAttributes() || !isOptional());	//suppress optional empty elements (empty means: no content and no attributes)
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
    
}
