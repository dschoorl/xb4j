package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Create an Java object by calling it's default constructor
 * 
 * @author Dave Schoorl
 */
public class DefaultConstructor implements ICreator {
    
    private Constructor<?> defaultConstructor = null;
    
    public DefaultConstructor(Class<?> javaType) {
        this.defaultConstructor = getDefaultConstructor(javaType);
    }

    @Override
    public Object newInstance() {
        Object instance = null;
        try {
            instance = defaultConstructor.newInstance();
        } catch (Exception e) {
            throw new Xb4jException("Could not create instance", e);
        }
        return instance;
    }
    
    private Constructor<?> getDefaultConstructor(Class<?> javaType) {
        Constructor<?> defaultConstructor = null;
        try {
            defaultConstructor = javaType.getDeclaredConstructor();
            if (!Modifier.isPublic(((Member)defaultConstructor).getModifiers()) || 
                    !Modifier.isPublic(((Member)defaultConstructor).getDeclaringClass().getModifiers())) {
                defaultConstructor.setAccessible(true);
            }
        } catch (Exception e) {
            throw new Xb4jException("Can not obtain a default constructor", e);
        }
        return defaultConstructor;
    }

    @Override
    public Class<?> getJavaType() {
        return defaultConstructor.getDeclaringClass();
    }
    
    @Override
    public String toString() {
        return String.format("DefaultConstructor[type=%s]", getJavaType());
    }

}
