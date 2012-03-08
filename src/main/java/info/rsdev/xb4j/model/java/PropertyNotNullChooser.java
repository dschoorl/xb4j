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
package info.rsdev.xb4j.model.java;

import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;

/**
 * 
 * @author Dave Schoorl
 */
public class PropertyNotNullChooser implements IChooser {
	
	private FieldAccessProvider fieldAccessor = null;
	
	public PropertyNotNullChooser(String fieldName) {
		this.fieldAccessor = new FieldAccessProvider(fieldName);
	}
	
	@Override
	public boolean matches(Object javaContext) {
		/* When the javaContext is null, we cannot establish the Object has the requested field 
		 * and thus we respond with false */
	    if (javaContext == null) { return false; }
		Object fieldValue = fieldAccessor.get(javaContext);
		return fieldValue != null;
	}
	
}
