package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.xml.IElementFetchStrategy;

import javax.xml.namespace.QName;

/**
 *
 * @author Dave Schoorl
 */
public abstract class AbstractBinding implements IBinding {
    
	private IElementFetchStrategy elementFetcher = null;
	
	private Instantiator objectCreator = null;
	
//	private IObjectFetchStrategy objectFetcher = null;
	
    private IGetter getter = null;
    
    private ISetter setter = null;
    
    private IBinding parent = null;
    
    public AbstractBinding() { }
    
    /**
     * Copy constructor that copies the properties of the original binding in a 
     * @param original
     * @param newParent
     */
    protected AbstractBinding(AbstractBinding original, ComplexTypeReference newParent) {
        this.elementFetcher = original.elementFetcher;
        this.objectCreator = original.objectCreator;
        this.getter = original.getter;
        this.setter = original.setter;
        this.parent = newParent;    //merge copy into another binding hierarchy
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
    
    protected Object getJavaContext(Object javaContext) {
        if (this.objectCreator != null) {
            return newInstance();
        }
        return javaContext;
    }
    
    protected void setElementFetchStrategy(IElementFetchStrategy elementFetcher) {
    	if (elementFetcher == null) {
    		throw new NullPointerException("IElementFetchStrategy cannot be null");
    	}
    	this.elementFetcher = elementFetcher;
    }
    
    protected void setObjectCreator(Instantiator objectCreator) {
        this.objectCreator = objectCreator;
    }
    
    public void setGetter(IGetter getter) {
        this.getter = getter;
    }

    public void setSetter(ISetter setter) {
        this.setter = setter;
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
    
    protected RootBinding getRootBinding() {
        IBinding root = this;
        while (root.getParent() != null) {
        	root = root.getParent();
        }
        return (RootBinding)root;   //RootBinding should always be at the root of a binding hierarchy
    }

    public boolean setProperty(Object contextInstance, Object propertyValue) {
    	if (this.setter == null) {
    	    return false;
//    		throw new NullPointerException(String.format("No setter available. Cannot set property value '%s' on %s", propertyValue, contextInstance));
    	}
        return this.setter.set(contextInstance, propertyValue);
    }
    
    public Object getProperty(Object contextInstance) {
    	if (this.getter != null) {
            return this.getter.get(contextInstance);
    	}
    	return contextInstance;
    }
    
    public boolean isExpected(QName element) {
    	if (element == null) {
    		throw new NullPointerException("QName cannot be null");
    	}
        return element.equals(getElement());
    }
    
    @Override
    public String toString() {
        String fqClassName = getClass().getName();
        int dotIndex = Math.max(0, fqClassName.lastIndexOf('.') + 1);
        return String.format("%s[element=%s, javaType=%s]", fqClassName.substring(dotIndex), getElement(), getJavaType().getName());
    }
    
    protected void copyInto(AbstractBinding copy) {
        
    }
}
