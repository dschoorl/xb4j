package info.rsdev.xb4j.model.java.accessor;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class AbstractMethodAccessor {
	
    protected final String methodname;
    
    public AbstractMethodAccessor(String methodname) {
        if (methodname == null) {
            throw new NullPointerException("Methodname cannot be null");
        }
        this.methodname = validate(methodname);
    }

	protected Method getMethod(Class<?> objectType, String methodName, Class<?>... parameterTypes) {
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
                	if (parameters.length == parameterTypes.length) {
                		for (int i=0; i<parameters.length; i++) {
                    		if ((parameterTypes[i] != null) && !parameters[i].isAssignableFrom(parameterTypes[i])) { 
                    			continue;	//not the right parametertype: look at the next methods
                    		}
                		}
                		targetMethod = candidate;
                		break;
                	}
                }
            }
            
            if (targetMethod == null) {
                candidateClass = candidateClass.getSuperclass();
                if (candidateClass == null) {
                    throw new Xb4jException(String.format("Method '%s', taking parametertype(s) '%s', is not defined in the " +
                    		"entire class hierarchy of '%s'.", methodName, Arrays.asList(parameterTypes), objectType.getName()));
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
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.methodname == null) ? 0 : this.methodname.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AbstractMethodAccessor other = (AbstractMethodAccessor) obj;
		if (this.methodname == null) {
			if (other.methodname != null) return false;
		} else if (!this.methodname.equals(other.methodname)) return false;
		return true;
	}
    
}
