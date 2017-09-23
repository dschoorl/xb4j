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
import info.rsdev.xb4j.model.bindings.IBinding;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    public Object newInstance(IBinding caller, RecordAndPlaybackXMLStreamReader staxReader) {
        Object instance = null;
        try {
            instance = defaultConstructor.newInstance();
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            throw new Xb4jException("Could not create instance", e);
        }
        return instance;
    }

    private Constructor<?> getDefaultConstructor(Class<?> javaType) {
        if (javaType == null) {
            throw new NullPointerException("Java type to create cannot be null");
        }
        Constructor<?> constructor = null;
        try {
            constructor = javaType.getDeclaredConstructor();
            if (!Modifier.isPublic(((Member) constructor).getModifiers())
                    || !Modifier.isPublic(((Member) constructor).getDeclaringClass().getModifiers())) {
                constructor.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            throw new Xb4jException(String.format("'%s' has no default constructor. Please add one to your code or use another "
                    + "way to construct the class.", javaType.getName()), e);
        }
        return constructor;
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultConstructor other = (DefaultConstructor) obj;
        if (this.defaultConstructor == null) {
            if (other.defaultConstructor != null) {
                return false;
            }
        } else if (!this.defaultConstructor.equals(other.defaultConstructor)) {
            return false;
        }
        return true;
    }

}
