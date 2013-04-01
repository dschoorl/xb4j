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

import info.rsdev.xb4j.model.bindings.action.IPhasedAction;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;


/**
 * This interface defines how to transform from Java instance to xml and visa versa, regardless whether the binding represents
 * a single element or a group of elements.
 * 
 * @author Dave Schoorl
 */
public interface IBinding {
    
    public UnmarshallResult toJava(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException;
    
    public void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException;
    
    /**
     * Determine if the binding will output anything to xmlStream, so that we can know if we have to output an empty mandatory 
     * container tag, or suppress an empty optional container tag. Or, ofcourse, output a non-empty element to the xml stream.
     * @param javaContext
     * @return
     */
    public boolean generatesOutput(JavaContext javaContext);
    
    /**
     * Bindings are organized in a hierarchy. Call setParent to build the hierarchy of bindings.
     * @param parent the parent {@link IBinding} that this binding is a child of.
     */
    public void setParent(IBinding parent);
    
    public IBinding getParent();
    
    public QName getElement();
    
    public IBinding addAttribute(IAttribute attribute, String fieldName);
    
    public IBinding addAttribute(IAttribute attribute, IGetter getter, ISetter setter);
    
    public Class<?> getJavaType();
    
    /**
     * Get the {@link JavaContext} that will be passed on to nested bindings. The new JavaContext is based on the currentContext,
     * meaning that any external context objects are passed on, and the context object is set with the value created by this
     * binding. If this binding does not create a new context object, then the value will be set to null.
     * @param currentContext the current JavaContext that was passed on to the 
     * @return a new {@link JavaContext} with the context object created by this binding or null when no contex object is created
     */
    public JavaContext newInstance(RecordAndPlaybackXMLStreamReader staxReader, JavaContext currentContext);
    
    public Object getProperty(JavaContext javaContext);
    
    public boolean setProperty(JavaContext javaContext, Object propertyValue);
    
    public IBinding setGetter(IGetter getter);
    
    public IBinding setSetter(ISetter setter);
    
    public IBinding addAction(IPhasedAction action);
    
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
    
    /**
     * Get the {@link ISemaphore} instance that is at the root of the binding tree that this binding belongs to. If the binding
     * does not belong to a binding tree yet, a {@link NullSafeSemaphore} instance should be returned, to support threatsafe
     * operations on thhis binding tree.
     * 
     * @return An {@link ISemaphore} instance that represent the root of the binding tree, or {@link NullSafeSemaphore} when the
     * 		root is not an instance of {@link ISemaphore} 
     */
    public ISemaphore getSemaphore();
    
    public IModelAware getModelAware();
    
    public void validateMutability();
    
}
