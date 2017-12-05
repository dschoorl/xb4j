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

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Implementations of this interface represent an xml attribute. In contrast to {@link IBinding xml elements}, implementations
 * should not throw an exception when it encounters attributes that are not registered in the {@link BindingModel}.
 *
 * @author Dave Schoorl
 */
public interface IAttribute {

    /**
     * Get the fully qualified name of the xml attribute
     *
     * @return the {@link QName} of the attribute
     */
    QName getAttributeName();

    /**
     * Apply the attribute value from the xml to the Java representation present in the java context
     *
     * @param valueAsText the attribute value read
     * @param javaContext the java context containing the current java context object (the java instance currently being build from
     *      the xml representation.
     */
    void toJava(String valueAsText, JavaContext javaContext);

    /**
     * Read the attribute value from the java context object and write it out to the {@link SimplifiedXMLStreamWriter xml stream}
     * 
     * @param staxWriter {@link SimplifiedXMLStreamWriter} that wraps a stax writer
     * @param javaContext the context containing the java object to marshall
     * @param elementName The {@link QName} of the xml element that this attribute is a child of
     * @throws XMLStreamException propagate any xml stream exception that may occur
     */
    void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext, QName elementName) throws XMLStreamException;

    /**
     * Get the value of this attribute from the {@link JavaContext}, fallback on a possible defaultValue defined by this attribute.
     *
     * @param javaContext the java context to get the value from
     * @return the value for this attribute
     */
    String getValue(JavaContext javaContext);

    /**
     * Navigate the java context object to obtain the java context for this attribute. How to navigate the context object is 
     * known to the implementation (see {@link #setGetter(info.rsdev.xb4j.model.java.accessor.IGetter)}
     * @param javaContext the parent java context
     * @return the java context applicable for this attribute
     */
    JavaContext getProperty(JavaContext javaContext);

    /**
     * Try to set the read attribute value on the java context object. This is only possible, when the default {@link NoSetter} has 
     * been replaced with a proper {@link ISetter} implementation (see {@link #setSetter(info.rsdev.xb4j.model.java.accessor.ISetter)}.
     * 
     * @param javaContext the java context holding the java context object
     * @param propertyValue the value to set
     * @return true if the value was set, false otherwise
     */
    boolean setProperty(JavaContext javaContext, Object propertyValue);

    /**
     * Define how this binding navigates through the java context object to get the java value to marshall.
     * @param <T> the type of IAttribute to return. Usually inferred by the compiler.
     * @param getter the {@link IGetter} implementation that knows how to navigate the java context to access the java value for 
     *      marshalling
     * @return this {@link IAttribute} instance for fluent programming
     */
    <T extends IAttribute> T setGetter(IGetter getter);

    /**
     * Define how this binding navigates through the java context object to mutate the java context object and set the value read 
     * from the xml during unmarshalling.
     * @param <T> the type of IAttribute to return. Usually inferred by the compiler.
     * @param setter the {@link  ISetter} implementation that allows the binding to set the value in the correct place in the java 
     *    context object
     * @return this {@link IAttribute} instance for fluent programming
     */
    <T extends IAttribute> T setSetter(ISetter setter);

    /**
     * Query if this attribute is required or not
     * 
     * @return true if this attribute is required, false otherwise
     */
    boolean isRequired();

    /**
     * Mark this attribute as required (true) or optional (false).    
     * @param <T> the type of IAttribute to return. Usually inferred by the compiler.
     * @param isRequired the boolean value to set
     * @return this {@link IAttribute} instance for fluent programming
     */
    <T extends IAttribute> T setRequired(boolean isRequired);

    /**
     * The default value for this attribute
     * @param <T> the type of IAttribute to return. Usually inferred by the compiler.
     * @param defaultValue the default value
     * @return this {@link IAttribute} instance for fluent programming
     */
    <T extends IAttribute> T setDefault(String defaultValue);

    /**
     * Query the default value for this attribute.
     * @return the default value of this attribute, when set. Null otherwise.
     */
    String getDefaultValue();

    /**
     * Copy this attribute and make it a child of the given newParent binding.
     * @param <T> the type of IAttribute to return. Usually inferred by the compiler.
     * @param newParent the parent to set for the copied attribute
     * @return this {@link IAttribute} instance for fluent programming
     */
    <T extends IAttribute> T copy(IBinding newParent);

    /**
     * Determine if the binding will output anything to xmlStream when marshalling a given javaContext.
     *
     * @param javaContext the javaContext that would be the basis for marshalling with this binding
     * @return {@link OutputState} value
     */
    OutputState generatesOutput(JavaContext javaContext);
}
