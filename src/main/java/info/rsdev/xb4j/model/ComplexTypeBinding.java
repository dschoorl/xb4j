package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.InheritElementFetchStrategy;

import javax.xml.stream.XMLStreamException;

/**
 * <p>This type of binding get's it's element from it's parent container.</p>
 * 
 * @author Dave Schoorl
 */
public class ComplexTypeBinding extends AbstractBinding implements IBinding {
	
	public ComplexTypeBinding() {
    	setElementFetchStrategy(new InheritElementFetchStrategy());
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
	
}
