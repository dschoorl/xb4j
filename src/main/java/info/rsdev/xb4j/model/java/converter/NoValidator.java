package info.rsdev.xb4j.model.java.converter;

import info.rsdev.xb4j.exceptions.ValidationException;

/**
 * This {@link IValidator} performs no validation and will never throw a {@link ValidationException}
 *  
 * @author Dave Schoorl
 */
public class NoValidator implements IValidator {
	
	/**
	 * Singleton version of this implementation
	 */
	public static final NoValidator INSTANCE = new NoValidator();
	
	/**
	 * Do not create a new instance every time, but reuse {@link #INSTANCE}
	 */
	private NoValidator() {}
	
	@Override
	public <T> T isValid(T instance) throws ValidationException {
		return instance;
	}
	
}
