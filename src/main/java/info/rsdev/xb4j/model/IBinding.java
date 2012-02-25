package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import javax.xml.stream.XMLStreamException;


/**
 * This interface defines how to transform from Java instance to xml and visa versa
 * 
 * @author Dave Schoorl
 */
public interface IBinding {
    
    public Object toJava(RecordAndPlaybackXMLStreamReader stream) throws XMLStreamException;
    
    public void toXml(SimplifiedXMLStreamWriter stream, Object javaContext) throws XMLStreamException;
    
}
