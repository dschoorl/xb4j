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

import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Dave Schoorl
 */
public interface IAttribute {

    QName getAttributeName();

    void toJava(String valueAsText, JavaContext javaContext) throws XMLStreamException;

    void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext, QName elementName) throws XMLStreamException;

    /**
     * Get the value of this attribute from the {@link JavaContext}, fallback on a possible defaultValue defined by this attribute.
     *
     * @param javaContext
     * @return the value
     */
    String getValue(JavaContext javaContext);

    JavaContext getProperty(JavaContext javaContext);

    boolean setProperty(JavaContext javaContext, Object propertyValue);

    <T extends IAttribute> T setGetter(IGetter getter);

    <T extends IAttribute> T setSetter(ISetter setter);

    boolean isRequired();

    <T extends IAttribute> T setRequired(boolean isRequired);

    <T extends IAttribute> T setDefault(String defaultValue);

    String getDefaultValue();

    <T extends IAttribute> T copy(IBinding newParent);
    
    /**
     * Determine if the binding will output anything to xmlStream.
     *
     * @param javaContext
     * @return
     */
    OutputState generatesOutput(JavaContext javaContext);
}
