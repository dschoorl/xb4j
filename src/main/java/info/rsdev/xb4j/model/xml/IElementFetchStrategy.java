package info.rsdev.xb4j.model.xml;

import javax.xml.namespace.QName;

/**
 * Define how a binding knows what element it is bound to
 * 
 * @author Dave Schoorl
 */
public interface IElementFetchStrategy {
	
	public QName getElement();
	
}
