package info.rsdev.xb4j.model.xml;

import info.rsdev.xb4j.model.IBinding;

import javax.xml.namespace.QName;

/**
 * Define how a binding knows what element it is bound to
 * 
 * @author Dave Schoorl
 */
public interface IElementFetchStrategy {
	
	public QName getElement(IBinding binding);
	
}
