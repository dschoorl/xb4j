package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * These type of bindings always have an element they attach to. 
 * @author Dave Schoorl
 */
public class ConcreteBinding extends AbstractBinding implements IBinding {
	
	public ConcreteBinding(QName element) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
	}

	@Override
	public Object toJava(RecordAndPlaybackXMLStreamReader stream) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void toXml(SimplifiedXMLStreamWriter stream, Object javaContext) throws XMLStreamException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParent(IBinding parent) {
		// TODO Auto-generated method stub
		
	}
	
	
}
