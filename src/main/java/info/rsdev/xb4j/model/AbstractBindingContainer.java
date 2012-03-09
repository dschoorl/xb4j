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

import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;


public abstract class AbstractBindingContainer extends AbstractBindingBase implements IBindingContainer {

    private ArrayList<IBindingBase> children = new ArrayList<IBindingBase>();
    
    protected AbstractBindingContainer(IElementFetchStrategy elementFetcher, ICreator objectCreator) {
    	super(elementFetcher, objectCreator);
    }
    
    /**
     * <p>When unmarshalling, the child binding will know how to get from the current element to the next one. Which
     * element to expect next. Any, choice, sequence</p> 
     * @param childBinding
     * @return the childBinding
     */
    public <T extends IBindingBase> T add(T childBinding, IGetter getter, ISetter setter) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        childBinding.setGetter(getter);
        childBinding.setSetter(setter);
        
        return add(childBinding);
    }
    
    /**
     * Convenience method, which adds a child binding, and navigating the object tree from parent to child is done through
     * the field with the given fieldname.
     * 
     * @param childBinding
     * @param fieldName
     * @return the childBinding
     */
    public <T extends IBindingBase> T add(T childBinding, String fieldName) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        if (fieldName == null) {
        	throw new NullPointerException("Fieldname cannot be null");
        }
        FieldAccessProvider provider = new FieldAccessProvider(fieldName);
        childBinding.setGetter(provider);
        childBinding.setSetter(provider);
        
        return add(childBinding);
    }

    /**
     * Add a {@link IBindingBase} to a binding container. A bidirectional relationship will be established between the
     * container and the child.
     * 
     * @param childBinding the binding to add to this group
     * @return the childBinding
     */
    public <T extends IBindingBase> T add(T childBinding) {
        this.children.add(childBinding);
        childBinding.setParent(this);   //maintain bidirectional relationship
        return childBinding;
    }
    
    public Collection<IBindingBase> getChildren() {
        return Collections.unmodifiableList(this.children);
    }
    
    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
    	Object newJavaContext = null;
    	QName expectedElement = getElement();
    	if ((expectedElement != null) && !staxReader.isAtElementStart(expectedElement)) {
    		return null;
    	}
    	
    	newJavaContext = newInstance();
        for (IBindingBase child: getChildren()) {
            Object result = child.toJava(staxReader, select(javaContext, newJavaContext));
            setProperty(newJavaContext, result);
        }
        
        return newJavaContext;
    }
    
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        
        //mixed content is not yet supported -- there are either child elements or there is content
        Collection<IBindingBase> children = getChildren();
        boolean isEmptyElement = children.isEmpty();
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
        }
        
        for (IBindingBase child: children) {
            child.toXml(staxWriter, getProperty(javaContext));
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
    }
    
}
