package info.rsdev.xb4j.model.java;

import info.rsdev.xb4j.model.IBinding;

public class InheritObjectFetchStrategy implements IObjectFetchStrategy {
    
    private IBinding thisBinding = null;
    
    public InheritObjectFetchStrategy(IBinding thisBinding) {
        if (thisBinding == null) {
            throw new NullPointerException("IBinding cannot be null");
        }
        this.thisBinding = thisBinding;
    }

    @Override
    public Class<?> getJavaType() {
        return thisBinding.getParent().getJavaType();
    }

    @Override
    public Object newInstance() {
        return thisBinding.getParent().newInstance();
    }

}
