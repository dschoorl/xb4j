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
import info.rsdev.xb4j.model.java.JavaContext;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.SourceVersion;

/**
 * Get or set the value of a class property by accessing it bean-style: by getter and setter methods. Those methods need not be 
 * public.
 * 
 * @author Dave Schoorl
 */
public class BeanPropertyAccessor implements ISetter, IGetter {
	
    private static final Map<Class<?>, Class<?>> autoboxingSupport = new HashMap<Class<?>, Class<?>>(8);
    static {
        autoboxingSupport.put(byte.class, Byte.class);
        autoboxingSupport.put(short.class, Short.class);
        autoboxingSupport.put(int.class, Integer.class);
        autoboxingSupport.put(long.class, Long.class);
        autoboxingSupport.put(double.class, Double.class);
        autoboxingSupport.put(float.class, Float.class);
        autoboxingSupport.put(char.class, Character.class);
        autoboxingSupport.put(boolean.class, Boolean.class);
    }
    
	private String propertyName = null;
	
	public BeanPropertyAccessor(String propertyName) {
		this.propertyName = validate(propertyName);
	}
	
	@Override
	public boolean set(JavaContext javaContext, Object propertyValue) {
		try {
		    Class<?> parameterType = (propertyValue == null?null:propertyValue.getClass());
			Method setter = getSetter(javaContext.getContextObject().getClass(), this.propertyName, parameterType);
			setter.invoke(javaContext.getContextObject(), propertyValue);
			return true;
		} catch (Exception e) {
			throw new Xb4jException(String.format("Could not set field '%s' with value '%s' in object '%s'", 
			        propertyName, propertyValue, javaContext), e);
		}
	}
	
	@Override
	public JavaContext get(JavaContext javaContext) {
		try {
			Object contextObject = javaContext.getContextObject();
			Object newContextObject = getGetter(contextObject.getClass(), this.propertyName).invoke(contextObject);
			return javaContext.newContext(newContextObject);
		} catch (Exception e) {
			throw new Xb4jException(String.format("Could not get field '%s' from object %s", propertyName, javaContext), e);
		}
	}
	
	
	private String validate(String propertyName) {
		if (!SourceVersion.isIdentifier(propertyName)) {
			throw new IllegalArgumentException(String.format("Not a valid name for a property: %s", propertyName));
		}
		return propertyName;
	}
	
    private Method getGetter(Class<?> contextType, String propertyName) {
        Method targetGetter = null;
        String capitalizedPropertyName = capitalize(propertyName);
        Class<?> candidateClass = contextType;
        while ((candidateClass != null) && (targetGetter == null)) {
            for (Method method : candidateClass.getDeclaredMethods()) {
                String methodName = method.getName();
                if (methodName.endsWith(capitalizedPropertyName)) {
                    if (method.getParameterTypes().length == 0) {
                        Class<?> returnType = method.getReturnType();
                        if (((returnType.equals(Boolean.class) || returnType.equals(boolean.class)) &&
                                methodName.equals("is".concat(capitalizedPropertyName))) ||
                                methodName.equals("get".concat(capitalizedPropertyName))) {
                            targetGetter = method;
                            break;
                        }
                    }
                }
            }
            if (targetGetter == null) {
                candidateClass = candidateClass.getSuperclass();
            }
        }
        if (targetGetter != null) {
            setAccessible(targetGetter);
        }
        return targetGetter;
    }
    
    private Method getSetter(Class<?> contextType, String propertyName, Class<?> parameterType) throws Exception {
        Method targetSetter = null;
        String setterName = "set".concat(capitalize(propertyName));
        Class<?> candidateClass = contextType;
        while ((candidateClass != null) && (targetSetter == null)) {
            for (Method method : candidateClass.getDeclaredMethods()) {
                if (method.getName().equals(setterName)) {
                    Class<?>[] methodParameterTypes = method.getParameterTypes();    
                    if (methodParameterTypes.length == 1) {
                        Class<?> declaredParameterType = methodParameterTypes[0];
                        if (declaredParameterType.isPrimitive() && (parameterType == null)) {
                            break;    //a primitive value can not be set to null: no match
                        }
                        
                        if ((declaredParameterType.isPrimitive()) && (!declaredParameterType.equals(void.class))) {
                            declaredParameterType = autoboxingSupport.get(declaredParameterType);
                        }
                        
                        if ((parameterType == null) || declaredParameterType.isAssignableFrom(parameterType)) {
                            targetSetter = method;  //exact match, taking autoboxing into account
                            break;
                        }
                    }
                }
            }
            if (targetSetter == null) {
                candidateClass = candidateClass.getSuperclass();
            }
        }
        if (targetSetter != null) {
            setAccessible(targetSetter);
        }
        return targetSetter;
    }
    
    private void setAccessible(AccessibleObject member) {
        if (!Modifier.isPublic(((Member)member).getModifiers()) || !Modifier.isPublic(((Member)member).getDeclaringClass().getModifiers())) {
            member.setAccessible(true);
        }
    }
    
    private String capitalize(String propertyName) {
        StringBuffer capitalizedPropertyName = new StringBuffer(propertyName.trim());
        capitalizedPropertyName.setCharAt(0, Character.toUpperCase(capitalizedPropertyName.charAt(0)));
        return capitalizedPropertyName.toString();
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
