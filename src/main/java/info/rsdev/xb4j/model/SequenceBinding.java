package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

/**
 * Group a number of elements where ordering is fixed. Elements can be optional. When an element can occur more than once, you 
 * must wrap them inside a {@link CollectionBinding}. A sequence has no xml or java object eepresentation.
 * 
 * @author Dave Schoorl
 */
public class SequenceBinding extends AbstractBindingContainer {
	
	/**
	 * Create a new {@link SequenceBinding} which inherits it's element and javatype from it's parent
	 */
	public SequenceBinding() {
		setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
	}
	
	public SequenceBinding(Class<?> javaType) {
		setObjectCreator(new DefaultConstructor(javaType));
		setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
	}
    
}
