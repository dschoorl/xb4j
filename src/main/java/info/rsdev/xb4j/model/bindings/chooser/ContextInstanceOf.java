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
package info.rsdev.xb4j.model.bindings.chooser;

import info.rsdev.xb4j.model.java.JavaContext;

/**
 * A match is made when the current context object is an instance of the given java type
 *
 * @author Dave Schoorl
 */
public class ContextInstanceOf implements IChooser {

    private Class<?> instanceOf = null;

    /**
     * Create a new instance of {@link ContextInstanceOf}. This implementation of {@link IChooser} will match a choice when the type
     * of the current java context is an instance of this javaType
     *
     * @param javaType the type that the java context object must be an instance of have for the {@link IChooser} to match this choice
     */
    public ContextInstanceOf(Class<?> javaType) {
        if (javaType == null) {
            throw new NullPointerException("Class cannot be null");
        }
        this.instanceOf = javaType;
    }

    @Override
    public boolean matches(JavaContext javaContext) {
        if (javaContext.getContextObject() == null) {
            return false;
        }
        return this.instanceOf.isAssignableFrom(javaContext.getContextObject().getClass());
    }

    @Override
    public String toString() {
        return String.format("%s[type=%s]", getClass().getSimpleName(), this.instanceOf.getName());
    }
}
