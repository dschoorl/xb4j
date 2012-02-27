package info.rsdev.xb4j.model.xml;

import info.rsdev.xb4j.model.IBinding;

import javax.xml.namespace.QName;

/**
 * Get the element to use from the parent
 * 
 * @author Dave Schoorl
 */
public class InheritElementFetchStrategy implements IElementFetchStrategy {

	@Override
	public QName getElement(IBinding binding) {
		return binding.getParent().getElement();
	}
	
}
