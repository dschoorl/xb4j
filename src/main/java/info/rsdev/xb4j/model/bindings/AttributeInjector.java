package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.model.bindings.action.IMarshallingAction;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class allows you to attach an attribute to an xml element in the StAX stream, who's value is not present from 
 * the java context being unmarshalled. Instead, the value is provided by an {@link IMarshallingAction}.
 * 
 * @author Dave Schoorl
 */
public class AttributeInjector extends AbstractAttribute {
	
	private IMarshallingAction valueProvider = null;
	
	/**
	 * Create a new {@link AttributeInjector}
	 * @param element
	 * @param valueProvider
	 */
	public AttributeInjector(QName element, IMarshallingAction valueProvider) {
		super(element);
		setMarshallingAction(valueProvider);
	}
	
	@Override
	public void toJava(String valueAsText, JavaContext javaContext) throws XMLStreamException {
		//do nothing -- swallow xml attribute
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext, QName elementName) throws XMLStreamException {
        QName attributeName = getAttributeName();
        String value = getValue(javaContext);
        if (isRequired() || (value != null)) {
       		staxWriter.writeAttribute(elementName, attributeName, value);
        }
	}
	
	@Override
	public IAttribute setDefault(String defaultValue) {
		return this;
	}
	
	@Override
	public String getDefaultValue() {
		return null;	//not supported by an Injector
	}
	
    private void setMarshallingAction(IMarshallingAction valueProvider) {
		if (valueProvider == null) {
			throw new NullPointerException("IMarshallingAction cannot be null");
		}
		this.valueProvider  = valueProvider;
	}

	@Override
	public String getValue(JavaContext javaContext) {
		return valueProvider.execute(getProperty(javaContext));
	}

}
