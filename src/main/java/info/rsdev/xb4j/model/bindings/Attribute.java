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
import info.rsdev.xb4j.model.java.JavaContext;
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

    /**
     * Copy constructor for an {@link Attribute}
     *
     * @param original
     * @param newParent
     */
    private Attribute(Attribute original, IBinding newParent) {
        super(original, newParent);
        this.converter = original.converter;
        this.defaultValue = original.defaultValue;
    }

    @Override
    public void toJava(String valueAsText, JavaContext javaContext) throws XMLStreamException {
        if ((valueAsText == null) && (this.defaultValue != null)) {
            valueAsText = this.defaultValue;
        }
        Object value = this.converter.toObject(javaContext, valueAsText);
        setProperty(javaContext, value);
    }

    @Override
    public void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext, QName elementName) throws XMLStreamException {
        QName attributeName = getAttributeName();
        String value = getValue(javaContext);
        if (isRequired() || (value != null)) {
            staxWriter.writeAttribute(elementName, attributeName, value);
        }
    }

    /**
     * Get the value of this attribute from the {@link JavaContext}, fallback on a possible defaultValue defined by this attribute.
     *
     * @param javaContext
     * @return the value to write to xml
     */
    @Override
    public String getValue(JavaContext javaContext) {
        String value = this.converter.toText(javaContext, getProperty(javaContext).getContextObject());
        if ((value == null) && (getDefaultValue() != null)) {
            value = getDefaultValue();
        }
        return value;
    }

    private void setConverter(IValueConverter converter) {
        if (converter == null) {
            throw new NullPointerException("IValueConverter cannot be null");
        }
        //only called from constructor, no need to validate mutability
        this.converter = converter;
    }

    @Override
    public Attribute setDefault(String defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("No value provided for default value");
        }
        IBinding parent = attachedBinding;
        if (parent != null) {
            parent.getSemaphore().lock();
        }
        try {
            if (parent != null) {
                parent.validateMutability();
            }
            this.defaultValue = defaultValue;
        } finally {
            if (parent != null) {
                parent.getSemaphore().unlock();
            }
        }
        return this;
    }

    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public Attribute copy(IBinding newParent) {
        return new Attribute(this, newParent);
    }

}
