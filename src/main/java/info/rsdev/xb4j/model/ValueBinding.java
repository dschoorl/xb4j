package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.InheritObjectFetchStrategy;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * <p>Translates a text-only element to a Java field and vice versa. The Java field is expected to be a String.
 * Other types will need a converter to convert the field to and from a String.</p>
 * TODO: add converter mechanism 
 * 
 * @author Dave Schoorl
 */
public class ValueBinding extends AbstractBinding {
    
    /**
     * Create a new {@link ValueBinding} with a {@link DefaultElementFetchStrategy}
     * @param element the element 
     */
    public ValueBinding(QName element) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    	setObjectFetchStrategy(new InheritObjectFetchStrategy(this));
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

    @Override
    public String toString() {
        return String.format("ValueBinding[element=%s]", getElement());
    }
    
}
