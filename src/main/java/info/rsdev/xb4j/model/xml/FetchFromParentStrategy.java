package info.rsdev.xb4j.model.xml;

import info.rsdev.xb4j.model.IBinding;

import javax.xml.namespace.QName;

/**
 * Get the xml element from the parent binding
 * 
 * @author Dave Schoorl
 */
public class FetchFromParentStrategy implements IElementFetchStrategy {
    
    private IBinding thisBinding = null;
    
    /**
     * Create a new {@link FetchFromParentStrategy}
     * @param thisBinding the {@link IBinding} that owns this {@link FetchFromParentStrategy}
     */
    public FetchFromParentStrategy(IBinding thisBinding) {
        if (thisBinding == null) {
            throw new NullPointerException("IBinding cannot be null");
        }
        this.thisBinding = thisBinding;
    }

	@Override
	public QName getElement() {
		return getParentBinding().getElement();
	}
	
    private IBinding getParentBinding() {
    	IBinding parent = thisBinding.getParent();
    	if (parent == null) {
    		throw new NullPointerException("Parent is not set in ".concat(thisBinding.getClass().getName()));
    	}
    	return parent;
    }

}
