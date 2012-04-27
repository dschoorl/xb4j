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
package info.rsdev.xb4j.model.util;

import info.rsdev.xb4j.model.bindings.Attribute;

import java.util.Collection;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SimplifiedXMLStreamWriter {
	
	private static final String EMPTY = "";

    private XMLStreamWriter staxWriter = null;
    
    private NamespaceContext namespaceContext = new NamespaceContext();
    
    public SimplifiedXMLStreamWriter(XMLStreamWriter staxWriter) {
        if (staxWriter == null) {
            throw new NullPointerException("XMLStreamWriter cannot be null");
        }
        this.staxWriter = staxWriter;
        staxWriter.getNamespaceContext();
    }
    
    public void writeElement(QName element, Collection<Attribute> attributes, boolean isEmptyElement) throws XMLStreamException {
        String namespace = element.getNamespaceURI();
        boolean nsIsKnown = namespaceContext.isRegistered(namespace);
        if (namespace.equals(XMLConstants.NULL_NS_URI)) {
            if (isEmptyElement) {
                staxWriter.writeEmptyElement(element.getLocalPart());
            } else {
                staxWriter.writeStartElement(element.getLocalPart());
            }
        } else {
            String prefix = namespaceContext.registerNamespace(element);
            if (isEmptyElement) {
                if (nsIsKnown) {
                    staxWriter.writeEmptyElement(namespace, element.getLocalPart());
                } else {
                    staxWriter.writeEmptyElement(prefix, element.getLocalPart(), namespace);
                }
            } else {
                if (nsIsKnown) {
                    staxWriter.writeStartElement(namespace, element.getLocalPart());
                } else {
                    staxWriter.writeStartElement(prefix, element.getLocalPart(), namespace);
                }
            }
            if (!nsIsKnown) {
                staxWriter.writeNamespace(prefix, namespace);
            }
        }
    }
    
    public void writeAttribute(QName elementName, QName attributeName, String value) throws XMLStreamException {
    	if (value == null) { value = EMPTY; }
        String namespace = attributeName.getNamespaceURI();
        boolean nsIsKnown = namespaceContext.isRegistered(namespace);
        if (namespace.equals(XMLConstants.NULL_NS_URI)) {
            staxWriter.writeAttribute(attributeName.getLocalPart(), value);
        } else {
            String prefix = namespaceContext.registerNamespace(elementName, namespace, attributeName.getPrefix());
            if (nsIsKnown) {
                staxWriter.writeAttribute(namespace, attributeName.getLocalPart(), value);
            } else {
                staxWriter.writeAttribute(prefix, namespace, attributeName.getLocalPart(), value);
            }
                
            if (!nsIsKnown) {
                staxWriter.writeNamespace(prefix, namespace);
            }
        }
    }
    
    public void closeElement(QName element) throws XMLStreamException {
        staxWriter.writeEndElement();
        namespaceContext.unregisterNamespacesFor(element);
    }

    public void writeContent(String content) throws XMLStreamException {
        if ((content != null) && !content.isEmpty()) {
            staxWriter.writeCharacters(content);
        }
    }
    
    public void close() {
    	namespaceContext.clear();
    	staxWriter = null;	//do not close the underlying staxWriter, because we do not control it
    }
    
}
