package info.rsdev.xb4j.model;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * 
 * @author Dave Schoorl
 */
public class UnmarshallingContext {
    
    /**
     * We need the {@link InputStream} in order to mark/reset when necessary
     */
    private InputStream stream = null;
    
    public UnmarshallingContext(InputStream stream) {
        if (stream == null) {
            throw new NullPointerException("InputStream cannot be null");
        }
        this.stream = stream;
    }
    
    public Object unmarshall(ElementBinding binding, XMLStreamReader staxReader) throws XMLStreamException {
        if (staxReader == null) {
            staxReader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        }
        
        Object javaContext = null;
        if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
            QName element = staxReader.getName();
            if (binding.isExpected(element)) {
                javaContext = binding.newInstance();
                for (ElementBinding child: binding.getChildren()) {
                    Object childContext = unmarshall(child, staxReader);
                    binding.setProperty(javaContext, childContext);
                }
            }
        }
        
        return javaContext;
    }
}
