package info.rsdev.xb4j.exceptions;

/**
 *
 * @author Dave Schoorl
 */
public class Xb4jException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public Xb4jException(String message) {
        super(message);
    }
    
    public Xb4jException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
