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

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.JavaContext;

/**
 * Set a value on a parent object by calling a setter method. The setter must take a single parameter that must accept the type of
 * the value to set.
 * 
 * @author Dave Schoorl
 */
public class MethodSetter extends AbstractMethodAccessor implements ISetter {
    
    private Logger logger = LoggerFactory.getLogger(MethodSetter.class);
    
    public MethodSetter(String methodname) {
        super(methodname);
    }
    
    @Override
    public boolean set(JavaContext javaContext, Object propertyValue) {
        Class<?> parameterType = (propertyValue == null ? null : propertyValue.getClass());
        Method method = getMethod(javaContext.getContextObject().getClass(), this.methodname, parameterType);
        try {
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("[MethodSetter] Set value '%s' in object '%s' through method '%s'", propertyValue,
                        javaContext.getContextObject(), this.methodname));
            }
            method.invoke(javaContext.getContextObject(), propertyValue);
            return true;
        } catch (RuntimeException e) {
            throw e; // to signal FindBugs that I consciously do not handle RuntimeExceptions
        } catch (Exception e) {
            throw new Xb4jException(String.format("Could not set value '%s' in object '%s' through method '%s'", propertyValue,
                    javaContext.getContextObject(), this.methodname));
        }
    }
    
    @Override
    public String toString() {
        return "MethodSetter[methodname=".concat(methodname).concat("]");
    }
    
}
