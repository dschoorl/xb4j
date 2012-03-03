package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class is a stand-in for a {@link ComplexTypeBinding}, so that a binding can be re-used in multiple 
 * {@link RootBinding} hierarchies.
 * 
 * @author Dave Schoorl
 */
public class ComplexTypeReference extends AbstractSingleBinding {

    private String identifier = null;

    private String namespaceUri = null;
    
    public ComplexTypeReference(QName element, Class<?> javaType, String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(new DefaultElementFetchStrategy(element));
        setObjectCreator(new DefaultConstructor(javaType));
    }

    public ComplexTypeReference(String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
    }

    public ComplexTypeReference(Class<?> javaType, String identifier, String namespaceUri) {
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
        setObjectCreator(new DefaultConstructor(javaType));
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

    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader stream, Object javaContext) throws XMLStreamException {
        Object newJavaContext = newInstance();  //use getter on supplied javaContext??
        getReferencedBinding().toJava(stream, select(javaContext, newJavaContext));
        setProperty(javaContext, newJavaContext);
        return newJavaContext;
    }

    @Override
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
//        getReferencedBinding().toXml(staxWriter, getProperty(javaContext));
        QName element = getElement();
        
        //mixed content is not yet supported -- there are either child elements or there is content
        IBindingBase childBinding = getReferencedBinding();
        boolean isEmptyElement = childBinding == null;
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
        }
        
        if (childBinding != null) {
            childBinding.toXml(staxWriter, getProperty(javaContext));
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
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
    
    public ComplexTypeBinding getReferencedBinding() {
        ComplexTypeBinding referenced = (ComplexTypeBinding)getChildBinding();
        if (referenced == null) {
            IModelAware root = getModelAware();
            ComplexTypeBinding complexType = root.getModel().getComplexType(identifier, namespaceUri);
            referenced = complexType.copy();    //copy without parent
            setChild(referenced);
        }
        return referenced;
    }
    
}
