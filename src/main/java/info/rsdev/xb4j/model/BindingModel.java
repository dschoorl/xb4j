package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
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
        if (instance == null) {
            throw new NullPointerException("Instance to marshall to xml cannot be null");
        }
        if (stream == null) {
            throw new NullPointerException("OutputStream cannot be null");
        }
        
        XMLStreamWriter staxWriter = null;
        try {
            staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
//            staxWriter.writeStartDocument();
            RootBinding binding = getBinding(instance.getClass());
            if (binding == null) {
                throw new IllegalArgumentException("No binding found for: ".concat(instance.getClass().getName()));
            }
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
                    return binding.toJava(staxReader, null);//context.unmarshall(staxReader, binding, null);
                }
            }
        } catch (XMLStreamException e) {
            log.error("Exception occured when reading instance from xml stream", e);
        } catch (FactoryConfigurationError e) {
            log.error("Exception occured when creating reader for xml stream", e);
        } finally {
        	if (staxReader != null) {
	            try {
	                staxReader.close();
	            } catch (XMLStreamException e) {
	                log.error("Exception occured when closing xml stream", e);
	            }
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
    
    public BindingModel register(RootBinding binding) {
    	if (binding == null) {
    		throw new NullPointerException("RootBinding cannot be null when registering it");
    	}
    	QName element = binding.getElement();
    	if (element == null) {
    		throw new NullPointerException("FQN of element cannot be null in a RootBinding you try to register");
    	}
    	Class<?> javaType = binding.getJavaType();
    	if (javaType == null) {
    		throw new NullPointerException("Java type cannot be null in a RootBinding you try to register");
    	}
    	if (xmlToClass.containsKey(element) && !binding.equals(xmlToClass.get(element))) {
    		throw new IllegalArgumentException(String.format("Cannot register binding '%s', because another one (%s) is " +
    				"already registered for this FQN element: '%s'", binding, xmlToClass.get(element), element));
    	}
    	if (classToXml.containsKey(javaType) && !binding.equals(classToXml.get(javaType))) {
    		throw new IllegalArgumentException(String.format("Cannot register RootBinding '%s', because another one (%s) is " +
    				"already registered for this Java type: '%s'", binding, xmlToClass.get(element), javaType.getName()));
    	}
        xmlToClass.put(element, binding);
        classToXml.put(javaType, binding);
        binding.setModel(this);
        return this;
    }
    
    public BindingModel register(ComplexTypeBinding complexType) {
        if (complexType == null) {
            throw new NullPointerException("ComplexTypeBinding cannot be null");
        }
        if (complexType.getParent() != null) {
        	throw new Xb4jException(String.format("ComplexType %s cannot be registered when it is part of a binding hierarchy; " +
        			"substitute with ComplexTypeReference in the binding hierarchy where applicable.", complexType));
        }
        QName fqComplexTypeName = new QName(complexType.getNamespace(), complexType.getIdentifier());
        if (this.complexTypes.containsKey(fqComplexTypeName)) {
            throw new IllegalArgumentException(String.format("Cannot register ComplexTypeBinding '%s', because another one (%s) is " +
    				"already registered with identifier='%s' and namespace='%s'", complexType, complexTypes.get(fqComplexTypeName), 
    				complexType.getIdentifier(), complexType.getNamespace()));
        }
        this.complexTypes.put(fqComplexTypeName, complexType);
        complexType.setModel(this);
        return this;
    }
    
    public ComplexTypeBinding getComplexType(String identifier, String namespaceUri) {
        return this.complexTypes.get(new QName(namespaceUri, identifier));
    }
    
}
