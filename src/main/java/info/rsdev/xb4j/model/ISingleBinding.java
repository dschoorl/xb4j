package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;

/**
 * The binding can have only one child
 * 
 * @author Dave Schoorl
 */
public interface ISingleBinding extends IBindingBase {
	
	public IBindingBase setChild(IBindingBase childContainer);
	
	public IBindingBase setChild(IBindingBase childBinding, IGetter getter, ISetter setter);
	
	public IBindingBase setChild(IBindingBase childBinding, String fieldname);
	
}
