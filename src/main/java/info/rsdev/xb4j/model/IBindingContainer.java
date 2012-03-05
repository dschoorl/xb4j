package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;

/**
 * group a number of bindings together
 * 
 * @author Dave Schoorl
 */
public interface IBindingContainer extends IBindingBase {
	
	public <T extends IBindingBase> T add(T childBinding);
	
	public <T extends IBindingBase> T add(T childBinding, IGetter getter, ISetter setter);
	
	public <T extends IBindingBase> T add(T childBinding, String fieldname);
	
}
