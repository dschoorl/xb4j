package info.rsdev.xb4j.model.java.constructor;

import info.rsdev.xb4j.model.IBinding;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;

/**
 * An {@link ICreator} is used during the unmarshalling process (from xml to java). It creates a new javaContext for 
 * the IBinding that it is owned by. Any {@link ISetter} that is defined on the binding will be applied to this new 
 * javaContext. The new javaContext is returned to the parent binding by the {@link IBinding#toJava(RecordAndPlaybackXMLStreamReader)} 
 * method.
 * 
 * @author Dave Schoorl
 */
public interface ICreator {
    
    public Object newInstance();
    
    /**
     * The Java type that this {@link ICreator} will create with a call to {@link #newInstance()}
     * @return the Java type
     */
    public Class<?> getJavaType();
    
}
