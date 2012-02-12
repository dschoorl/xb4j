package info.rsdev.xb4j.model;

import java.io.Writer;

/**
 * The model knows how to map Java objects to a certain xml definition and visa versa. The metaphor used 
 * to bind xml and java, regardless of direction, is a binding? A binding always binds an element to a 
 * Java class. Every binding can marhalled or unmarshalled standalone.
 *  
 * 
 * @author Dave Schoorl
 */
public class BindingModel {

    /**
     * Marshall a Java instance into xml representation
     * 
     * @param writer
     * @param instance
     */
    public void toXml(Writer writer, Object instance) {
        
    }
    
    
}
