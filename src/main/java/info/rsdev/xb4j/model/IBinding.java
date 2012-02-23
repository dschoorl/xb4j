package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlayBackXMLStreamReader;


/**
 * This interface defines how to transform from Java instance to xml and visa versa
 * 
 * @author Dave Schoorl
 */
public interface IBinding {
    
    public Object toJava(RecordAndPlayBackXMLStreamReader stream);
    
//    public void toXml(XMLStreamWriter stream);
    
}
