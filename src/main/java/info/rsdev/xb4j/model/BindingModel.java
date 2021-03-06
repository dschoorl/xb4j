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
import info.rsdev.xb4j.model.bindings.ISemaphore;
import info.rsdev.xb4j.model.bindings.Reference;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.UnmarshallResult;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader.Marker;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The model knows how to map Java objects to a certain xml definition and visa versa. The metaphor used to bind xml and java,
 * regardless of direction, is a binding? A binding always binds something in the xml world to something in the Java world. Every
 * binding can be marhalled or unmarshalled standalone.
 *
 * @author Dave Schoorl
 */
public class BindingModel {

    private final Logger log = LoggerFactory.getLogger(BindingModel.class);

    private final Map<Class<?>, LinkedList<Root>> classToXml = new ConcurrentHashMap<>();

    private final Map<QName, Root> xmlToClass = new ConcurrentHashMap<>();

    private final Map<QName, ComplexType> complexTypes = new ConcurrentHashMap<>();

    /**
     * Get the {@link XmlStreamer} that is capable of marshalling / unmarshalling the given Java class. When there are multiple
     * bindings for the same Java class, E.g. when there are different xml representations for it, a selector must be provided.
     *
     * @param type the Java class to get the {@link XmlStreamer} for
     * @param selector When the model has multiple bindings for a Java class, the selector must be used to select the right one. Can
     * be ommited (null) when there is only one binding for this class.
     * @return the {@link XmlStreamer} of choice. Once an XmlStreamer is requested, the underlying binding can no longer be changed
     */
    public XmlStreamer getXmlStreamer(Class<?> type, QName selector) {
        Root binding = getBinding(type, selector);
        if (binding == null) {
            throw new Xb4jException(String.format("No binding found for %s with selector %s", type, selector));
        }

        //make binding immutable on first use so we can guarantee marshall/unmarshall results will be the same every time it is used
        ISemaphore semaphore = binding.getSemaphore();
        semaphore.lock();
        try {
            binding.makeImmutable();
            return new XmlStreamer(binding);
        } finally {
            semaphore.unlock();
        }
    }

    /**
     * <p>
     * Read Java object tree from the given xml stream. At least the first start element is read, in order to determine if this
     * {@link BindingModel} knows how to construct the given Java object tree for that element. When this {@link BindingModel} does
     * not know how to handle the element, an {@link Xb4jException} is thrown. At least part of the xml stream will be consumed
     * nonetheless.</p>
     * <p>
     * The {@link XMLStreamReader} is not closed; that is the responsibility of the caller.</p>
     *
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

                    //make binding immutable on first use so we can guarantee marshall/unmarshall results will be the same every time it is used
                    ISemaphore semaphore = binding.getSemaphore();
                    semaphore.lock();
                    try {
                        binding.makeImmutable();
                    } finally {
                        semaphore.unlock();
                    }

                    UnmarshallResult result = binding.toJava(staxReader, new JavaContext(null));
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
     * Get the {@link Root} that is bound to the supplied type. When multiple bindings are bound to the supplied type, an
     * exception is thrown.
     *
     * @param type the Java type to get the binding for
     * @return a {@link Root} or null when no binding is defined for the Java type
     * @throws Xb4jException when multiple bindings are bound to the provided type
     * @see #getBinding(java.lang.Class, javax.xml.namespace.QName) 
     */
    protected Root getBinding(Class<?> type) {
        return getBinding(type, null);
    }

    /**
     * Get the {@link Root} that is bound to the supplied Java type and who's element equals the given selector. The selector is
     * mandatory when a single Java type has been bound through multiple root bindings to different xml elements, in order to make 
     * the appropriate selection.
     * 
     * @param type the Java type to obtain the root binding for
     * @param selector When multiple {@link Root} bindings exist for the given Java type, the binding is chosen who's element 
     *      matches this QName
     * @return a {@link Root} or null when no binding is defined for the Java type and selector
     * @throws Xb4jException when multiple root bindings are bound to the given Java type, but no selector was provided to 
     *      select the right one
     */
    protected Root getBinding(Class<?> type, QName selector) {
        if (!this.classToXml.containsKey(type)) {
            return null;
        }

        LinkedList<Root> bindings = classToXml.get(type);
        if ((bindings.size() == 1) && (selector == null)) {
            return bindings.getFirst();
        } else if ((bindings.size() > 1) && (selector == null)) {
            Set<String> candidates = new HashSet<>(bindings.size());
            bindings.forEach((candidate) -> {
                candidates.add(candidate.getElement().getNamespaceURI());
            });
            throw new Xb4jException(String.format("Multiple bindings found. Please specify a QName to select the required "
                    + "binding (one of: %s)", candidates));
        } else {
            Root target = null;
            for (Root candidate : bindings) {
                if (candidate.getElement().equals(selector)) {
                    target = candidate;
                    break;
                }
            }
            return target;
        }
    }

