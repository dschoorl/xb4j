package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;

/**
 * An {@link IGetter} is used during the marshalling process (from java to xml). It extracts from the current 
 * javaContext the new javaContext that will be pushed downwards in the binding hierarchy through the 
 * {@link IBinding#toXml(SimplifiedXMLStreamWriter, Object)} method.
 * 
 * @author Dave Schoorl
 */
public interface IGetter {
	
	public Object get(Object javaContext);
	
}
