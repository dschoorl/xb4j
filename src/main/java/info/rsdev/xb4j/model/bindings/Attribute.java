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

import info.rsdev.xb4j.model.converter.IValueConverter;
import info.rsdev.xb4j.model.converter.NOPConverter;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * 
 * @author Dave Schoorl
 */
public class Attribute extends AbstractAttribute {
	
	private IValueConverter converter = NOPConverter.INSTANCE;
    
    private String defaultValue = null;
	
    public Attribute(QName attributeName) {
    	this(attributeName, NOPConverter.INSTANCE);
    }
    
    public Attribute(QName attributeName, IValueConverter converter) {
    	super(attributeName);
    	setConverter(converter);
    }
    
    @Override
	public void toJava(String valueAsText, Object javaContext) throws XMLStreamException {
    	if ((valueAsText == null) && (this.defaultValue != null)) {
    		valueAsText = this.defaultValue;
    	}
        Object value = this.converter.toObject(valueAsText);
        setProperty(javaContext, value);
    }
    
    @Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext, QName elementName) throws XMLStreamException {
        QName attributeName = getAttributeName();
        String value = this.converter.toText(getProperty(javaContext));
        if ((value == null) && (defaultValue != null)) {
        	value = defaultValue;
        }
        if (isRequired() || (value != null)) {
       		staxWriter.writeAttribute(elementName, attributeName, value);
        }
    }
    
    private void setConverter(IValueConverter converter) {
    	if (converter == null) {
    		throw new NullPointerException("IValueConverter cannot be null");
    	}
    	this.converter = converter;
    }
    
    @Override
	public IAttribute setDefault(String defaultValue) {
    	if (defaultValue == null) {
    		throw new NullPointerException("No value provided for default value");
    	}
    	this.defaultValue = defaultValue;
    	return this;
    }

	@Override
	public String getDefaultValue() {
		return this.defaultValue;
	}
    
}
