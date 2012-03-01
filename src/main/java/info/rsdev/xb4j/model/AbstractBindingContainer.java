package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public abstract class AbstractBindingContainer extends AbstractBinding implements IBindingContainer {

    private ArrayList<IBinding> children = new ArrayList<IBinding>();
    
    /**
     * <p>When unmarshalling, the child binding will know how to get from the current element to the next one. Which
     * element to expect next. Any, choice, sequence</p> 
     * @param childBinding
     * @return the childBinding
     */
    public IBinding add(IBinding childBinding, IGetter getter, ISetter setter) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        setGetter(getter);
        setSetter(setter);
        
        return add(childBinding);
    }
    
    /**
     * Convenience method, which adds a child binding, and navigating the object tree from parent to child is done through
     * the field with the given fieldname.
     * 
     * @param childBinding
     * @param fieldName
     * @return the childBinding
     */
    public IBinding add(IBinding childBinding, String fieldName) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        if (fieldName == null) {
        	throw new NullPointerException("Fieldname cannot be null");
        }
        FieldAccessProvider provider = new FieldAccessProvider(fieldName);
        setGetter(provider);
        setSetter(provider);
        
        return add(childBinding);
    }

    /**
     * Add a {@link IBinding} to a binding container. A bidirectional relationship will be established between the
     * container and the child.
     * 
     * @param childBinding the binding to add to this group
     * @return the childBinding
     */
    public IBinding add(IBinding childBinding) {
        this.children.add(childBinding);
        childBinding.setParent(this);   //maintain bidirectional relationship
        return childBinding;
    }
    
    public ChoiceBinding add(ChoiceBinding childBinding) {
    	add((IBinding)childBinding);
    	return childBinding;
    }
    
    public ComplexTypeBinding add(ComplexTypeBinding childBinding) {
    	add((IBinding)childBinding);
    	return childBinding;
    }
    
    /**
     * @param childBinding
     * @return the {@link SequenceBinding} that was added to this binding container
     */
    public SequenceBinding add(SequenceBinding childBinding) {
    	add((IBinding)childBinding);
    	return childBinding;
    }
    
	@Override
	public IBindingContainer add(IBindingContainer childContainer) {
		add((IBinding)childContainer);
		return childContainer;
	}

    public Collection<IBinding> getChildren() {
        return Collections.unmodifiableList(this.children);
    }
    
    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
    	Object newJavaContext = null;
        if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
            QName element = staxReader.getName();
            if (isExpected(element)) {
            	newJavaContext = newInstance();    //or instance is passed on - through a stack somewhere?
                for (IBinding child: getChildren()) {
                    Object childContext = child.toJava(staxReader, select(javaContext, newJavaContext));
                    setProperty(newJavaContext, childContext);
                }
            }
        }
        
        return newJavaContext;
    }
    
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        
        //mixed content is not yet supported -- there are either child elements or there is content
        Collection<IBinding> children = getChildren();
        boolean isEmptyElement = children.isEmpty();
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
        }
        
        for (IBinding child: children) {
            child.toXml(staxWriter, getProperty(javaContext));
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
    }
    
}