    /**
     * Register a {@link Root} binding with this binding model.
     * @param binding the {@link Root} to register with this model
     * @return the registered Root binding for further fluent programming
     * @throws IllegalArgumentException when a root binding with the same type and element QName is already registered
     */
    public Root registerRoot(Root binding) {
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
            throw new IllegalArgumentException(String.format("Cannot register '%s', because %s is "
                    + "already registered for %s", binding, xmlToClass.get(element), element));
        }

        /* A Java class can be bound to multiple Root-bindings, but each binding must use a different QName.
         */
        xmlToClass.put(element, binding);
        LinkedList<Root> boundToClass = classToXml.get(javaType);
        if (boundToClass == null) {
            boundToClass = new LinkedList<>();
            classToXml.put(javaType, boundToClass);
        }
        QName newElement = binding.getElement();
        if (!boundToClass.isEmpty()) {
            for (Root candidate : boundToClass) {
                QName candidateElement = candidate.getElement();
                if (candidateElement.equals(newElement)) {
                    throw new Xb4jException(String.format("Multiple bindings for %s must all use a different QName. Qname "
                            + "%s is not unique.", javaType.getName(), newElement));
                }
            }
        }
        boundToClass.add(binding);

        binding.setModel(this);
        return binding;
    }

    /**
     * Register a {@link ComplexType} with this binding model, provided that no complex type with the same namespace and identifier 
     * has already been registered. 
     * @param complexType the complex type to register
     * @param errorIfExists flag to ignore or throw exception when a complex type registration collides with a previous one
     * @return the registered {@link ComplexType} for further fluent programming
     * @throws IllegalArgumentException when an registration collision occurs and is not allowed vi parameter errorIfExists
     */
    public ComplexType registerComplexType(ComplexType complexType, boolean errorIfExists) {
        if (complexType == null) {
            throw new NullPointerException("ComplexTypeBinding cannot be null");
        }
        if (complexType.getParent() != null) {
            throw new Xb4jException(String.format("ComplexType %s cannot be registered when it is part of a binding hierarchy; "
                    + "substitute with ComplexTypeReference in the binding hierarchy where applicable.", complexType));
        }
        QName fqComplexTypeName = new QName(complexType.getNamespace(), complexType.getIdentifier());
        if (this.complexTypes.containsKey(fqComplexTypeName)) {
            String message = String.format("Cannot register ComplexTypeBinding '%s', because a ComplextTypeBinding (%s) is "
                    + "already registered with identifier='%s' and namespace='%s'", complexType, complexTypes.get(fqComplexTypeName),
                    complexType.getIdentifier(), complexType.getNamespace());
            if (errorIfExists) {
                throw new IllegalArgumentException(message);
            } else if (log.isDebugEnabled()) {
                log.debug(message);
            }
        }
        this.complexTypes.put(fqComplexTypeName, complexType);
        complexType.setModel(this);
        return complexType;
    }

    /**
     * Get the instance of the {@link ComplexType} with the given identifier in the given namespace. This method is used by the 
     * framework to resolve {@link Reference references}
     * 
     * @param namespaceUri the namespace uri in which the complex type lives
     * @param identifier the identifier for the complex type within the given namespace
     * @return the {@link ComplexType} requested
     * @throws Xb4jException when there is no complex type registered by the given identifier and namespace
     * @deprecated TODO: remove public access once the complex type references are resolved when obtaining an {@link XmlStreamer}
     */
    public ComplexType getComplexType(String identifier, String namespaceUri) {
        QName identification = new QName(namespaceUri, identifier);
        if (!complexTypes.containsKey(identification)) {
            throw new Xb4jException(String.format("No ComplexType registered with identifier='%s' and namespaceUri='%s'",
                    identifier, namespaceUri));
        }
        return this.complexTypes.get(identification);
    }

}
