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
	
	public AbstractAttribute(QName attributeName) {
    	setAttributeName(attributeName);
	}

	@Override
	public QName getAttributeName() {
		return this.attributeName;
	}

	protected void setAttributeName(QName attributeName) {
		if (attributeName == null) {
			throw new NullPointerException("Attribute QName cannot be null");
		}
		this.attributeName = attributeName;
	}

	@Override
	public Object getProperty(Object contextInstance) {
		if (contextInstance == null) {
			return getDefaultValue();
		}
	    return this.getter.get(contextInstance);
	}

	@Override
	public boolean setProperty(Object contextInstance, Object propertyValue) {
	    return this.setter.set(contextInstance, propertyValue);
	}

	@Override
	public IAttribute setGetter(IGetter getter) {
	    this.getter = getter;
	    return this;
	}

	@Override
	public IAttribute setSetter(ISetter setter) {
	    this.setter = setter;
	    return this;
	}

	@Override
	public boolean isRequired() {
		return this.isRequired;
	}

	@Override
	public IAttribute setRequired(boolean isRequired) {
		this.isRequired = isRequired;
		return this;
	}

	@Override
	public String toString() {
	    return String.format("Attribute[name=%s]", this.attributeName);
	}
	
}