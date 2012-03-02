package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.FetchFromParentStrategy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * <p>Translates a text-only element to a Java field and vice versa. The Java field is expected to be a String.
 * Other types will need a converter to convert the field to and from a String.</p>
 * 
 * TODO: add support for fixed / default values in the xml world?
 * TODO: simple type cannot be an empty element??
 * 
 * @author Dave Schoorl
 */
public class ElementBinding extends AbstractBinding {
	
	private IBinding childBinding = null;
	
    public ElementBinding() {
    	setElementFetchStrategy(new FetchFromParentStrategy(this));
    }

    /**
     * Create a new {@link ElementBinding} with a {@link DefaultElementFetchStrategy}
     * @param element the element 
     */
    public ElementBinding(QName element) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    }

    public ElementBinding(QName element, Class<?> javaType) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    	setObjectCreator(new DefaultConstructor(javaType));
    }

    public ElementBinding(QName element, ICreator creator) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    	setObjectCreator(creator);
    }
    
    public ElementBinding setChild(IBinding childBinding) {
    	if (this.childBinding == null) {
    		throw new NullPointerException("Child IBinding must not be null when you explicitly set it");
    	}
    	this.childBinding = childBinding;
    	return this;
    }

    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        Object newJavaContext = null;
        if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
            QName element = staxReader.getName();
            if (isExpected(element)) {
            	newJavaContext = newInstance();
            	if (childBinding != null) {
            		childBinding.toJava(staxReader, select(javaContext, newJavaContext));
            	}
                setProperty(javaContext, newJavaContext);
            }
        }
        
        return newJavaContext;
    }
    
    @Override
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        
        //mixed content is not yet supported -- there are either child elements or there is content
        boolean isEmptyElement = this.childBinding == null;
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
        }
        
        if (this.childBinding != null) {
        	this.childBinding.toXml(staxWriter, getProperty(javaContext));
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
    }
    
    @Override
    public String toString() {
        return String.format("ElementBinding[element=%s]", getElement());
    }
    
}
