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
package info.rsdev.xb4j.model.java.constructor;

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
        } catch (NoSuchMethodException e) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.defaultConstructor == null) ? 0 : this.defaultConstructor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DefaultConstructor other = (DefaultConstructor) obj;
		if (this.defaultConstructor == null) {
			if (other.defaultConstructor != null) return false;
		} else if (!this.defaultConstructor.equals(other.defaultConstructor)) return false;
		return true;
	}
    
}
