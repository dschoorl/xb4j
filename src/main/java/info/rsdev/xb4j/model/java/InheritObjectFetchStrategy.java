package info.rsdev.xb4j.model.java;

import info.rsdev.xb4j.model.IBinding;

public class InheritObjectFetchStrategy implements IObjectFetchStrategy {
    
    private IBinding thisBinding = null;
    
    /**
     * Create a new {@link InheritObjectFetchStrategy}
     * @param thisBinding
     */
    public InheritObjectFetchStrategy(IBinding thisBinding) {
        if (thisBinding == null) {
            throw new NullPointerException("IBinding cannot be null");
        }
        this.thisBinding = thisBinding;
    }

    @Override
    public Class<?> getJavaType() {
        return getParentBinding().getJavaType();
    }

    @Override
    public Object newInstance() {
        return getParentBinding().newInstance();
    }
    
    private IBinding getParentBinding() {
    	IBinding parent = thisBinding.getParent();
    	if (parent == null) {
    		throw new NullPointerException("Parent is not set in ".concat(thisBinding.getClass().getName()));
    	}
    	return parent;
    }

}
