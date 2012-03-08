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

import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SimplifiedXMLStreamWriter {

    private static final String DEFAULT_NS_PREFIX = "ns"; 
    
    private int generatedPrefixCounter = 0;
    
    private XMLStreamWriter staxWriter = null;
    
    /**
     * keys are namespaceUri's and values are prefixes
     */
    private HashMap<String, String> namespacesInContext = new HashMap<String, String>();
    
    public SimplifiedXMLStreamWriter(XMLStreamWriter staxWriter) {
        if (staxWriter == null) {
            throw new NullPointerException("XMLStreamWriter cannot be null");
        }
        this.staxWriter = staxWriter;
    }
    
    public void writeElement(QName element, boolean isEmptyElement) throws XMLStreamException {
        String namespace = element.getNamespaceURI();
        boolean nsIsKnown = namespacesInContext.containsKey(namespace);
        if (namespace.equals(XMLConstants.NULL_NS_URI)) {
            if (isEmptyElement) {
                staxWriter.writeEmptyElement(element.getLocalPart());
            } else {
                staxWriter.writeStartElement(element.getLocalPart());
            }
        } else {
            String prefix = getAndPutPrefix(namespace, element.getPrefix());
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
    
    public void closeElement(QName element) throws XMLStreamException {
        staxWriter.writeEndElement();
        String namespace = element.getNamespaceURI();
        boolean nsIsKnown = namespacesInContext.containsKey(namespace);
        if (!nsIsKnown && (namespace != null)) {
            namespacesInContext.remove(namespace);
        }
    }

    private String getAndPutPrefix(String namespaceUri, String suggestedPrefix) {
        String prefix = null;
        if (namespaceUri != null) {
            prefix = this.namespacesInContext.get(namespaceUri);
            if (prefix == null) {
                if ((suggestedPrefix != null) && !this.namespacesInContext.containsValue(suggestedPrefix)) {
                    prefix = suggestedPrefix;
                } else {
                    do {
                        prefix = DEFAULT_NS_PREFIX + this.generatedPrefixCounter;
                        this.generatedPrefixCounter++;
                    } while (!this.namespacesInContext.containsValue(prefix));
                }
                this.namespacesInContext.put(namespaceUri, prefix);
            }
        }
        return prefix;
    }

    public void writeContent(String content) throws XMLStreamException {
        if ((content != null) && !content.isEmpty()) {
            staxWriter.writeCharacters(content);
        }
    }
    
}
