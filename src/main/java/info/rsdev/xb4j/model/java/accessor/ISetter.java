package info.rsdev.xb4j.model.java.accessor;

import info.rsdev.xb4j.model.IBinding;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;

/**
 * An {@link ISetter} is used during the unmarshalling process (from xml to java). It will set the result that was 
 * returned from the {@link IBinding#toJava(RecordAndPlaybackXMLStreamReader)} method of a childbinding in the 
 * current javaContext.
 * 
 * @author Dave Schoorl
 */
public interface ISetter {
	
	boolean set(Object javaContext, Object propertyValue);
	
}
