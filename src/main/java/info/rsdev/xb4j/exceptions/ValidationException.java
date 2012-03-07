package info.rsdev.xb4j.exceptions;

/**
 * Indicates that a value is not valid
 * 
 * @author dschoorl
 */
public class ValidationException extends Xb4jException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String message) {
		super(message);
	}
	
	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
