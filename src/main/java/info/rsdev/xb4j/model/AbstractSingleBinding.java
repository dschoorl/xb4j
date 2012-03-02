package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;

/**
 * 
 * @author Dave Schoorl
 */
public abstract class AbstractSingleBinding extends AbstractBindingBase implements ISingleBinding {
	
	private IBindingBase childBinding = null;
	
	public IBindingContainer setChild(IBindingContainer container) {
		setChild((IBindingBase)container);
		return container;
	}
	
	public SequenceBinding setChild(SequenceBinding container) {
		setChild((IBindingBase)container);
		return container;
	}
	
    public IBindingBase setChild(IBindingBase childBinding, IGetter getter, ISetter setter) {
    	setChild(childBinding);
    	childBinding.setGetter(getter);
        childBinding.setSetter(setter);
        
        return this;
    }
    
    /**
     * Convenience method, which adds a child binding, and navigating the object tree from parent to child is done through
     * the field with the given fieldname.
     * 
     * @param childBinding
     * @param fieldName
     * @return the childBinding
     */
    public IBindingBase setChild(IBindingBase childBinding, String fieldName) {
        if (fieldName == null) {
        	throw new NullPointerException("Fieldname cannot be null");
        }
        setChild(childBinding);
        FieldAccessProvider provider = new FieldAccessProvider(fieldName);
        childBinding.setGetter(provider);
        childBinding.setSetter(provider);
        
        return childBinding;
    }

    private IBindingBase setChild(IBindingBase childBinding) {
    	if (childBinding == null) {
    		throw new NullPointerException("Child IBinding must not be null when you explicitly set it");
    	}
        this.childBinding = childBinding;
        childBinding.setParent(this);   //maintain bidirectional relationship
        return childBinding;
    }
    
    protected IBindingBase getChildBinding() {
    	return this.childBinding;
    }
	
}
