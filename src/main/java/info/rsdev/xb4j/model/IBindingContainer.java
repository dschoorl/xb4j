package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;

/**
 * group a number of bindings together
 * 
 * @author Dave Schoorl
 */
public interface IBindingContainer extends IBindingBase {
	
	public IBindingBase add(IBindingBase childBinding);
	
	public IBindingBase add(IBindingBase childBinding, IGetter getter, ISetter setter);
	
	public IBindingBase add(IBindingBase childBinding, String fieldname);
	
}
