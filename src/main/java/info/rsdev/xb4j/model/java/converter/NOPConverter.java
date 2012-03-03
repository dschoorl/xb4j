package info.rsdev.xb4j.model.java.converter;

import info.rsdev.xb4j.exceptions.Xb4jException;

/**
 * Convenience class. It does not perform any conversion; it only casts an Object to a String and a String to an Object.
 * When the Object is not a String, an exception is thrown. The existence of this class can make operations nullsafe, 
 * without performing a null check.
 * 
 * @author Dave Schoorl
 */
public class NOPConverter implements IValueConverter {
	
	public static final NOPConverter INSTANCE = new NOPConverter();
	
	@Override
	public Object toObject(String value) {
		if (value == null) { return null; }
		return value;
	}
	
	@Override
	public String toText(Object value) {
		if (value == null) { return null; }
		if (!(value instanceof String)) {
			throw new Xb4jException(String.format("Expected a %s, but encountered a %s", String.class.getName(), 
					value.getClass().getName()));
		}
		return (String)value;
	}
	
	public Class<?> getJavaType() {
	    return String.class;
	}
	
}
