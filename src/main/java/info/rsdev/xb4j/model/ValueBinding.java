package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * 
 * @author Dave Schoorl
 */
public class ValueBinding extends AbstractBinding {
    
    public ValueBinding(QName element) {
        super(element);
    }

    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader) throws XMLStreamException {
        String value = null;
        if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
            QName element = staxReader.getName();
            if (isExpected(element)) {
                value = staxReader.getElementText();
            }
        }
        
        return value;
    }
    
    @Override
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object elementValue) throws XMLStreamException {
        QName element = getElement();
        
        staxWriter.writeElement(element, false);
        
        if (elementValue != null) {
            staxWriter.writeContent(elementValue.toString());
        }
        
        staxWriter.closeElement(element);
    }

}
