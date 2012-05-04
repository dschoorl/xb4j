/* Copyright 2012 Red Star Development / Dave Schoorl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.xb4j.model.java.accessor;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.lang.reflect.Method;

import javax.lang.model.SourceVersion;

/**
 * Get or set the value of a class property by accessing it bean-style: by getter and setter methods. Those methods need not be 
 * public.
 * 
 * @author Dave Schoorl
 */
public class BeanPropertyAccessor implements ISetter, IGetter {
	
	private String propertyName = null;
	
	public BeanPropertyAccessor(String propertyName) {
		this.propertyName = validate(propertyName);
	}
	
	@Override
	public boolean set(Object contextInstance, Object propertyValue) {
		try {
			getSetter(contextInstance.getClass(), this.propertyName).invoke(contextInstance, propertyValue);
			return true;
		} catch (Exception e) {
			throw new Xb4jException(String.format("Could not set field '%s' with value '%s' in object '%s'", 
			        propertyName, propertyValue, contextInstance), e);
		}
	}
	
	@Override
	public Object get(Object contextInstance) {
		try {
			return getGetter(contextInstance.getClass(), this.propertyName).invoke(contextInstance);
		} catch (Exception e) {
			throw new Xb4jException(String.format("Could not get field '%s' from object %s", propertyName, contextInstance), e);
		}
	}
	
	
	private Method getGetter(Class<?> contextType, String propertyName) {
		if (contextType == null) {
			throw new NullPointerException("Type must be provided");
		}
		//TODO: implement
		return null;
	}
	
	
	private Method getSetter(Class<?> contextType, String propertyName) {
		if (contextType == null) {
			throw new NullPointerException("Type must be provided");
		}
		//TODO: implement
		return null;
	}
	
//	private Field getField(Class<?> contextType, String fieldName) {
//		if (contextType == null) {
//			throw new NullPointerException("Type must be provided");
//		}
//		if (fieldName == null) {
//			throw new NullPointerException("The name of the Field must be provided");
//		}
//		
//		Field targetField = null;
//		Class<?> candidateClass = contextType;
//		while (targetField == null) {
//			for (Field candidate: candidateClass.getDeclaredFields()) {
//				if (candidate.getName().equals(fieldName)) {
//					targetField = candidate;
//					break;
//				}
//			}
//			if (targetField ==  null) {
//				candidateClass = candidateClass.getSuperclass();
//				if (candidateClass == null) {
//					throw new IllegalStateException(String.format("Field '%s' is not definied in the entire class hierarchy " +
//							"of '%s'.",fieldName, contextType.getName()));
//				}
//			}
//		}
//		
//		if (!Modifier.isPublic(((Member)targetField).getModifiers()) || !Modifier.isPublic(((Member)targetField).getDeclaringClass().getModifiers())) {
//			targetField.setAccessible(true);
//		}
//		//TODO: check if the field is final? warn if static?
//		
//		return targetField;
//	}
	
	private String validate(String propertyName) {
		if (!SourceVersion.isIdentifier(propertyName)) {
			throw new IllegalArgumentException(String.format("Not a valid name for a property: %s", propertyName));
		}
		return propertyName;
	}
	
	@Override
	public String toString() {
	    return String.format("%s[property=%s]", getClass().getSimpleName(), propertyName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.propertyName == null) ? 0 : this.propertyName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		BeanPropertyAccessor other = (BeanPropertyAccessor) obj;
		if (this.propertyName == null) {
			if (other.propertyName != null) return false;
		} else if (!this.propertyName.equals(other.propertyName)) return false;
		return true;
	}
	
}
