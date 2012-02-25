package info.rsdev.xb4j.model;

import java.util.Collection;

import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Dave Schoorl
 */
public class ElementBinding extends AbstractBinding {
    
    private Instantiator instantiator = null;
    
    public ElementBinding(QName element, Instantiator instantiator) {
        super(element);
        this.instantiator = instantiator;
    }
    
    /**
     * Create a new {@link AbstractBinding} where the javaType will be created with a {@link DefaultConstructor}
     * 
     * @param element
     * @param javaType
     */
    public ElementBinding(QName element, Class<?> javaType) {
        super(element);
        this.instantiator = new DefaultConstructor(javaType);
    }

    public Class<?> getJavaType() {
        return instantiator.getJavaType();
    }
    
    public Object newInstance() {
        return instantiator.newInstance();
    }

    /**
     * <p>When unmarshalling, the child binding will know how to get from the current element to the next one. Which
     * element to expect next. Any, choice, sequence</p> 
     * @param childBinding
     */
    public void addChild(IBinding childBinding, IGetter getter, ISetter setter) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        setGetter(getter.setContext(getJavaType()));
        setSetter(setter.setContext(getJavaType()));
        
        super.add(childBinding);
    }
    
    /**
     * Convenience method, which adds a child binding, and navigating the object tree from parent to child is done through
     * the field with the given fieldname.
     * 
     * @param childBinding
     * @param fieldName
     */
    public void addChild(IBinding childBinding, String fieldName) {
        if (childBinding == null) {
            throw new NullPointerException("Child binding cannot be null");
        }
        if (fieldName == null) {
        }
        FieldAccessProvider provider = new FieldAccessProvider(getJavaType(), fieldName);
        setGetter(provider);
        setSetter(provider);
        
        super.add(childBinding);
    }

    @Override
    public String toString() {
        return String.format("ElementBinding[element=%s, javaType=%s]", getElement(), getJavaType());
    }

    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader) throws XMLStreamException {
        Object javaContext = null;
        if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
            QName element = staxReader.getName();
            if (isExpected(element)) {
                javaContext = newInstance();
                for (IBinding child: getChildren()) {
                    Object childContext = child.toJava(staxReader);
                    setProperty(javaContext, childContext);
                }
            }
        }
        
        return javaContext;
    }
    
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        QName element = getElement();
        
        //mixed content is not yet supported -- there are either child elements or there is content
        Collection<IBinding> children = getChildren();
        boolean mustClose = children.isEmpty();
        staxWriter.writeElement(element, mustClose);
        
        for (IBinding child: children) {
            child.toXml(staxWriter, getProperty(javaContext));
        }
        
        if (!mustClose) {
            staxWriter.closeElement(element);
        }
    }
    
}
