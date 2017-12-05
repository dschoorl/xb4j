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
import info.rsdev.xb4j.model.converter.IValueConverter;
import info.rsdev.xb4j.model.converter.NOPConverter;
import info.rsdev.xb4j.model.java.JavaContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * An {@link IAttribute} implementation with a static (default) value which overrides the values provided by the xml stream or the
 * {@link JavaContext}
 *
 * @author Dave Schoorl
 */
public class StaticAttribute extends AbstractAttribute {

    private IValueConverter converter = null;

    private String staticValue = null;

    /**
     * Create a new {@link StaticAttribute} with the given attribute name and the provided static value. The static value is offered
     * to the Java Object Tree without conversion.
     *
     * @param attributeName
     * @param staticValue
     */
    public StaticAttribute(QName attributeName, String staticValue) {
        this(attributeName, staticValue, NOPConverter.INSTANCE);
    }

    /**
     * Create a new {@link StaticAttribute} with the given attribute name and the provided static value. The static value is offered
     * to the Java Object Tree after converting it, using the provided {@link IValueConverter converter}.
     *
     * @param attributeName
     * @param staticValue
     * @param converter
     */
    public StaticAttribute(QName attributeName, String staticValue, IValueConverter converter) {
        super(attributeName);
        this.staticValue = staticValue;
        setConverter(converter);
    }

    private StaticAttribute(StaticAttribute original, IBinding newParent) {
        super(original, newParent);
        this.converter = original.converter;
        this.staticValue = original.staticValue;
    }

    @Override
    public void toJava(String valueAsText, JavaContext javaContext) {
        //Ignore provided value from the xml stream; always use the static value
        Object value = this.converter.toObject(javaContext, this.staticValue);
        setProperty(javaContext, value);
    }

    /**
     * Get the static value defined for this attribute, ignore the {@link JavaContext}
     *
     * @param javaContext
     * @return the value to write to xml
     */
    @Override
    public String getValue(JavaContext javaContext) {
        return this.staticValue;
    }

    /**
     * 
     * @param javaContext
     * @return 
     */
    @Override
    public OutputState generatesOutput(JavaContext javaContext) {
        if (isRequired()) {
            return OutputState.HAS_OUTPUT;
        }
        return OutputState.COLLABORATE;
    }

    private void setConverter(IValueConverter converter) {
        if (converter == null) {
            throw new NullPointerException("IValueConverter cannot be null");
        }
        //only called from constructor: no need to validate mutability
        this.converter = converter;
    }

    /**
     * Override the static value from the constructor with a new static value. This can only be done when the attribute has not yet 
     * been used in a marshall / unmarshall operation
     * @param defaultValue 
     * @return 
     */
    @Override
    public StaticAttribute setDefault(String defaultValue) {
        throw new Xb4jException("StaticAttribute supports default values as static values through the constructor");
    }

    @Override
    public String getDefaultValue() {
        return this.staticValue;
    }

    @Override
    public StaticAttribute copy(IBinding newParent) {
        return new StaticAttribute(this, newParent);
    }
}
