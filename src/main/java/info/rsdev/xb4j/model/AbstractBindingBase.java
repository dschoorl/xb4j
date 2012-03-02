package info.rsdev.xb4j.model;

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
    
    private boolean isOptional = false;
    
    public AbstractBindingBase() {
    	this.getter = NoGetter.INSTANCE;
    	this.setter = NoSetter.INSTANCE;
    }
    
    /**
     * Copy constructor that copies the properties of the original binding in a 
     * @param original
     * @param newParent
     */
    protected AbstractBindingBase(AbstractBindingBase original, ComplexTypeReference newParent) {
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
    
    protected RootBinding getRootBinding() {
        IBindingBase root = this;
        while (root.getParent() != null) {
        	root = root.getParent();
        }
        return (RootBinding)root;   //RootBinding should always be at the root of a binding hierarchy
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
        return String.format("%s[element=%s, javaType=%s]", fqClassName.substring(dotIndex), getElement(), getJavaType().getName());
    }
    
}
