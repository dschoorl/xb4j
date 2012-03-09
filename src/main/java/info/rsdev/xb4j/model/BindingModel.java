/* Copyright 2012 Red Star Development / Dave Schoorl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader.Marker;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    
    private Map<Class<?>, LinkedList<RootBinding>> classToXml = new HashMap<Class<?>, LinkedList<RootBinding>>();
    
    private Map<QName, RootBinding> xmlToClass = new HashMap<QName, RootBinding>();
    
    private Map<QName, ComplexTypeBinding> complexTypes = new HashMap<QName, ComplexTypeBinding>();

    /**
     * Marshall a Java instance into xml representation
     * 
     * @param stream
     * @param instance
     */
    public void toXml(OutputStream stream, Object instance) {
        toXml(stream, instance, (QName)null);
    }
    
    public void toXml(OutputStream stream, Object instance, QName specifier) {
        if (instance == null) {
            throw new NullPointerException("Java instance to convert to xml cannot be null");
        }
        RootBinding binding = getBinding(instance.getClass(), specifier);
        toXml(stream, instance, binding);
    }
    
    private void toXml(OutputStream stream, Object instance, RootBinding binding) {
        if (stream == null) {
            throw new NullPointerException("OutputStream cannot be null");
        }
        
        XMLStreamWriter staxWriter = null;
        try {
            staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
//            staxWriter.writeStartDocument();
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
        try {
            return toJava(XMLInputFactory.newInstance().createXMLStreamReader(stream));
        } catch (XMLStreamException e) {
            log.error("Exception occured when reading instance from xml stream", e);
        } catch (FactoryConfigurationError e) {
            log.error("Exception occured when creating reader for xml stream", e);
        }
        return null;
    }
    
    public Object toJava(XMLStreamReader reader) {
        RecordAndPlaybackXMLStreamReader staxReader = null;
        try {
            staxReader = new RecordAndPlaybackXMLStreamReader(reader);
            Marker startMarker = staxReader.startRecording();
            if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
                QName element = staxReader.getName();
                staxReader.rewindAndPlayback(startMarker);
                if (xmlToClass.containsKey(element)) {
                    RootBinding binding = xmlToClass.get(element);
                    return binding.toJava(staxReader, null);//context.unmarshall(staxReader, binding, null);
                }
            }
        } catch (XMLStreamException e) {
            log.error("Exception occured when reading instance from xml stream", e);
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
    
    /**
     * Get the {@link RootBinding} that is bound to the supplied type. When multiple bindings are bound to the
     * supplied type, a
     * @param type
     * @return a {@link RootBinding} or null when no binding is bound to the Java type
     * @throws Xb4jException when multiple bindings are bound to 
     * @see
     */
    public RootBinding getBinding(Class<?> type) {
        return getBinding(type, null);
    }
    
    public RootBinding getBinding(Class<?> type, QName specifier) {
        if (!this.classToXml.containsKey(type)) {
            return null;
        }
        
        LinkedList<RootBinding> bindings = classToXml.get(type);
        if ((bindings.size() == 1) && (specifier == null)) {
            return bindings.getFirst();
        } else if ((bindings.size() > 1) && (specifier == null)) {
            List<QName> candidates = new ArrayList<QName>(bindings.size());
            for (RootBinding candidate: bindings) {
                candidates.add(candidate.getElement());
            }
            throw new Xb4jException(String.format("Multiple bindings found. Please specify a QName to select the required " +
                    "binding (one of: %s)", candidates));
        } else {
            RootBinding target = null;
            for (RootBinding candidate: bindings) {
                if (candidate.getElement().equals(specifier)) {
                    target = candidate;
                    break;
                }
            }
            return target;
        }
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
    		throw new IllegalArgumentException(String.format("Cannot register '%s', because %s is " +
    				"already registered for %s", binding, xmlToClass.get(element), element));
    	}
//    	if (classToXml.containsKey(javaType) && !binding.equals(classToXml.get(javaType))) {
//    		throw new IllegalArgumentException(String.format("Cannot register RootBinding '%s', because %s is " +
//    				"already registered for %s", binding, classToXml.get(javaType), javaType.getName()));
//    	}
        xmlToClass.put(element, binding);
        LinkedList<RootBinding> boundToClass = classToXml.get(javaType);
        if (boundToClass == null) {
            boundToClass = new LinkedList<RootBinding>();
            classToXml.put(javaType, boundToClass);
        }
        boundToClass.add(binding);  //TODO: check if another RootBinding is not already registered for this QName?
        
        binding.setModel(this);
        return this;
    }
    
    public BindingModel register(ComplexTypeBinding complexType, boolean errorIfExists) {
        if (complexType == null) {
            throw new NullPointerException("ComplexTypeBinding cannot be null");
        }
        if (complexType.getParent() != null) {
        	throw new Xb4jException(String.format("ComplexType %s cannot be registered when it is part of a binding hierarchy; " +
        			"substitute with ComplexTypeReference in the binding hierarchy where applicable.", complexType));
        }
        QName fqComplexTypeName = new QName(complexType.getNamespace(), complexType.getIdentifier());
        if (this.complexTypes.containsKey(fqComplexTypeName)) {
        	String message = String.format("Cannot register ComplexTypeBinding '%s', because a ComplextTypeBinding (%s) is " +
        			"already registered with identifier='%s' and namespace='%s'", complexType, complexTypes.get(fqComplexTypeName), 
    				complexType.getIdentifier(), complexType.getNamespace());
        	if (errorIfExists) {
                throw new IllegalArgumentException(message);
        	} else if (log.isDebugEnabled()) {
        		log.debug(message);
        	}
        }
        this.complexTypes.put(fqComplexTypeName, complexType);
        complexType.setModel(this);
        return this;
    }
    
    public ComplexTypeBinding getComplexType(String identifier, String namespaceUri) {
        return this.complexTypes.get(new QName(namespaceUri, identifier));
    }
    
}
