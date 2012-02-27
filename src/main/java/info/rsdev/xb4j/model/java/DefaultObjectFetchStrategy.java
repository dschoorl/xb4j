package info.rsdev.xb4j.model.java;

import info.rsdev.xb4j.model.DefaultConstructor;
import info.rsdev.xb4j.model.Instantiator;

/**
 * 
 * @author Dave Schoorl
 */
public class DefaultObjectFetchStrategy implements IObjectFetchStrategy {

    private Instantiator instantiator = null;
    
    public DefaultObjectFetchStrategy(Class<?> javaType) {
        this.instantiator = new DefaultConstructor(javaType);
    }
    
    public DefaultObjectFetchStrategy(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    @Override
    public Class<?> getJavaType() {
        return instantiator.getJavaType();
    }
    
    @Override
    public Object newInstance() {
        return instantiator.newInstance();
    }
}
