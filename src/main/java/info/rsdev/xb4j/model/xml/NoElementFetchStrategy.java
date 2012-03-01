package info.rsdev.xb4j.model.xml;

import javax.xml.namespace.QName;

/**
 * Suppress sending element to output in marshall process
 * 
 * @author Dave Schoorl
 */
public class NoElementFetchStrategy implements IElementFetchStrategy {
	
	public static final NoElementFetchStrategy INSTANCE = new NoElementFetchStrategy();

	/**
	 * Create a new {@link NoElementFetchStrategy}. This implementation of {@link IElementFetchStrategy} suppresses the
	 * output of an element to the xml stream
	 */
	private NoElementFetchStrategy() { }
	
	@Override
	public QName getElement() {
		return null;
	}
	
	@Override
	public String toString() {
	    return "NoElementFetchStrategy[suppress element]";
	}
}
