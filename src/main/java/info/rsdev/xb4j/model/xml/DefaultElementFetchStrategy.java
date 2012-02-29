package info.rsdev.xb4j.model.xml;

import javax.xml.namespace.QName;

/**
 * Get the element stored in this strategy
 * 
 * @author Dave Schoorl
 */
public class DefaultElementFetchStrategy implements IElementFetchStrategy {

	private QName element = null;
	
	/**
	 * Create a new {@link DefaultElementFetchStrategy}. This implementation of {@link IElementFetchStrategy} is the simplest:
	 * the bound element is stored in this strategy
	 * @param element the element bound
	 */
	public DefaultElementFetchStrategy(QName element) {
		if (element == null) {
			throw new NullPointerException("QName must be provided");
		}
		this.element = element;
	}
	
	@Override
	public QName getElement() {
		return this.element;
	}
	
	@Override
	public String toString() {
	    return String.format("DefaultElementFetchStrategy[element=%s]", this.element);
	}
}
