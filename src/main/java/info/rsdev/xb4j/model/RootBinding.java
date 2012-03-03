package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * <p>This Binding is at the root of a binding hierarchy. It has a reference to the {@link BindingModel}, so
 * it can lookup {@link ComplexTypeBinding complextype definitions}.</p>
 * 
 * TODO: set schema on the root type to use validation on stax reader/writer?
 * 
 * @author Dave Schoorl
 */
public class RootBinding extends ElementBinding implements IModelAware {
	
    private BindingModel model = null;
    
	public RootBinding(QName element, Class<?> javaType) {
	    if (javaType == null) {
	        throw new NullPointerException("Java type cannot be null");
	    }
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    	setObjectCreator(new DefaultConstructor(javaType));
    	super.setOptional(false);
	}
	
	public ComplexTypeBinding getComplexType(String identifier, String namespaceUri) {
	    if (namespaceUri == null) { namespaceUri = XMLConstants.NULL_NS_URI; }
	    ComplexTypeBinding complexType = this.model.getComplexType(identifier, namespaceUri);
        if (complexType == null) {
            throw new Xb4jException(String.format("ComplexTypeBinding with identifier=%s and namespace=%s is not" +
                    "registered in the BindingModel", identifier, namespaceUri));
        }

	    return complexType;
	}
	
    @Override
	public void setModel(BindingModel model) {
	    if (model == null) {
	        throw new NullPointerException("BindingModel cannot be null");
	    }
	    if ((this.model != null) && !this.model.equals(model)) {
	        throw new IllegalArgumentException("It is currently not supported that a RootBinding is added to multiple BindingModels");
	    }
	    this.model = model;
	}
	
    @Override
	public BindingModel getModel() {
	    return this.model;
	}
	
	@Override
	public IBindingBase setOptional(boolean isOptional) {
		throw new Xb4jException("A RootBinding cannot be made optional");
	}
	
    @Override
    public String toString() {
        String fqClassName = getClass().getName();
        int dotIndex = Math.max(0, fqClassName.lastIndexOf('.') + 1);
        return String.format("%s[element=%s, javaType=%s]", fqClassName.substring(dotIndex), getElement(), getJavaType().getName());
    }
}
