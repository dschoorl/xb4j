package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.IObjectFetchStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * 
 * @author Dave Schoorl
 */
public class FieldAccessProvider implements ISetter, IGetter {
	
	private String fieldName = null;
	
//	private IObjectFetchStrategy objectFetcher = null;
	
//	private Field field = null;
	
	public FieldAccessProvider(IObjectFetchStrategy objectFetcher, String fieldName) {
		this.fieldName = fieldName;
//		this.objectFetcher = objectFetcher;
	}
	
	public FieldAccessProvider(String fieldName) {
		this.fieldName = fieldName;
	}
	
	@Override
	public boolean set(Object contextInstance, Object propertyValue) {
		try {
			getField(contextInstance.getClass(), this.fieldName).set(contextInstance, propertyValue);
		} catch (Exception e) {
//			String fieldName = field==null?"null":field.getName();
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
//			String fieldName = field==null?"null":field.getName();
			throw new Xb4jException(String.format("Could not get field %s from %s", fieldName, contextInstance), e);
		}
	}
	
//	private Field getField() {
//		if (this.field == null) {
//			this.field = getField(this.objectFetcher.getJavaType(), this.fieldName);
//		}
//		return this.field;
//	}
	
	private Field getField(Class<?> context, String fieldName) {
		if (context == null) {
			throw new NullPointerException("Type must be provided");
		}
		if (fieldName == null) {
			throw new NullPointerException("The name of the Field must be provided");
		}
		
		Field targetField = null;
		Class<?> candidateClass = context;
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
					throw new IllegalStateException(String.format("Field '%s' is not definied in the entire class hierarchy of '%s'.",fieldName, context.getName()));
				}
			}
		}
		
		if (targetField != null) {
			if (!Modifier.isPublic(((Member)targetField).getModifiers()) || !Modifier.isPublic(((Member)targetField).getDeclaringClass().getModifiers())) {
				targetField.setAccessible(true);
			}
		}
		
		return targetField;
	}

//	@Override
//	public FieldAccessProvider setContext(Class<?> javaContext) {
//		if (javaContext != null) {
//			throw new NullPointerException("Context cannot be null");
//		}
//		//this only works when the fieldname is set at construction time and cannot be changed afterwards
//		if (this.field != null) {
//			if (this.field.getType().equals(javaContext)) {
//				return this; 
//			} else {
//				throw new Xb4jException(String.format("Cannot set Java context %s, because context is already set to %s", 
//						javaContext, this.field.getType()));
//			}
//		}
//		this.field = getField(javaContext, this.fieldName);
//		return this;
//	}

}
