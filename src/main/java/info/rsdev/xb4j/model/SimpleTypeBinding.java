package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.converter.IValueConverter;
import info.rsdev.xb4j.model.java.converter.NOPConverter;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;

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
public class SimpleTypeBinding extends AbstractBindingBase {
	
	private IValueConverter converter = NOPConverter.INSTANCE;
    
    /**
     * Create a new {@link SimpleTypeBinding} with a {@link DefaultElementFetchStrategy}
     * @param element the element 
     */
    public SimpleTypeBinding(QName element) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    }

    public SimpleTypeBinding(QName element, IValueConverter converter) {
    	setConverter(converter);
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    }

    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        Object value = null;
        if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
            QName element = staxReader.getName();
            if (isExpected(element)) {
                value = this.converter.toObject(staxReader.getElementText());
                setProperty(javaContext, value);
            }
        }
        
        return value;
    }
    
    @Override
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        QName element = getElement();
        
        staxWriter.writeElement(element, false);
        
        Object elementValue = getProperty(javaContext);
        if (elementValue != null) {
            staxWriter.writeContent(this.converter.toText(elementValue));
        }
        
        staxWriter.closeElement(element);
    }
    
    private void setConverter(IValueConverter converter) {
    	if (converter == null) {
    		throw new NullPointerException("IValueConverter cannot be null");
    	}
    	this.converter = converter;
    }

    @Override
    public String toString() {
        return String.format("SimpleTypeBinding[element=%s]", getElement());
    }
    
}
