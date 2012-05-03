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
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * 
 * @author dschoorl
 */
public class Attribute {
	
	private IValueConverter converter = NOPConverter.INSTANCE;
    
    private IGetter getter = null;
    
    private ISetter setter = null;
    
	private QName attributeName = null;
	
	private boolean isRequired = false;
	
	private String defaultValue = null;
	
    public Attribute(QName attributeName) {
    	setAttributeName(attributeName);
    	setConverter(NOPConverter.INSTANCE);
    }
    
    public Attribute(QName attributeName, IValueConverter converter) {
    	setAttributeName(attributeName);
    	setConverter(converter);
    }
    
    public QName getAttributeName() {
    	return this.attributeName;
    }
    
    private void setAttributeName(QName attributeName) {
    	if (attributeName == null) {
    		throw new NullPointerException("Attribute QName cannot be null");
    	}
    	this.attributeName = attributeName;
    }
    
    public void toJava(String valueAsText, Object javaContext) throws XMLStreamException {
    	if ((valueAsText == null) && (this.defaultValue != null)) {
    		valueAsText = this.defaultValue;
    	}
        Object value = this.converter.toObject(valueAsText);
        setProperty(javaContext, value);
    }
    
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext, QName elementName) throws XMLStreamException {
        QName attributeName = getAttributeName();
        String value = this.converter.toText(getProperty(javaContext));
        if ((value == null) && (defaultValue != null)) {
        	value = defaultValue;
        }
        if (isRequired || (value != null)) {
        	try {
        		staxWriter.writeAttribute(elementName, attributeName, value);
        	} catch(XMLStreamException e) {
        		System.out.printf("Error writing attribute %s for element %s%n", attributeName, elementName);
        		throw e;
        	}
        }
    }
    
    public Object getProperty(Object contextInstance) {
    	if (contextInstance == null) {
    		return defaultValue;
    	}
        return this.getter.get(contextInstance);
    }
    
    public boolean setProperty(Object contextInstance, Object propertyValue) {
        return this.setter.set(contextInstance, propertyValue);
    }
    
    private void setConverter(IValueConverter converter) {
    	if (converter == null) {
    		throw new NullPointerException("IValueConverter cannot be null");
    	}
    	this.converter = converter;
    }
    
    public Attribute setGetter(IGetter getter) {
        this.getter = getter;
        return this;
    }

    public Attribute setSetter(ISetter setter) {
        this.setter = setter;
        return this;
    }
    
    public boolean isRequired() {
    	return this.isRequired;
    }
    
    public Attribute setRequired(boolean isRequired) {
    	this.isRequired = isRequired;
    	return this;
    }
    
    public Attribute setDefault(String defaultValue) {
    	if (defaultValue == null) {
    		throw new NullPointerException("No value provided for default value");
    	}
    	this.defaultValue = defaultValue;
    	return this;
    }
    
    @Override
    public String toString() {
        return String.format("Attribute[name=%s]", this.attributeName);
    }
    
}
