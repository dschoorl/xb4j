package info.rsdev.xb4j.model.java.converter;

import info.rsdev.xb4j.exceptions.ValidationException;

/**
 * 
 * @author Dave Schoorl
 */
public interface IValidator {
	
	/**
	 * Check if the provided instance is valid, and returns the supplied instance when it is. If the instance is not
	 * valid, a {@link ValidationException} will be thrown.
	 * @param instance the value to validate
	 * @return the supplied instance, unmodified.
	 * @throws ValidationException when the instance is not valid
	 */
	public <T> T isValid(T instance) throws ValidationException ;
	
}
