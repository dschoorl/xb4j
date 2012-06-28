package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public interface IAttribute {
	
	public QName getAttributeName();
	
	public void toJava(String valueAsText, Object javaContext) throws XMLStreamException;
	
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext, QName elementName) throws XMLStreamException;
	
	public Object getProperty(Object contextInstance);
	
	public boolean setProperty(Object contextInstance, Object propertyValue);
	
	public IAttribute setGetter(IGetter getter);
	
	public IAttribute setSetter(ISetter setter);
	
	public boolean isRequired();
	
	public IAttribute setRequired(boolean isRequired);
	
	public IAttribute setDefault(String defaultValue);
	
	public String getDefaultValue();
	
}