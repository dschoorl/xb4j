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

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;

/**
 * 
 * @author Dave Schoorl
 */
public abstract class AbstractSingleBinding extends AbstractBindingBase implements ISingleBinding {
	
	private IBindingBase childBinding = null;
	
    public <T extends IBindingBase> T setChild(T childBinding, IGetter getter, ISetter setter) {
    	setChild(childBinding);
    	childBinding.setGetter(getter);
        childBinding.setSetter(setter);
        
        return childBinding;
    }
    
    public AbstractSingleBinding() {}
    
    /**
     * Copy constructor
     * @param original
     */
    protected AbstractSingleBinding(AbstractSingleBinding original) {
        super(original);
        this.childBinding = original.childBinding;
    }
    
    /**
     * Convenience method, which adds a child binding, and navigating the object tree from parent to child is done through
     * the field with the given fieldname.
     * 
     * @param childBinding
     * @param fieldName
     * @return the childBinding
     */
    public <T extends IBindingBase> T setChild(T childBinding, String fieldName) {
        if (fieldName == null) {
        	throw new NullPointerException("Fieldname cannot be null");
        }
        setChild(childBinding);
        FieldAccessProvider provider = new FieldAccessProvider(fieldName);
        childBinding.setGetter(provider);
        childBinding.setSetter(provider);
        
        return childBinding;
    }

    public <T extends IBindingBase> T setChild(T childBinding) {
    	if (childBinding == null) {
    		throw new NullPointerException("Child IBinding must not be null when you explicitly set it");
    	}
        if ((this.childBinding != null) && !this.childBinding.equals(childBinding)) {
            throw new Xb4jException(String.format("Cannot replace existing child %s with ew one: %s", this.childBinding, childBinding));
        }
        this.childBinding = childBinding;
        childBinding.setParent(this);   //maintain bidirectional relationship
        return childBinding;
    }
    
    protected IBindingBase getChildBinding() {
    	return this.childBinding;
    }
	
}
