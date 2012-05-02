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
import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

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
    
    private List<Attribute> attributes = null;
    
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
        if (original.attributes != null) {
        	this.attributes = new LinkedList<Attribute>(original.attributes);
        }
        this.parent = null;    //clear parent, so that copy can be used in another binding hierarchy
    }
    
    @Override
    public IBinding addAttribute(Attribute attribute, String fieldName) {
    	if (attribute == null) {
    		throw new NullPointerException(String.format("Attribute cannot be null (binding=%s)", this));
    	}
    	if (getElement() == null) {
    		throw new Xb4jException(String.format("No element defined to bind attributes to (binding=%s)", this));
    	}
    	Collection<Attribute> attributes = getAttributes();
    	if (attributes.contains(attribute)) {
    		throw new Xb4jException(String.format("Attribute %s already defined (binding=%s)", attribute, this));
    	}
        if (fieldName == null) {
        	throw new NullPointerException("Fieldname cannot be null");
        }
        FieldAccessProvider provider = new FieldAccessProvider(fieldName);
        attribute.setGetter(provider);
        attribute.setSetter(provider);
    	attributes.add(attribute);
    	return this;
    }
    
    public Collection<Attribute> getAttributes() {
    	if ((this.attributes == null) && (getElement() != null)) {
    		//only create new collection, when there is an element to bind them to
    		this.attributes = new LinkedList<Attribute>();
    	}
    	return this.attributes;
    }
    
    public boolean hasAttributes() {
    	return (this.attributes != null) && !this.attributes.isEmpty();
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
    	    throw new IllegalArgumentException(String.format("This binding '%s' is already part of a binding tree.", this));
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
    	if (contextInstance == null) {
    		return null;
    	}
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
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
    	//This class provides a default implementation - template style
    	elementToXml(staxWriter, javaContext);
    	attributesToXml(staxWriter, javaContext);
    }
    
    public abstract void elementToXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException;
    
    public void attributesToXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
    	if ((attributes != null) && !attributes.isEmpty()) {
    		for (Attribute attribute: this.attributes) {
    			attribute.toXml(staxWriter, javaContext, getElement());
    		}
    	}
    }
    
    public void attributesToJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
    	Collection<Attribute> expectedAttributes = getAttributes();
    	if ((expectedAttributes != null) && !expectedAttributes.isEmpty()) {
    		Map<QName, String> attributes = staxReader.getAttributes();
    		if (attributes != null) {
    			attributes = new HashMap<QName, String>(attributes);	//copy attributes that were encountered in xml
    			for (Attribute attribute: expectedAttributes) {
    				if (!attributes.containsKey(attribute.getAttributeName()) && attribute.isRequired()) {
    					throw new Xb4jException(String.format("%s is required but not found in xml for %s", attribute, this));
    				}
    				String value = attributes.get(attribute.getAttributeName());
    				attribute.toJava(value, javaContext);
    			}
    		}
    	}
    }
    
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append(getClass().getSimpleName()).append("[");
    	String separator = "";
    	QName element = getElement();
    	if (element != null) {
    		sb.append(separator).append("element=");
    		sb.append(element.toString());
    		separator = ",";
    	}
    	Class<?> collectionType = getJavaType();
    	if (collectionType != null) {
    		sb.append(separator).append("javaType=").append(collectionType.getName());
    		separator = ",";
    	}
    	
    	sb.append(separator).append("path=").append(getPath()).append("]");
        return sb.toString();
    }
    
    @Override
    public String getPath() {
    	List<String> pathToRoot = new ArrayList<String>();
    	IBinding binding = this;
    	while (binding != null) {
    		String bindingType = binding.getClass().getSimpleName();
    		if (binding.getElement() != null) {
    			bindingType = bindingType.concat("<").concat(binding.getElement().getLocalPart()).concat(">");
    		}
   			pathToRoot.add(bindingType);
    		binding = binding.getParent();
    	}
    	
    	StringBuffer sb = new StringBuffer();
    	for (int i=pathToRoot.size() - 1; i >= 0; i--) {
    		sb.append("/").append(pathToRoot.get(i));
    	}
    	return sb.toString();
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
