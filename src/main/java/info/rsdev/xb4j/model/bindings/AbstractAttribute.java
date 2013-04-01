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
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;

import javax.xml.namespace.QName;

/**
 * 
 * @author Dave Schoorl
 */
public abstract class AbstractAttribute implements IAttribute {
	
	private IGetter getter = null;
	
	private ISetter setter = null;
	
	private QName attributeName = null;
	
	private boolean isRequired = false;
	
	protected IBinding attachedBinding = null;
	
	public AbstractAttribute(QName attributeName) {
    	setAttributeName(attributeName);
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param original
	 * @param newParent
	 */
	protected AbstractAttribute(AbstractAttribute original, IBinding newParent) {
    	if (newParent == null) {
    		throw new NullPointerException("New IBinding parent cannot be null");
    	}
		this.getter = original.getter;
		this.setter = original.setter;
		this.attributeName = original.attributeName;
		this.isRequired = original.isRequired;
		this.attachedBinding = newParent;
	}
	
	void attachToBinding(IBinding parent) {
		if (parent == null) {
			throw new NullPointerException("AbstractBinding cannot be null");
		}
    	if ((this.attachedBinding != null) && !this.attachedBinding.equals(parent)) {
    	    throw new IllegalArgumentException(String.format("This attribute '%s' is already part of a binding tree.", this));
    	}
		this.attachedBinding = parent;
	}

	@Override
	public QName getAttributeName() {
		return this.attributeName;
	}

	protected void setAttributeName(QName attributeName) {
		if (attributeName == null) {
			throw new NullPointerException("Attribute QName cannot be null");
		}
		IBinding parent = attachedBinding;
		if (parent != null) {parent.getSemaphore().lock(); }
		try {
			if (parent != null) {parent.validateMutability(); }
			this.attributeName = attributeName;
		} finally {
			if (parent != null) {parent.getSemaphore().unlock(); }
		}
	}

	@Override
	public JavaContext getProperty(JavaContext javaContext) {
		if (javaContext.getContextObject() == null) {
			return javaContext.newContext(getDefaultValue());
		}
	    return this.getter.get(javaContext);
	}

	@Override
	public boolean setProperty(JavaContext javaContext, Object propertyValue) {
	    return this.setter.set(javaContext, propertyValue);
	}

	@Override
	public IAttribute setGetter(IGetter getter) {
		if (getter == null) {
			getter = NoGetter.INSTANCE;
		}
		IBinding parent = attachedBinding;
		if (parent != null) {parent.getSemaphore().lock(); }
		try {
			if (parent != null) {parent.validateMutability(); }
		    this.getter = getter;
		} finally {
			if (parent != null) {parent.getSemaphore().unlock(); }
		}
	    return this;
	}

	@Override
	public IAttribute setSetter(ISetter setter) {
		if (setter == null) {
			setter = NoSetter.INSTANCE;
		}
		IBinding parent = attachedBinding;
		if (parent != null) {parent.getSemaphore().lock(); }
		try {
			if (parent != null) {parent.validateMutability(); }
		    this.setter = setter;
		} finally {
			if (parent != null) {parent.getSemaphore().unlock(); }
		}
	    return this;
	}

	@Override
	public boolean isRequired() {
		return this.isRequired;
	}

	@Override
	public IAttribute setRequired(boolean isRequired) {
		IBinding parent = attachedBinding;
		if (parent != null) {parent.getSemaphore().lock(); }
		try {
			if (parent != null) {parent.validateMutability(); }
			this.isRequired = isRequired;
		} finally {
			if (parent != null) {parent.getSemaphore().unlock(); }
		}
		return this;
	}

	@Override
	public String toString() {
	    return String.format("Attribute[name=%s]", this.attributeName);
	}
	
}