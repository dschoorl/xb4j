package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Set a value on a parent object by calling this setter method
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
        try {
            getMethod(javaContext.getClass(), this.methodname).invoke(javaContext, propertyValue);
        } catch (Exception e) {
            throw new Xb4jException(String.format("Could not set value '%s' in object '%s' through method '%'", 
                    propertyValue, javaContext, this.methodname));
        }
        return false;
    }
    
    private Method getMethod(Class<?> contextType, String methodName) {
        if (contextType == null) {
            throw new NullPointerException("Type cannot be null");
        }
        if (methodName == null) {
            throw new NullPointerException("The name of the Method cannot be null");
        }
        
        Method targetMethod = null;
        Class<?> candidateClass = contextType;
        while ((candidateClass != null) && (targetMethod == null)) {
            for (Method candidate: candidateClass.getDeclaredMethods()) {
                if (candidate.getName().equals(methodName)) {
                    targetMethod = candidate;   //TODO: check parameters
                    break;
                }
            }
            
            if (targetMethod ==  null) {
                candidateClass = candidateClass.getSuperclass();
                if (candidateClass == null) {
                    throw new IllegalStateException(String.format("Method '%s' is not definied in the entire class hierarchy " +
                            "of '%s'.",methodName, contextType.getName()));
                }
            }
        }
        
        if (targetMethod != null) {
            if (!Modifier.isPublic(((Member)targetMethod).getModifiers()) || !Modifier.isPublic(((Member)targetMethod).getDeclaringClass().getModifiers())) {
                targetMethod.setAccessible(true);
            }
        }
        
        return targetMethod;
    }
    
    private String validate(String methodname) {
        return methodname;
    }

}
