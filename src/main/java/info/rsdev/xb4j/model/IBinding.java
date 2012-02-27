package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;


/**
 * This interface defines how to transform from Java instance to xml and visa versa
 * 
 * @author Dave Schoorl
 */
public interface IBinding {
    
    public Object toJava(RecordAndPlaybackXMLStreamReader stream) throws XMLStreamException;
    
    public void toXml(SimplifiedXMLStreamWriter stream, Object javaContext) throws XMLStreamException;
    
    /**
     * Bindings are organized in a hierarchy. Call setParent to build the hierarchy of bindings.
     * @param parent the parent {@link IBinding} that this binding is a child of.
     */
    public void setParent(IBinding parent);
    
    public IBinding getParent();
    
    public QName getElement();
    
    public Class<?> getJavaType();
    
    public Object newInstance();
}
