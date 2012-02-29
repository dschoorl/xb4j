package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.DefaultObjectFetchStrategy;
import info.rsdev.xb4j.model.java.InheritObjectFetchStrategy;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.InheritElementFetchStrategy;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class is a stand-in for a {@link ComplexTypeBinding}, so that a binding can be re-used in multiple 
 * {@link RootBinding} hierarchies.
 * 
 * @author Dave Schoorl
 */
public class ComplexTypeReference extends AbstractBinding {

    private String identifier = null;

    private String namespaceUri = null;

    public ComplexTypeReference(QName element, Class<?> javaType, String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(new DefaultElementFetchStrategy(element));
        setObjectFetchStrategy(new DefaultObjectFetchStrategy(javaType));
    }

    public ComplexTypeReference(String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(new InheritElementFetchStrategy(this));
        setObjectFetchStrategy(new InheritObjectFetchStrategy(this));
    }

    public ComplexTypeReference(IElementFetchStrategy elementFetcher, String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(elementFetcher);
        setObjectFetchStrategy(new InheritObjectFetchStrategy(this));
    }

    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader stream) throws XMLStreamException {
        RootBinding root = getRootBinding();
        ComplexTypeBinding complexType = root.getComplexType(identifier, namespaceUri);
        return complexType.toJava(stream);  //TODO: solve -- the complextype is not in the right binding hierarchy
    }

    @Override
    public void toXml(SimplifiedXMLStreamWriter stream, Object javaContext) throws XMLStreamException {
        RootBinding root = getRootBinding();
        ComplexTypeBinding complexType = root.getComplexType(identifier, namespaceUri);
        complexType.toXml(stream, javaContext);  //TODO: solve -- the complextype is not in the right binding hierarchy
    }
    
    private void setIdentifier(String newIdentifier) {
        if (newIdentifier == null) {
            throw new NullPointerException("Identifier cannot be null");
        }
        this.identifier = newIdentifier;
    }
    
    private void setNamespaceUri(String newNamespaceUri) {
    	if (newNamespaceUri == null) {
    		newNamespaceUri = XMLConstants.NULL_NS_URI;
    	}
    	this.namespaceUri = newNamespaceUri;
    }

}
