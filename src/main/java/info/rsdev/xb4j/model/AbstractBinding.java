package info.rsdev.xb4j.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.namespace.QName;

/**
 *
 * @author Dave Schoorl
 */
public abstract class AbstractBinding implements IBinding {
    
    private QName element = null;
    
    private IGetter getter = null;
    
    private ISetter setter = null;
    
    private ArrayList<IBinding> children = new ArrayList<IBinding>();
    
    public AbstractBinding(QName element) {
        this.element = element;
    }
    
    public QName getElement() {
        return element;
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
        return this.element.equals(element);
    }
    
}
