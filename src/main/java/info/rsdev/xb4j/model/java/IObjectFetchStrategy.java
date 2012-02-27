package info.rsdev.xb4j.model.java;

/**
 * Define how a binding knows what java object it is bound to
 * 
 * @author Dave Schoorl
 */
public interface IObjectFetchStrategy {
    
    public Class<?> getJavaType();
    
    public Object newInstance();

}
