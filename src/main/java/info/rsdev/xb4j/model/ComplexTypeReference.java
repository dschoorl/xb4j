package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;

/**
 * This class is a stand-in for a {@link ComplexTypeBinding}, so that a binding can be re-used in multiple 
 * {@link RootBinding} hierarchies.
 * 
 * @author Dave Schoorl
 */
public class ComplexTypeReference extends AbstractBinding implements IBinding {

    private String identifier = null;

    private String namespaceUri = null;

    public ComplexTypeReference(String identifier, String namespaceUri) {
        if (identifier == null) {
            throw new NullPointerException("Identifier cannot be null");
        }
        this.identifier = identifier;
        this.namespaceUri = namespaceUri == null ? XMLConstants.NULL_NS_URI : namespaceUri;
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

}
