package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.xml.IElementFetchStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.namespace.QName;

/**
 *
 * @author Dave Schoorl
 */
public abstract class AbstractBinding implements IBinding {
    
	private IElementFetchStrategy elementFetcher = null;
	
    private IGetter getter = null;
    
    private ISetter setter = null;
    
    private ArrayList<IBinding> children = new ArrayList<IBinding>();
    
    private IBinding parent = null;
    
    public AbstractBinding() { }
    
    public QName getElement() {
    	if (elementFetcher != null) {
    		return elementFetcher.getElement(this);
    	}
        return null;
    }
    
    protected void setElementFetchStrategy(IElementFetchStrategy elementFetcher) {
    	this.elementFetcher = elementFetcher;
    }
    
    public Collection<IBinding> getChildren() {
        return Collections.unmodifiableList(this.children);
    }
    
    protected void setGetter(IGetter getter) {
        this.getter = getter;
    }

    protected void setSetter(ISetter setter) {
        this.setter = setter;
    }
    
    protected void add(IBinding childBinding) {
        this.children.add(childBinding);
        childBinding.setParent(this);	//maintain bidirectional relationship
    }
    
    public void setParent(IBinding parent) {
    	if (parent == null) {
    		throw new NullPointerException();
    	}
    	this.parent = parent;
    }
    
    public IBinding getParent() {
    	return this.parent;
    }

    public boolean setProperty(Object contextInstance, Object propertyValue) {
    	if (this.setter == null) {
    		throw new NullPointerException(String.format("No setter available. Cannot set property value %s on %s", propertyValue, contextInstance));
    	}
        return this.setter.set(contextInstance, propertyValue);
    }
    
    public Object getProperty(Object contextInstance) {
    	if (this.getter != null) {
            return this.getter.get(contextInstance);
    	}
    	return null;
    }
    
    public boolean isExpected(QName element) {
    	if (element == null) {
    		throw new NullPointerException("QName cannot be null");
    	}
        return element.equals(getElement());
    }
    
}
