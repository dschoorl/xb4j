package info.rsdev.xb4j.model;

import java.util.Collection;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Dave Schoorl
 */
public class MarshallingContext {
    
    private static final String DEFAULT_NS_PREFIX = "ns"; 
    
    private int generatedPrefixCounter = 0;
    
//    private Object objectContext = null;
    
    private XMLStreamWriter staxWriter = null;
    
//    private ElementBinding binding = null;
    
    /**
     * keys are namespaceUri's and values are prefixes
     */
    private HashMap<String, String> namespacesInContext = new HashMap<String, String>();
    
    public MarshallingContext(XMLStreamWriter stream) {
//        setInstanceContext(javaInstance);
//        if (stream == null) {
//            throw new NullPointerException("XMLStreamWriter cannot be null");
//        }
        this.staxWriter = stream;
//        if (binding == null) {
//            throw new NullPointerException("Binding cannot be null");
//        }
//        this.binding = binding;
    }
    
//    private void setInstanceContext(Object javaInstance) {
//        if (javaInstance == null) {
//            throw new NullPointerException("Java object context cannot be set to null");
//        }
//        this.objectContext = javaInstance;
//    }
    
    public void marshall(ElementBinding binding, Object objectContext) throws XMLStreamException {
        QName element = binding.getElement();
        String namespace = element.getNamespaceURI();
        Collection<ElementBinding> children = binding.getChildren();
        boolean nsIsKnown = namespacesInContext.containsKey(namespace);
        if (namespace.equals(XMLConstants.NULL_NS_URI)) {
            if (children.isEmpty()) {
                staxWriter.writeEmptyElement(element.getLocalPart());
            } else {
                staxWriter.writeStartElement(element.getLocalPart());
            }
        } else {
            String prefix = getAndPutPrefix(namespace, element.getPrefix());
            if (children.isEmpty()) {
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
        
        for (ElementBinding child: binding.getChildren()) {
            marshall(child, child.getContext(objectContext));
        }
        
        if (!nsIsKnown && (namespace != null)) {
            namespacesInContext.remove(namespace);
        }
        
        if (!children.isEmpty()) {
            staxWriter.writeEndElement();
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
    
    
}
