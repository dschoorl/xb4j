package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.FetchFromParentStrategy;

import javax.xml.namespace.QName;

/**
 * Group a number of elements where ordering is fixed. Elements can be optional or occur more than once. 
 * @author Dave Schoorl
 */
public class SequenceBinding extends AbstractBindingContainer implements IBindingContainer {
	
	/**
	 * Create a new {@link SequenceBinding} which inherits it's element and javatype from it's parent
	 */
	public SequenceBinding() {
		setElementFetchStrategy(new FetchFromParentStrategy(this));
	}
    
    /**
     * Create new {@link SequenceBinding}
     * 
     * @param element
     * @param instantiator
     */
    public SequenceBinding(QName element, ICreator instantiator) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    	setObjectCreator(instantiator);
    }
    
    /**
     * Create a new {@link AbstractBinding} where the javaType will be created with a {@link DefaultConstructor}
     * 
     * @param element
     * @param javaType
     */
    public SequenceBinding(QName element, Class<?> javaType) {
        this(element, new DefaultConstructor(javaType));
    }
    
}
