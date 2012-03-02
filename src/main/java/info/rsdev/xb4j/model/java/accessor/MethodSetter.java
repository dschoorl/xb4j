package info.rsdev.xb4j.model.java.accessor;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Set a value on a parent object by calling a setter method. The setter must take a single parameter that must accept the
 * type of the value to set.
 * 
 * @author Dave Schoorl
 */
public class MethodSetter implements ISetter {
    
    private String methodname = null;
    
    public MethodSetter(String methodname) {
        if (methodname == null) {
            throw new NullPointerException("Methodname cannot be null");
        }
        this.methodname = validate(methodname);
    }

    @Override
    public boolean set(Object javaContext, Object propertyValue) {
      	Class<?> parameterType = (propertyValue==null?null:propertyValue.getClass());
      	Method method = getMethod(javaContext.getClass(), this.methodname, parameterType);
        try {
            method.invoke(javaContext, propertyValue);
        } catch (RuntimeException e) {
        	throw e;	//to signal FindBugs that I consciously do not handle RuntimeExceptions
        } catch (Exception e) {
            throw new Xb4jException(String.format("Could not set value '%s' in object '%s' through method '%s'", 
                    propertyValue, javaContext, this.methodname));
        }
        return false;
    }
    
    private Method getMethod(Class<?> objectType, String methodName, Class<?> parameterType) {
        if (objectType == null) {
            throw new NullPointerException("Type cannot be null");
        }
        if (methodName == null) {
            throw new NullPointerException("The name of the Method cannot be null");
        }
        
        Method targetMethod = null;
        Class<?> candidateClass = objectType;
        while (targetMethod == null) {
            for (Method candidate: candidateClass.getDeclaredMethods()) {
                if (candidate.getName().equals(methodName)) {
                	Class<?>[] parameters = candidate.getParameterTypes();
                	if (parameters.length == 1) {
                		if ((parameterType != null) && !parameters[0].isAssignableFrom(parameterType)) { 
                			continue;	//not the right parametertype: look at the next methods
                		}
                		targetMethod = candidate;
                		break;
                	}
                }
            }
            
            if (targetMethod ==  null) {
                candidateClass = candidateClass.getSuperclass();
                if (candidateClass == null) {
                    throw new Xb4jException(String.format("Method '%s', taking single parameter of type '%s', is not " +
                    		"definied in the entire class hierarchy of '%s'.", methodName, parameterType, objectType.getName()));
                }
            }
        }
        
        if (!Modifier.isPublic(((Member)targetMethod).getModifiers()) || !Modifier.isPublic(((Member)targetMethod).getDeclaringClass().getModifiers())) {
            targetMethod.setAccessible(true);
        }
        
        return targetMethod;
    }
    
    private String validate(String methodname) {
        return methodname;
    }

}
