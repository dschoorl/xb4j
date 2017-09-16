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

    public QName getAttributeName();

    public void toJava(String valueAsText, JavaContext javaContext) throws XMLStreamException;

    public void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext, QName elementName) throws XMLStreamException;

    /**
     * Get the value of this attribute from the {@link JavaContext}, fallback on
     * a possible defaultValue defined by this attribute.
     *
     * @param javaContext
     * @return the value
     */
    public String getValue(JavaContext javaContext);

    public Object getProperty(JavaContext javaContext);

    public boolean setProperty(JavaContext javaContext, Object propertyValue);

    public IAttribute setGetter(IGetter getter);

    public IAttribute setSetter(ISetter setter);

    public boolean isRequired();

    public IAttribute setRequired(boolean isRequired);

    public IAttribute setDefault(String defaultValue);

    public String getDefaultValue();

    public IAttribute copy(IBinding newParent);

}
