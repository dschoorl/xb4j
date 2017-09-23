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

import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.FieldAccessor;
import info.rsdev.xb4j.model.java.accessor.IGetter;

import java.util.Collection;

/**
 * Follow the option coupled with this {@link CollectionNotEmptyChooser}, when the java context has a matching property that is a
 * collection which has at least one element. This {@link IChooser} implementation is null safe.
 *
 * @author Dave Schoorl
 */
public class CollectionNotEmptyChooser implements IChooser {

    private IGetter propertyAccessor = null;

    /**
     * Create a new {@link CollectionNotEmptyChooser} instance that will match the coupled option from the {@link Choice} binding
     * with the java context when the context object has a field with the given fieldName that is a {@link Collection} with at least
     * one element (not empty).
     *
     * @param fieldName the name of the field that should be a non-empty collection for this {@link IChooser} to match the java
     * context at hand
     */
    public CollectionNotEmptyChooser(String fieldName) {
        this.propertyAccessor = new FieldAccessor(fieldName);
    }

    @Override
    public boolean matches(JavaContext javaContext) {
        /* When the javaContext is null, we cannot establish the Object has the requested field 
		 * and thus we respond with false */
        if (javaContext.getContextObject() == null) {
            return false;
        }
        Object fieldValue = propertyAccessor.get(javaContext).getContextObject();
        boolean matches = fieldValue != null;
        matches = matches && (fieldValue instanceof Collection<?>);
        matches = matches && !(((Collection<?>) fieldValue).isEmpty());
        return matches;
    }

    @Override
    public String toString() {
        return String.format("%s[getter=%s]", getClass().getSimpleName(), this.propertyAccessor);
    }

}
