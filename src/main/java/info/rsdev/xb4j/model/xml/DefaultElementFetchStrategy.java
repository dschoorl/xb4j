package info.rsdev.xb4j.model.xml;

import info.rsdev.xb4j.model.IBinding;

import javax.xml.namespace.QName;

/**
 * Get the element stored in this strategy
 * 
 * @author Dave Schoorl
 */
public class DefaultElementFetchStrategy implements IElementFetchStrategy {

	private QName element = null;
	
	public DefaultElementFetchStrategy(QName element) {
		if (element == null) {
			throw new NullPointerException("QName must be provided");
		}
		this.element = element;
	}
	
	@Override
	public QName getElement(IBinding binding) {
		return this.element;
	}
	
}
