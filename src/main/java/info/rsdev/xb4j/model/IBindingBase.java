package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;


/**
 * This interface defines how to transform from Java instance to xml and visa versa, regardless whether the binding represents
 * a single element or a group of elements.
 * 
 * @author Dave Schoorl
 */
public interface IBindingBase {
    
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException;
    
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException;
    
    /**
     * Bindings are organized in a hierarchy. Call setParent to build the hierarchy of bindings.
     * @param parent the parent {@link IBindingBase} that this binding is a child of.
     */
    public void setParent(IBindingBase parent);
    
    public IBindingBase getParent();
    
    public QName getElement();
    
    public Class<?> getJavaType();
    
    public Object newInstance();
    
    public Object getProperty(Object contextInstance);
    
    public boolean setProperty(Object contextInstance, Object propertyValue);
    
    public IBindingBase setGetter(IGetter getter);
    
    public IBindingBase setSetter(ISetter setter);
    
    public boolean isOptional();
    
    public IBindingBase setOptional(boolean isOptional);
}
