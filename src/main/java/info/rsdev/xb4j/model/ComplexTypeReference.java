package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * This class is a stand-in for a {@link ComplexTypeBinding}, so that a binding can be re-used in multiple 
 * {@link RootBinding} hierarchies.
 * 
 * @author Dave Schoorl
 */
public class ComplexTypeReference extends ElementBinding {

    private String identifier = null;

    private String namespaceUri = null;
    
    public ComplexTypeReference(String identifier, String namespaceUri) {
        setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
    }

    public ComplexTypeReference(QName element, Class<?> javaType, String identifier, String namespaceUri) {
        super(element, javaType);
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
    }

    public ComplexTypeReference(Class<?> javaType, String identifier, String namespaceUri) {
        super(javaType);
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
    }
    
    /**
     * Create a ComplexTypeReference for an anonymous ComplexType (not registered with {@link BindingModel}.
     * This method is not to be called directly, only by the framework to establish an anonymous complextype 
     * mechanism.
     * 
     * @param element
     * @param referencedBinding
     */
    ComplexTypeReference(QName element, ComplexTypeBinding referencedBinding) {
        if (referencedBinding == null) {
            throw new NullPointerException("ComplexTypeBinding cannot be null");
        }
        setChild(referencedBinding);
        setElementFetchStrategy(new DefaultElementFetchStrategy(element));
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
    
    public ComplexTypeBinding getChildBinding() {
        ComplexTypeBinding referenced = (ComplexTypeBinding)super.getChildBinding();
        if (referenced == null) {
            IModelAware root = getModelAware();
            ComplexTypeBinding complexType = root.getModel().getComplexType(identifier, namespaceUri);
            referenced = complexType.copy();    //copy without parent
            setChild(referenced);
        }
        return referenced;
    }
    
    @Override
    public String toString() {
        if (this.identifier != null) {
            String fqClassName = getClass().getName();
            int dotIndex = Math.max(0, fqClassName.lastIndexOf('.') + 1);
            return String.format("%s[references complexType: identifier=%s, namespace=%s]", fqClassName.substring(dotIndex), identifier, namespaceUri);
        } else {
            return super.toString();
        }
    }
    
}
