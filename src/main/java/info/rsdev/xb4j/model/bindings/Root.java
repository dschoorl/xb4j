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
import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * <p>This Binding is at the root of a binding hierarchy. It has a reference to the {@link BindingModel}, so
 * it can lookup {@link ComplexType complextype definitions}.</p>
 * 
 * TODO: set schema on the root type to use validation on stax reader/writer?
 * 
 * @author Dave Schoorl
 */
public class Root extends Element implements IModelAware, ISemaphore {
	
	private ReentrantLock lock = new ReentrantLock();
	
    private BindingModel model = null;
    
    /**
     * Flag that indicates whether this {@link Root} element can be changed or not. A root element is made immutable, the
     * first time it is used to marshall/unmarshall, so that it can be used in a threadsafe manner. When a {@link #copy(QName)} 
     * is made, the copy is mutable again.
     */
    private AtomicBoolean isImmutable = new AtomicBoolean(false);
    
	public Root(QName element, Class<?> javaType) {
		super(element, javaType);
    	super.setOptional(false);
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param original
	 * @param newElement
	 */
	protected Root(Root original, QName newElement) {
		//do not copy the BindingModel of original! - we want the flexibility to register the copy with another BindingModel
		super(original, new DefaultElementFetchStrategy(newElement));
	}
	
	public ComplexType getComplexType(String identifier, String namespaceUri) {
	    if (namespaceUri == null) { namespaceUri = XMLConstants.NULL_NS_URI; }
	    ComplexType complexType = this.model.getComplexType(identifier, namespaceUri);
        if (complexType == null) {
            throw new Xb4jException(String.format("ComplexTypeBinding with identifier=%s and namespace=%s is not" +
                    "registered in the BindingModel", identifier, namespaceUri));
        }

	    return complexType;
	}
	
    @Override
	public void setModel(BindingModel model) {
	    if (model == null) {
	        throw new NullPointerException("BindingModel cannot be null");
	    }
	    if ((this.model != null) && !this.model.equals(model)) {
	        throw new IllegalArgumentException("It is currently not supported that a RootBinding is added to multiple BindingModels");
	    }
	    
	    lock();
	    try {
    	    validateMutability();
    	    this.model = model;
	    } finally {
	    	unlock();
	    }
	}
	
    @Override
	public BindingModel getModel() {
	    return this.model;
	}
	
	@Override
	public IBinding setOptional(boolean isOptional) {
		if (isOptional == true) {
			throw new Xb4jException("A Root binding cannot be made optional");
		}
		return this;
	}
	
    @Override
    public String toString() {
        String fqClassName = getClass().getName();
        int dotIndex = Math.max(0, fqClassName.lastIndexOf('.') + 1);
        return String.format("%s[element=%s, javaType=%s]", fqClassName.substring(dotIndex), getElement(), getJavaType().getName());
    }
    
    /**
     * Copy this {@link Root}, so it can be registered for another element (E.g. another namespace), but with the same
     * structure). The BindingModel of this RootBinding (if any) will not be copied, so you can register this copy also with 
     * another BindingModel.
     * 
     * @param newElement
     * @return A copy of this {@link Root}. It still needs to be registered with the {@link BindingModel}
     */
    public Root copy(QName newElement) {
    	return new Root(this, newElement);
    }
    
    public boolean isImmutable() {
    	lock();
    	try {
    		return this.isImmutable.get();
    	} finally {
    		unlock();
    	}
    }
    
    /**
     * Resolve all references to {@link ComplexType}s and mark this binding immutable. 
     * 
     * @see info.rsdev.xb4j.model.bindings.IModelAware#makeImmutable()
     */
    public void makeImmutable() {
    	lock();
    	try {
    		if (!isImmutable.get()) {
    			resolveReferences();
    			isImmutable.set(true);
    		}
    	} finally {
    		unlock();
    	}
    }
    
    public void lock() {
    	this.lock.lock();
    }
    
    public void unlock() {
    	this.lock.unlock();
    }
    
}
