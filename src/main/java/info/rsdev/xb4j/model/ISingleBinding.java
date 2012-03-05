package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;

/**
 * The binding can have only one child
 * 
 * @author Dave Schoorl
 */
public interface ISingleBinding extends IBindingBase {
	
	public <T extends IBindingBase> T setChild(T childContainer);
	
	public <T extends IBindingBase> T setChild(T childBinding, IGetter getter, ISetter setter);
	
	public <T extends IBindingBase> T setChild(T childBinding, String fieldname);
	
}
