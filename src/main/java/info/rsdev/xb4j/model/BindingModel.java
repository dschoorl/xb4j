package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

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
 * to bind xml and java, regardless of direction, is a binding? A binding always binds something in the xml 
 * world to something in the Java world. Every binding can be marhalled or unmarshalled standalone.
 *  
 * @author Dave Schoorl
 */
public class BindingModel {
    
    private static final Logger log = LoggerFactory.getLogger(BindingModel.class);
    
    private Map<Class<?>, RootBinding> classToXml = new HashMap<Class<?>, RootBinding>();
    
    private Map<QName, RootBinding> xmlToClass = new HashMap<QName, RootBinding>();
    
    private Map<QName, ComplexTypeBinding> complexTypes = new HashMap<QName, ComplexTypeBinding>();

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
            RootBinding binding = getBinding(instance.getClass());
            binding.toXml(new SimplifiedXMLStreamWriter(staxWriter), instance);
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
        RecordAndPlaybackXMLStreamReader staxReader = null;
        try {
            staxReader = new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream));
            staxReader.startRecording();
            if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
                staxReader.rewindAndPlayback();
                QName element = staxReader.getName();
                if (xmlToClass.containsKey(element)) {
                    RootBinding binding = xmlToClass.get(element);
                    return binding.toJava(staxReader);//context.unmarshall(staxReader, binding, null);
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
    
    private RootBinding getBinding(Class<?> type) {
        if (!this.classToXml.containsKey(type)) {
            return null;
        }
        return this.classToXml.get(type);
    }
    
    public void register(RootBinding binding) {
        xmlToClass.put(binding.getElement(), binding);
        classToXml.put(binding.getJavaType(), binding);
        binding.setModel(this);
    }
    
    public void register(ComplexTypeBinding complexType) {
        if (complexType == null) {
            throw new NullPointerException("ComplexTypeBinding cannot be null");
        }
        QName fqComplexTypeName = new QName(complexType.getNamespace(), complexType.getIdentifier());
        if (this.complexTypes.containsKey(fqComplexTypeName)) {
            throw new IllegalArgumentException(String.format("A ComplexTypeBinding with identifier='%s' and namespace='%'" +
                    "is already registered", complexType.getIdentifier(), complexType.getNamespace()));
        }
        this.complexTypes.put(fqComplexTypeName, complexType);
    }
    
    public ComplexTypeBinding getComplexType(String identifier, String namespaceUri) {
        return this.complexTypes.get(new QName(namespaceUri, identifier));
    }
    
}
