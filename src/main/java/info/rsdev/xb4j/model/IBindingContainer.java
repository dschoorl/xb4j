package info.rsdev.xb4j.model;

/**
 * group a number of bindings together
 * 
 * @author Dave Schoorl
 */
public interface IBindingContainer extends IBinding {
	
	public IBindingContainer add(IBindingContainer childContainer);
	
	public IBinding add(IBinding childBinding);
	
}
