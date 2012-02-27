package info.rsdev.xb4j.model.xml;

import info.rsdev.xb4j.model.IBinding;

import javax.xml.namespace.QName;

/**
 * Get the xml element from the parent binding
 * 
 * @author Dave Schoorl
 */
public class InheritElementFetchStrategy implements IElementFetchStrategy {
    
    private IBinding thisBinding = null;
    
    /**
     * Create a new {@link InheritElementFetchStrategy}
     * @param thisBinding the {@link IBinding} that owns this {@link InheritElementFetchStrategy}
     */
    public InheritElementFetchStrategy(IBinding thisBinding) {
        if (thisBinding == null) {
            throw new NullPointerException("IBinding cannot be null");
        }
        this.thisBinding = thisBinding;
    }

	@Override
	public QName getElement() {
		return thisBinding.getParent().getElement();
	}
	
}
