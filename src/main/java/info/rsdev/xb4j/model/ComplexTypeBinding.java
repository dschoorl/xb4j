package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.DefaultObjectFetchStrategy;
import info.rsdev.xb4j.model.java.IObjectFetchStrategy;
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
 * <p>This binding should not be used directly in a binding hierarchy, but instead </p>
 * 
 * @see ComplexTypeReference
 * @author Dave Schoorl
 */
public class ComplexTypeBinding extends AbstractBinding {
    
    private String identifier = null;
    
    private String namespaceUri = null;
    
    private IBinding childBinding = null;
	
    public ComplexTypeBinding(QName element, Class<?> javaType, String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(new DefaultElementFetchStrategy(element));
        setObjectFetchStrategy(new DefaultObjectFetchStrategy(javaType));
    }

    public ComplexTypeBinding(String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(new InheritElementFetchStrategy(this));
        setObjectFetchStrategy(new InheritObjectFetchStrategy(this));
    }

    public ComplexTypeBinding(IElementFetchStrategy elementFetcher, String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(elementFetcher);
        setObjectFetchStrategy(new InheritObjectFetchStrategy(this));
    }

    public ComplexTypeBinding(IElementFetchStrategy elementFetcher, IObjectFetchStrategy objectFetcher, String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(elementFetcher);
        setObjectFetchStrategy(objectFetcher);
    }
    
    public IBinding setChild(IBinding childBinding) {
    	if (childBinding == null) {
    		throw new NullPointerException(String.format("Childbinding for %s cannot be null", this));
    	}
    	if ((this.childBinding != null) && !this.childBinding.equals(childBinding)) {
    		throw new IllegalArgumentException();
    	}
    	this.childBinding = childBinding;
    	return childBinding;
    }
    
    public SequenceBinding setChild(SequenceBinding childBinding) {
    	setChild((IBinding)childBinding);
    	return childBinding;
    }

    public ChoiceBinding setChild(ChoiceBinding childBinding) {
    	setChild((IBinding)childBinding);
    	return childBinding;
    }

	public String getIdentifier() {
	    return this.identifier;
	}
	
	private void setIdentifier(String newIdentifer) {
	    if (newIdentifer == null) {
	        throw new NullPointerException("Identifier cannot be null");
	    }
	    this.identifier = newIdentifer;
	}
	
	public String getNamespace() {
	    return this.namespaceUri;
	}

    private void setNamespaceUri(String newNamespaceUri) {
    	if (newNamespaceUri == null) {
    		newNamespaceUri = XMLConstants.NULL_NS_URI;
    	}
    	this.namespaceUri = newNamespaceUri;
    }

	@Override
	public Object toJava(RecordAndPlaybackXMLStreamReader staxReader) throws XMLStreamException {
		return null;
	}

	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
		
	}

}
