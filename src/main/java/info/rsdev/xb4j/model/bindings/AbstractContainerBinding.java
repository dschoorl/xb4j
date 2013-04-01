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

import info.rsdev.xb4j.model.java.accessor.FieldAccessor;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public abstract class AbstractContainerBinding extends AbstractBinding implements IContainerBinding {

    private ArrayList<IBinding> children = new ArrayList<IBinding>();
    
    protected AbstractContainerBinding(IElementFetchStrategy elementFetcher, ICreator objectCreator) {
    	super(elementFetcher, objectCreator);
    }
    
    /**
     * <p>When unmarshalling, the child binding will know how to get from the current element to the next one. Which
     * element to expect next. Any, choice, sequence</p> 
     * @param childBinding
     * @return the childBinding
     */
    public <T extends IBinding> T add(T childBinding, IGetter getter, ISetter setter) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        
		getSemaphore().lock();
		try {
			validateMutability();
			add(childBinding);
	        childBinding.setGetter(getter);
	        childBinding.setSetter(setter);
		} finally {
			getSemaphore().unlock();
		}
		return childBinding;
    }
    
    /**
     * Convenience method, which adds a child binding, and navigating the object tree from parent to child is done through
     * the field with the given fieldname.
     * 
     * @param childBinding
     * @param fieldName
     * @return the childBinding
     */
    public <T extends IBinding> T add(T childBinding, String fieldName) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        if (fieldName == null) {
        	throw new NullPointerException("Fieldname cannot be null");
        }
        
		getSemaphore().lock();
		try {
			validateMutability();
			add(childBinding);
	        FieldAccessor provider = new FieldAccessor(fieldName);
	        childBinding.setGetter(provider);
	        childBinding.setSetter(provider);
		} finally {
			getSemaphore().unlock();
		}
		return childBinding;
    }

    /**
     * Add a {@link IBinding} to a binding container. A bidirectional relationship will be established between the
     * container and the child.
     * 
     * @param childBinding the binding to add to this group
     * @return the childBinding
     */
    public <T extends IBinding> T add(T childBinding) {
		getSemaphore().lock();
		try {
			validateMutability();
	        this.children.add(childBinding);
	        childBinding.setParent(this);   //maintain bidirectional relationship
		} finally {
			getSemaphore().unlock();
		}
        return childBinding;
    }
    
    /**
     * Get the children for this container or an empty list when there are none. The Collection of children cannot be changed.
     * @return an unmodifiable collection of child {@link IBinding bindings}
     */
    public Collection<IBinding> getChildren() {
        return Collections.unmodifiableList(this.children);
    }
    
}
