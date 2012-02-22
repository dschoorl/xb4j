package info.rsdev.xb4j.model;

/**
 * 
 * @author Dave Schoorl
 */
public interface Instantiator {
    
    public Object newInstance();
    
    /**
     * The Java type that this Instantiator will create with a call to {@link #newInstance()}
     * @return the Java type
     */
    public Class<?> getJavaType();
    
}
