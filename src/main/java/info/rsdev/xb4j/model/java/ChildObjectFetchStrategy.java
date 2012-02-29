package info.rsdev.xb4j.model.java;

import info.rsdev.xb4j.model.AbstractBindingContainer;

/**
 * 
 * @author Dave Schoorl
 */
public class ChildObjectFetchStrategy implements IObjectFetchStrategy {
	
	private AbstractBindingContainer thisBinding = null;
	
	@Override
	public Class<?> getJavaType() {
		thisBinding.getChildren();
		return null;
	}
	
	@Override
	public Object newInstance() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
