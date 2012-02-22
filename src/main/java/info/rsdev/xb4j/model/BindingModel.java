package info.rsdev.xb4j.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The model knows how to map Java objects to a certain xml definition and visa versa. The metaphor used 
 * to bind xml and java, regardless of direction, is a binding? A binding always binds an element to a 
 * Java class. Every binding can marhalled or unmarshalled standalone.
 *  
 * 
 * @author Dave Schoorl
 */
public class BindingModel {
    
    private static final Logger log = LoggerFactory.getLogger(BindingModel.class);
    
    private Map<Class<?>, ElementBinding> classToXml = new HashMap<Class<?>, ElementBinding>();
    
    private Map<QName, ElementBinding> xmlToClass = new HashMap<QName, ElementBinding>();

    /**
     * Marshall a Java instance into xml representation
     * 
     * @param stream
     * @param instance
     */
    public void toXml(OutputStream stream, Object instance) {
        XMLStreamWriter staxWriter = null;
        try {
            staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
//            staxWriter.writeStartDocument();
            ElementBinding binding = getBinding(instance.getClass());
            MarshallingContext context = new MarshallingContext(staxWriter);
            context.marshall(binding, instance);
            staxWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            log.error("Exception occured when writing instance to xml stream: ".concat(instance.toString()), e);
        } finally {
            if (staxWriter != null) {
                try {
                    staxWriter.close();
                } catch (XMLStreamException e) {
                    log.error("Exception occured when closing xml stream", e);
                }
            }
        }
    }
    
    public Object toJava(InputStream stream) {
        if (!stream.markSupported()) {
            throw new IllegalArgumentException("Stream does not supported mark/reset. Please choose a stream, E.g. " +
            		"BufferedOutputStream, that does support mark/reset");
        }
        
        XMLStreamReader staxReader = null;
        try {
            staxReader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
            if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
                QName element = staxReader.getName();
                if (xmlToClass.containsKey(element)) {
                    return xmlToClass.get(element).newInstance();
                }
            }
        } catch (XMLStreamException e) {
            log.error("Exception occured when reading instance from xml stream", e);
        } catch (FactoryConfigurationError e) {
            log.error("Exception occured when creating reader for xml stream", e);
        } finally {
            try {
                staxReader.close();
            } catch (XMLStreamException e) {
                log.error("Exception occured when closing xml stream", e);
            }
        }
        return null;
    }
    
    private ElementBinding getBinding(Class<?> type) {
        if (!this.classToXml.containsKey(type)) {
            return null;
        }
        return this.classToXml.get(type);
    }
    
    public void bind(ElementBinding binding) {
        xmlToClass.put(binding.getElement(), binding);
        classToXml.put(binding.getJavaType(), binding);
    }
    
}
