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

import info.rsdev.xb4j.model.java.accessor.FieldAccessor;
import info.rsdev.xb4j.model.java.accessor.IGetter;

/**
 * A match is made when the actual property of the given javaContext is of the given java type. If the field is null, this is
 * considered to be a mismatch.
 * 
 * @author Dave Schoorl
 */
public class PropertyInstanceOf implements IChooser {
	
	private IGetter propertyAccessor = null;
	
	private Class<?> instanceOf = null;

	/**
	 * Create a new instance of {@link PropertyInstanceOf}. This implementation of {@link IChooser} will match a choice when a 
	 * certain property of the current java context matches the given javaType
	 * @param javaType the type that the property in the java context object must have for the {@link IChooser} to match this choice
	 */
	public PropertyInstanceOf(String fieldName, Class<?> javaType) {
		if (javaType == null) {
			throw new NullPointerException("Class cannot be null");
		}
		this.instanceOf = javaType;
		this.propertyAccessor = new FieldAccessor(fieldName);
	}
	
	@Override
	public boolean matches(Object javaContext) {
	    if (javaContext == null) { return false; }
		Object fieldValue = propertyAccessor.get(javaContext);
		boolean matches = fieldValue != null;
		matches = matches && (this.instanceOf.isAssignableFrom(fieldValue.getClass()));
		return matches;
	}
	
	@Override
	public String toString() {
        return String.format("%s[getter=%s, type=%s]", getClass().getSimpleName(), this.propertyAccessor, this.instanceOf.getName());
	}
}
