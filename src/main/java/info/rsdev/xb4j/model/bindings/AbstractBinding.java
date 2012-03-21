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
public abstract class AbstractBinding implements IBinding {
    
	private IElementFetchStrategy elementFetcher = null;
	
	private ICreator objectCreator = null;
	
    private IGetter getter = null;
    
    private ISetter setter = null;
    
    private IBinding parent = null;
    
    private boolean isOptional = false; //by default, everything is mandatory, unless explicitly made optional
    
    protected AbstractBinding(IElementFetchStrategy elementFetcher, ICreator objectCreator) {
    	setElementFetchStrategy(elementFetcher);
    	this.objectCreator = objectCreator;	//null is allowed
    	this.getter = NoGetter.INSTANCE;
    	this.setter = NoSetter.INSTANCE;
    }
    
    /**
     * Copy constructor that copies the properties of the original binding in a 
     * @param original
     * @param newParent
     */
    protected AbstractBinding(AbstractBinding original) {
    	copyFields(original, original.elementFetcher);
    }
    
    /**
     * Copy constructor that copies the properties of the original binding in a 
     * @param original
     * @param newParent
     */
    protected AbstractBinding(AbstractBinding original, IElementFetchStrategy elementFetcher) {
    	copyFields(original, elementFetcher);
    }
    
    private void copyFields(AbstractBinding original, IElementFetchStrategy elementFetcher) {
        this.elementFetcher = elementFetcher;
        this.objectCreator = original.objectCreator;
        this.getter = original.getter;
        this.setter = original.setter;
        this.isOptional = original.isOptional;
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
    
    /**
     * Select a non-null context (if possible), where the newJavaContext takes precedence over the javaContext, when both of them
     * are not null.
     * 
     * @param javaContext
     * @param newJavaContext
     * @return either the javaContext or the newJavaContext
     */
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
    	if ((this.elementFetcher != null) && !this.elementFetcher.equals(elementFetcher)) {
    		throw new Xb4jException("Once set, an IElementFetchStrategy cannot be changed: ".concat(this.toString()));
    	}
    	this.elementFetcher = elementFetcher;
    }
    
    protected IElementFetchStrategy getElementFetchStrategy() {
        return this.elementFetcher;
    }
    
    protected void setObjectCreator(ICreator objectCreator) {
    	if (objectCreator == null) {
    		throw new NullPointerException("ICreator cannot be null");
    	}
    	if ((this.objectCreator != null) && !this.objectCreator.equals(objectCreator)) {
    		throw new Xb4jException("Once set, an ICreator cannot be changed: ".concat(this.toString()));
    	}
        this.objectCreator = objectCreator;
    }
    
    public IBinding setGetter(IGetter getter) {
        this.getter = getter;
        return this;
    }

    public IBinding setSetter(ISetter setter) {
        this.setter = setter;
        return this;
    }
    
    public boolean hasSetter() {
    	return (this.setter != null) && !(this.setter instanceof NoSetter);
    }
    
    public void setParent(IBinding parent) {
    	if (parent == null) {
    		throw new NullPointerException("Parent IBinding cannot be null");
    	}
    	if ((this.parent != null) && !this.parent.equals(parent)) {
    	    throw new IllegalArgumentException(String.format("This binding '%s' is already is part of a binding tree.", this));
    	}
    	this.parent = parent;
    }
    
    public IBinding getParent() {
    	return this.parent;
    }
    
    protected IModelAware getModelAware() {
        IBinding modelAwareBinding = this;
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
    
    public IBinding setOptional(boolean isOptional) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.elementFetcher == null) ? 0 : this.elementFetcher.hashCode());
		result = prime * result + ((this.getter == null) ? 0 : this.getter.hashCode());
		result = prime * result + (this.isOptional ? 1231 : 1237);
		result = prime * result + ((this.objectCreator == null) ? 0 : this.objectCreator.hashCode());
		result = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
		result = prime * result + ((this.setter == null) ? 0 : this.setter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AbstractBinding other = (AbstractBinding) obj;
		if (this.elementFetcher == null) {
			if (other.elementFetcher != null) return false;
		} else if (!this.elementFetcher.equals(other.elementFetcher)) return false;
		if (this.getter == null) {
			if (other.getter != null) return false;
		} else if (!this.getter.equals(other.getter)) return false;
		if (this.isOptional != other.isOptional) return false;
		if (this.objectCreator == null) {
			if (other.objectCreator != null) return false;
		} else if (!this.objectCreator.equals(other.objectCreator)) return false;
		if (this.parent == null) {
			if (other.parent != null) return false;
		} else if (!this.parent.equals(other.parent)) return false;
		if (this.setter == null) {
			if (other.setter != null) return false;
		} else if (!this.setter.equals(other.setter)) return false;
		return true;
	}
    
}
