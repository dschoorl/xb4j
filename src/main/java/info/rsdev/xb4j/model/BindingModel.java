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
import info.rsdev.xb4j.model.bindings.ComplexType;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.UnmarshallResult;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader.Marker;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
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
    
    private Map<Class<?>, LinkedList<Root>> classToXml = new HashMap<Class<?>, LinkedList<Root>>();
    
    private Map<QName, Root> xmlToClass = new HashMap<QName, Root>();
    
    private Map<QName, ComplexType> complexTypes = new HashMap<QName, ComplexType>();

    /**
     * Marshall a Java instance into xml representation
     * 
     * @param stream
     * @param instance
     */
    public void toXml(OutputStream stream, Object instance) {
        toXml(stream, instance, (String)null);
    }
    
    public void toXml(OutputStream stream, Object instance, String namespaceSpecifier) {
        if (instance == null) {
            throw new NullPointerException("Java instance to convert to xml cannot be null");
        }
        Root binding = getBinding(instance.getClass(), namespaceSpecifier);
        toXml(stream, instance, binding);
    }
    
    private void toXml(OutputStream stream, Object instance, Root binding) {
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
            SimplifiedXMLStreamWriter simpleWriter = new SimplifiedXMLStreamWriter(staxWriter);
            binding.toXml(simpleWriter, instance);
            simpleWriter.close();
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
    
    /**
     * Create an {@link XMLStreamReader} from the given {@link InputStream} and call {@link #toJava(XMLStreamReader)}. The 
     * {@link XMLStreamReader} is closed, but the {@link InputStream} is not; it's the responsibility of the caller to close 
     * the {@link InputStream}
     * 
     * @param stream the xml provided as an {@link InputStream}
     * @return the Java object tree read from the xml stream
     * @throws Xb4jException any unmarshalling exception that may occur is propogated
     */
    public Object toJava(InputStream stream) {
    	XMLStreamReader xmlStream = null;
        try {
        	xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(stream);
            return toJava(xmlStream);
        } catch (XMLStreamException e) {
        	throw new Xb4jException("Exception occured when creating XMLStreamReader from InputStream", e);
        } finally {
        	if (xmlStream != null) {
        		try {
					xmlStream.close();
				} catch (XMLStreamException e) {
					log.error("Exception occured when closing xml stream", e);
				}
        	}
        }
    }
    
    /**
     * <p>Read Java object tree from the given xml stream. At least the first start element is read, in order to determine if
     * this {@link BindingModel} knows how to construct the given Java object tree for that element. When this {@link BindingModel}
     * does not know how to handle the element, an {@link Xb4jException} is thrown. At least part of the xml stream will be consumed 
     * nonetheless.</p>
     * <p>The {@link XMLStreamReader} is not closed; that is the responsibility of the caller.</p>
     * @param reader the xml stream reader
     * @return the Java object tree read from the xml stream
     * @throws Xb4jException when something went wrong during unmarshalling of the xml stream
     */
    public Object toJava(XMLStreamReader reader) {
        RecordAndPlaybackXMLStreamReader staxReader = null;
        try {
            staxReader = new RecordAndPlaybackXMLStreamReader(reader);
            Marker startMarker = staxReader.startRecording();
            if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
                QName element = staxReader.getName();
                staxReader.rewindAndPlayback(startMarker);
                if (xmlToClass.containsKey(element)) {
                    Root binding = xmlToClass.get(element);
                    UnmarshallResult result = binding.toJava(staxReader, null);
                    if (result.isUnmarshallSuccessful()) {
                    	return result.getUnmarshalledObject();
                    } else {
                    	throw new Xb4jException(result.getErrorMessage());
                    }
                    
                } else {
                	throw new Xb4jException(String.format("No binding found for xml element %s", element));
                }
            }
        } catch (XMLStreamException e) {
        	throw new Xb4jException("Exception occured when reading from xml stream", e);
        } finally {
        	if (staxReader != null) {
                staxReader.close();
        	}
        }
        return null;
    }
    
    /**
     * Get the {@link Root} that is bound to the supplied type. When multiple bindings are bound to the
     * supplied type, a
     * @param type
     * @return a {@link Root} or null when no binding is bound to the Java type
     * @throws Xb4jException when multiple bindings are bound to 
     * @see
     */
    public Root getBinding(Class<?> type) {
        return getBinding(type, null);
    }
    
    public Root getBinding(Class<?> type, String namespaceSpecifier) {
        if (!this.classToXml.containsKey(type)) {
            return null;
        }
        
        LinkedList<Root> bindings = classToXml.get(type);
        if ((bindings.size() == 1) && (namespaceSpecifier == null)) {
            return bindings.getFirst();
        } else if ((bindings.size() > 1) && (namespaceSpecifier == null)) {
            Set<String> candidates = new HashSet<String>(bindings.size());
            for (Root candidate: bindings) {
                candidates.add(candidate.getElement().getNamespaceURI());
            }
            throw new Xb4jException(String.format("Multiple bindings found. Please specify a namespace to select the required " +
                    "binding (one of: %s)", candidates));
        } else {
            Root target = null;
            for (Root candidate: bindings) {
                if (candidate.getElement().getNamespaceURI().equals(namespaceSpecifier)) {
                    target = candidate;
                    break;
                }
            }
            return target;
        }
    }
    
    public BindingModel register(Root binding) {
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
    	
    	/* A Java class can be bound to multiple RootBindings, on the condition that the element of the RootBinding uses
    	 * the same localpart as al other bindings for the Java class, but a different namespace.
    	 * The framework is capable of also differenting on localpart, but customers of this framework, E.g. SOAP stacks,
    	 * will not be able to supply the entire QName to select the RootBinding for marshalling. However, they are likely
    	 * able to supply the namespaceUri (based on the namespaceUri of the incoming message).
    	 */
        xmlToClass.put(element, binding);
        LinkedList<Root> boundToClass = classToXml.get(javaType);
        if (boundToClass == null) {
            boundToClass = new LinkedList<Root>();
            classToXml.put(javaType, boundToClass);
        }
        QName newElement = binding.getElement();
        if (!boundToClass.isEmpty()) {
            for (Root candidate: boundToClass) {
            	QName candidateElement = candidate.getElement();
                if (!candidateElement.getLocalPart().equals(newElement.getLocalPart())) {
                	throw new Xb4jException(String.format("Multiple bindings for %s must all use the same element name. Found " +
                			"%s and %s.", javaType.getName(), newElement.getLocalPart(), candidateElement.getLocalPart()));
                } else if (candidateElement.getNamespaceURI().equals(newElement.getNamespaceURI())) {
                	throw new Xb4jException(String.format("Multiple bindings for %s must all use a different namespaceUri. Uri" +
                			"%s is not unique.", javaType.getName(), newElement.getLocalPart()));
                }
            }
        }
        boundToClass.add(binding);
        
        binding.setModel(this);
        return this;
    }
    
    public BindingModel register(ComplexType complexType, boolean errorIfExists) {
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
    
    public ComplexType getComplexType(String identifier, String namespaceUri) {
    	QName identification = new QName(namespaceUri, identifier);
    	if (!complexTypes.containsKey(identification)) {
    		throw new Xb4jException(String.format("No ComplexType registered with identifier='%s' and namespaceUri='%s'", 
    				identifier, namespaceUri));
    	}
        return this.complexTypes.get(identification);
    }
    
}
