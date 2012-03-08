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
package info.rsdev.xb4j.model;

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
public class SimpleTypeBinding extends AbstractBindingBase {
	
	private IValueConverter converter = NOPConverter.INSTANCE;
    
    /**
     * Create a new {@link SimpleTypeBinding} with a {@link DefaultElementFetchStrategy}
     * @param element the element 
     */
    public SimpleTypeBinding(QName element) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    }

    public SimpleTypeBinding(QName element, IValueConverter converter) {
    	setConverter(converter);
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    }

    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();
        if ((expectedElement != null) && !staxReader.isAtElementStart(expectedElement)) {
        	return null;	//when mandatory: throw exception? -- but then we also need to add probe functionality to support ChoiceBinding
        }
        
        Object value = this.converter.toObject(staxReader.getElementText());	//this also reads the end element
        setProperty(javaContext, value);
        
        return value;
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
        return String.format("SimpleTypeBinding[element=%s]", getElement());
    }
    
}
