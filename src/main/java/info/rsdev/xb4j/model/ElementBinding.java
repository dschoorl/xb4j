package info.rsdev.xb4j.model;

import info.rsdev.xb4j.test.MyObject;
import info.rsdev.xb4j.test.ObjectTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.namespace.QName;

/**
 *
 * @author Dave Schoorl
 */
public class ElementBinding{
    
    private QName element = null;
    
    private Instantiator instantiator = null;
    
//    private PropertyAccesor accesor = null;
    
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
    public void addChild(ElementBinding childBinding) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
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
    
    public boolean setProperty(Object objectContext, Object property) {
        boolean isPropertySet = false;
        if (objectContext instanceof ObjectTree) {
            ((ObjectTree)objectContext).setMyObject((MyObject)property);
            isPropertySet = true;
        }
        return isPropertySet;
    }
    
    @Override
    public String toString() {
        return String.format("ElementBinding[element=%s, javaType=%s]",this.element, getJavaType());
    }

    public boolean isExpected(QName element) {
        return this.element.equals(element);
    }
}
