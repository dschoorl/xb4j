package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlayBackXMLStreamReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.namespace.QName;

/**
 *
 * @author Dave Schoorl
 */
public class ElementBinding implements IBinding {
    
    private QName element = null;
    
    private Instantiator instantiator = null;
    
    private IGetter getter = null;
    
    private ISetter setter = null;
    
    private ArrayList<ElementBinding> children = new ArrayList<ElementBinding>();
    
    public ElementBinding(QName element, Instantiator instantiator) {
        this.element = element;
        this.instantiator = instantiator;
    }
    
    /**
     * Create a new {@link ElementBinding} where the javaType will be created with a {@link DefaultConstructor}
     * 
     * @param element
     * @param javaType
     */
    public ElementBinding(QName element, Class<?> javaType) {
        this.element = element;
        this.instantiator = new DefaultConstructor(javaType);
    }
    
    /**
     * <p>When unmarshalling, the child binding will know how to get from the current element to the next one. Which
     * element to expect next. Any, choice, sequence</p> 
     * @param childBinding
     */
    public void addChild(ElementBinding childBinding, IGetter getter, ISetter setter) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        this.getter = getter.setContext(getJavaType());
        this.setter = setter.setContext(getJavaType());
        
        this.children.add(childBinding);
    }
    
    /**
     * Convenience method, which adds a child binding, and navigating the object tree from parent to child is done through
     * the field with the given fieldname.
     * 
     * @param childBinding
     * @param fieldName
     */
    public void addChild(ElementBinding childBinding, String fieldName) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        if (fieldName == null) {
        }
        FieldAccessProvider provider = new FieldAccessProvider(getJavaType(), fieldName);
        this.getter = provider;
        this.setter = provider;
        
        this.children.add(childBinding);
    }

    public QName getElement() {
        return element;
    }
    
    public Class<?> getJavaType() {
        return instantiator.getJavaType();
    }
    
    public Object newInstance() {
        return instantiator.newInstance();
    }

    public Collection<ElementBinding> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    public Object getContext(Object objectContext) {
        return newInstance();
    }
    
    public boolean setProperty(Object contextInstance, Object propertyValue) {
    	if (this.setter == null) {
    		throw new NullPointerException(String.format("No setter available. Cannot set property value %s on %s", propertyValue, contextInstance));
    	}
        return this.setter.set(contextInstance, propertyValue);
    }
    
    public Object getProperty(Object contextInstance) {
    	if (this.getter == null) {
    		throw new NullPointerException(String.format("No getter available. Cannot get property value from %s", contextInstance));
    	}
    	return this.getter.get(contextInstance);
    }
    
    @Override
    public String toString() {
        return String.format("ElementBinding[element=%s, javaType=%s]",this.element, getJavaType());
    }

    public boolean isExpected(QName element) {
        return this.element.equals(element);
    }

    @Override
    public Object toJava(RecordAndPlayBackXMLStreamReader stream) {
        // TODO Auto-generated method stub
        return null;
    }
}
