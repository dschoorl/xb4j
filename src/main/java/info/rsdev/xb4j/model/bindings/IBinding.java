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

import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.action.IAction;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;


/**
 * This interface defines how to transform from Java instance to xml and visa versa, regardless whether the binding represents
 * a single element or a group of elements.
 * 
 * @author Dave Schoorl
 */
public interface IBinding {
    
    public UnmarshallResult toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException;
    
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException;
    
    /**
     * Determine if the binding will output anything to xmlStream, so that we can no if we have to output an empty mandatory 
     * container tag, or suppress an empty optional container tag. Or, ofcourse, output a non-empty element to the xml stream.
     * @param javaContext
     * @return
     */
    public boolean generatesOutput(Object javaContext);
    
    /**
     * Bindings are organized in a hierarchy. Call setParent to build the hierarchy of bindings.
     * @param parent the parent {@link IBinding} that this binding is a child of.
     */
    public void setParent(IBinding parent);
    
    public IBinding getParent();
    
    public QName getElement();
    
    public IBinding addAttribute(Attribute attribute, String fieldName);
    
    public Class<?> getJavaType();
    
    public Object newInstance();
    
    public Object getProperty(Object contextInstance);
    
    public boolean setProperty(Object contextInstance, Object propertyValue);
    
    public IBinding setGetter(IGetter getter);
    
    public IBinding setSetter(ISetter setter);
    
    public IBinding setActionAfterUnmarshalling(IAction action);
    
    /**
     * Whether a binding is optional, is only relevant when it has an xml representation. Checking for presence of an element 
     * in this binding definition must be done by the developer where applicable, prior to calling this method. This method 
     * simply returns the value of the isOptional indicator.
     * @return true if the element (when applicable) can appear in the xml, false if it must appear in the xml
     */
    public boolean isOptional();
    
    public IBinding setOptional(boolean isOptional);
    
    public int hashCode();
    
    public boolean equals(Object obj);
    
    public String getPath();
    
}
