package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.xml.InheritElementFetchStrategy;

import javax.xml.XMLConstants;

/**
 * <p>This type of binding get's it's element from it's parent container.</p>
 * 
 * @author Dave Schoorl
 */
public class ComplexTypeBinding extends AbstractGroupBinding implements IBinding {
    
    private String identifier = null;
    
    private String namespaceUri = null;
	
	public ComplexTypeBinding(String identifier, String namespaceUri) {
	    if (identifier == null) {
	        throw new NullPointerException("Identifier cannot be null");
	    }
	    this.identifier = identifier;
	    this.namespaceUri = namespaceUri==null?XMLConstants.NULL_NS_URI:namespaceUri;
    	setElementFetchStrategy(new InheritElementFetchStrategy(this));
	}
	
	public String getIdentifier() {
	    return this.identifier;
	}
	
	public String getNamespace() {
	    return this.namespaceUri;
	}
	
}
