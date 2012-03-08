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
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;

import javax.xml.namespace.QName;

/**
 *
 * @author Dave Schoorl
 */
public abstract class AbstractBindingBase implements IBindingBase {
    
	private IElementFetchStrategy elementFetcher = null;
	
	private ICreator objectCreator = null;
	
    private IGetter getter = null;
    
    private ISetter setter = null;
    
    private IBindingBase parent = null;
    
    private boolean isOptional = true;
    
    public AbstractBindingBase() {
    	this.getter = NoGetter.INSTANCE;
    	this.setter = NoSetter.INSTANCE;
    }
    
    /**
     * Copy constructor that copies the properties of the original binding in a 
     * @param original
     * @param newParent
     */
    protected AbstractBindingBase(AbstractBindingBase original) {
        this.elementFetcher = original.elementFetcher;
        this.objectCreator = original.objectCreator;
        this.getter = original.getter;
        this.setter = original.setter;
        this.parent = null;    //clear parent, so that copy can be used in another binding hierarchy
    }
    
    public QName getElement() {
    	if (elementFetcher != null) {
    		return elementFetcher.getElement();
    	}
        return null;
    }
    
    public Class<?> getJavaType() {
        if (objectCreator != null) {
            return objectCreator.getJavaType();
        }
        return null;
    }
    
    public Object newInstance() {
        if (objectCreator != null) {
            return objectCreator.newInstance();
        }
        return null;
    }
    
    protected Object select(Object javaContext, Object newJavaContext) {
        if (newJavaContext != null) {
            return newJavaContext;
        }
        return javaContext;	//TODO: must we detect when both are null? That's worth an Exception, right?
    }
    
    protected void setElementFetchStrategy(IElementFetchStrategy elementFetcher) {
    	if (elementFetcher == null) {
    		throw new NullPointerException("IElementFetchStrategy cannot be null");
    	}
    	this.elementFetcher = elementFetcher;
    }
    
    protected IElementFetchStrategy getElementFetchStrategy() {
        return this.elementFetcher;
    }
    
    protected void setObjectCreator(ICreator objectCreator) {
        this.objectCreator = objectCreator;
    }
    
    public IBindingBase setGetter(IGetter getter) {
        this.getter = getter;
        return this;
    }

    public IBindingBase setSetter(ISetter setter) {
        this.setter = setter;
        return this;
    }
    
    public void setParent(IBindingBase parent) {
    	if (parent == null) {
    		throw new NullPointerException("Parent IBinding cannot be null");
    	}
    	if ((this.parent != null) && !this.parent.equals(parent)) {
    	    throw new IllegalArgumentException(String.format("This binding '%s' is already is part of a binding tree.", this));
    	}
    	this.parent = parent;
    }
    
    public IBindingBase getParent() {
    	return this.parent;
    }
    
    protected IModelAware getModelAware() {
        IBindingBase modelAwareBinding = this;
        while (modelAwareBinding.getParent() != null) {
        	modelAwareBinding = modelAwareBinding.getParent();
        }
        if (!(modelAwareBinding instanceof IModelAware)) {
            throw new Xb4jException(String.format("Expected top level binding to implement IModelAware, but found %s", 
                    modelAwareBinding.getClass().getName()));
        }
        return (IModelAware)modelAwareBinding;
    }

    public boolean setProperty(Object contextInstance, Object propertyValue) {
        return this.setter.set(contextInstance, propertyValue);
    }
    
    public Object getProperty(Object contextInstance) {
        return this.getter.get(contextInstance);
    }
    
    public boolean isExpected(QName element) {
    	if (element == null) {
    		throw new NullPointerException("QName cannot be null");
    	}
        return element.equals(getElement());
    }
    
    public boolean isOptional() {
        return this.isOptional;
    }
    
    public IBindingBase setOptional(boolean isOptional) {
        this.isOptional = isOptional;
        return this;
    }
    
    @Override
    public String toString() {
        String fqClassName = getClass().getName();
        int dotIndex = Math.max(0, fqClassName.lastIndexOf('.') + 1);
        String typename = (getJavaType()==null?null:getJavaType().getName());
        return String.format("%s[element=%s, javaType=%s]", fqClassName.substring(dotIndex), getElement(), typename);
    }
    
}
