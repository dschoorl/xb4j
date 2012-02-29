package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import javax.lang.model.SourceVersion;

/**
 * Get or set the value of a class property by accessing it's {@link Field} by fieldname
 * @author Dave Schoorl
 */
public class FieldAccessProvider implements ISetter, IGetter {
	
	private String fieldName = null;
	
	public FieldAccessProvider(String fieldName) {
		this.fieldName = validate(fieldName);
	}
	
	@Override
	public boolean set(Object contextInstance, Object propertyValue) {
		try {
			getField(contextInstance.getClass(), this.fieldName).set(contextInstance, propertyValue);
		} catch (Exception e) {
			throw new Xb4jException(String.format("Could not set field '%s' with value '%s' in object '%s'", 
			        fieldName, propertyValue, contextInstance), e);
		}
		return true;
	}
	
	@Override
	public Object get(Object contextInstance) {
		try {
			return getField(contextInstance.getClass(), this.fieldName).get(contextInstance);
		} catch (Exception e) {
			throw new Xb4jException(String.format("Could not get field '%s' from %s", fieldName, contextInstance), e);
		}
	}
	
	private Field getField(Class<?> contextType, String fieldName) {
		if (contextType == null) {
			throw new NullPointerException("Type must be provided");
		}
		if (fieldName == null) {
			throw new NullPointerException("The name of the Field must be provided");
		}
		
		Field targetField = null;
		Class<?> candidateClass = contextType;
		while ((candidateClass != null) && (targetField == null)) {
			for (Field candidate: candidateClass.getDeclaredFields()) {
				if (candidate.getName().equals(fieldName)) {
					targetField = candidate;
					break;
				}
			}
			if (targetField ==  null) {
				candidateClass = candidateClass.getSuperclass();
				if (candidateClass == null) {
					throw new IllegalStateException(String.format("Field '%s' is not definied in the entire class hierarchy " +
							"of '%s'.",fieldName, contextType.getName()));
				}
			}
		}
		
		if (targetField != null) {
			if (!Modifier.isPublic(((Member)targetField).getModifiers()) || !Modifier.isPublic(((Member)targetField).getDeclaringClass().getModifiers())) {
				targetField.setAccessible(true);
			}
			//TODO: check if the field is final? warn if static?
		}
		
		return targetField;
	}
	
	private String validate(String fieldName) {
		if (!SourceVersion.isIdentifier(fieldName)) {
			throw new IllegalArgumentException(String.format("Not a valid name for a field: %s", fieldName));
		}
		return fieldName;
	}
	
	@Override
	public String toString() {
	    return String.format("FieldAccessProvider[field=%s]", fieldName);
	}

}
