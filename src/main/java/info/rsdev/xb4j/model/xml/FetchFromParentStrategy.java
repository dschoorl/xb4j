package info.rsdev.xb4j.model.xml;

import info.rsdev.xb4j.model.IBindingBase;

import javax.xml.namespace.QName;

/**
 * Get the xml element from the parent binding
 * 
 * @author Dave Schoorl
 */
public class FetchFromParentStrategy implements IElementFetchStrategy {
    
    private IBindingBase thisBinding = null;
    
    /**
     * Create a new {@link FetchFromParentStrategy}
     * @param thisBinding the {@link IBindingBase} that owns this {@link FetchFromParentStrategy}
     */
    public FetchFromParentStrategy(IBindingBase thisBinding) {
        if (thisBinding == null) {
            throw new NullPointerException("IBinding cannot be null");
        }
        this.thisBinding = thisBinding;
    }

	@Override
	public QName getElement() {
		return getParentBinding().getElement();
	}
	
    private IBindingBase getParentBinding() {
    	IBindingBase parent = thisBinding.getParent();
    	if (parent == null) {
    		throw new NullPointerException("Parent is not set in ".concat(thisBinding.getClass().getName()));
    	}
    	return parent;
    }

}
