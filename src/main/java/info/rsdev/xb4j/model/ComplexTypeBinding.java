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
 * <p>This binding can be used as an anonymous type in a RootBinding hierarchy, or it can be
 * registered as a type with a {@link BindingModel}, so that the definition can be reused. Reuse 
 * is accomplished by adding a {@link ComplexTypeReference} into the RootBinding hierarchy, that
 * references the ComplexTypeBinding.</p>
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
    
    /**
     * Copy constructor that creates a copy of ComplexTypeBinding with the given {@link ComplexTypeReference parent}
     * as it's parent 
     */
    private ComplexTypeBinding(ComplexTypeBinding original, ComplexTypeReference newParent) {
        super(original, newParent);
        this.identifier = original.identifier;
        this.namespaceUri = original.namespaceUri;
        this.childBinding = original.childBinding;
    }
    
    public IBinding setChild(IBinding childBinding, String fieldName) {
        if (fieldName == null) {
            throw new NullPointerException("Fieldname cannot be null");
        }
        FieldAccessProvider provider = new FieldAccessProvider(fieldName);
        setGetter(provider);
        setSetter(provider);
        
        return setChild(childBinding);
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

	public Object toJava(RecordAndPlaybackXMLStreamReader staxReader) throws XMLStreamException {
		return childBinding.toJava(staxReader);
	}

	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
	    childBinding.toXml(staxWriter, getProperty(javaContext));
	}

    /**
     * Copy the ComplexTypeHierarchy and place it as a child under the supplied  {@link ComplexTypeReference parent}
     * @param complexTypeReference the parent in the hierarchy
     * @return a copy of this {@link ComplexTypeBinding}
     */
    public ComplexTypeBinding copyIntoHierarchy(ComplexTypeReference newParent) {
        return new ComplexTypeBinding(this, newParent);
    }

}
