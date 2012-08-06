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
package info.rsdev.xb4j.util;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SimplifiedXMLStreamWriter {
	
	private static final String EMPTY = "";

    private XMLStreamWriter staxWriter = null;
    
    private NamespaceContext namespaceContext = new NamespaceContext();
    
    private final String encodingOfXmlStream;
    
    public SimplifiedXMLStreamWriter(XMLStreamWriter staxWriter) {
    	this(staxWriter, "UTF-8");
    }
    
    /**
     * @param staxWriter
     * @param encoding the encoding that the staxWriter is set to
     */
    public SimplifiedXMLStreamWriter(XMLStreamWriter staxWriter, String encoding) {
        if (staxWriter == null) {
            throw new NullPointerException("XMLStreamWriter cannot be null");
        }
        if (encoding == null) {
        	throw new NullPointerException("Encoding cannot be null");
        }
        this.staxWriter = staxWriter;
        this.encodingOfXmlStream = encoding;
    }
    
    public void writeElement(QName element, boolean isEmptyElement) throws XMLStreamException {
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
    
    /**
     * Insert the bytes of the inputStream into the staxWriter
     * 
     * @param in
     */
    public int elementContentFromInputStream(InputStream in) {
    	int totalCharsRead = 0;
    	try {
        	InputStreamReader reader = new InputStreamReader(in, this.encodingOfXmlStream);
        	char[] buffer = new char[1024];
        	int charsRead = 0;
        	while ((charsRead = reader.read(buffer)) != -1) {
        		totalCharsRead += charsRead;
        		if (charsRead > 0) {
        			staxWriter.writeCharacters(new String(buffer, 0, charsRead));
        		}
        	}
    	} catch (Exception e) {
    		throw new Xb4jException("Exception occured when inserting contents from external stream", e);
    	}
    	return totalCharsRead;
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
    
    public void close() throws XMLStreamException {
		staxWriter.writeEndDocument();
    	namespaceContext.clear();
    	staxWriter = null;	//do not close the underlying staxWriter, because we do not control it
    }
    
}
