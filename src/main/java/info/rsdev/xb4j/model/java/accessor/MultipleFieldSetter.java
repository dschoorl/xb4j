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

/**
 * Convenience class to provide a way to set multiple fields with the same value obtained from a single xml element. All
 * fields are expected to be part of the same java context.
 * @author Dave Schoorl
 */
public class MultipleFieldSetter implements ISetter {
	
	private FieldSetter[] setters = null;
	
	public MultipleFieldSetter(String... fieldNames) {
		if ((fieldNames == null) || (fieldNames.length == 0)) {
			throw new NullPointerException("You must provide at least one fieldname");
		}
		
		setters = new FieldSetter[fieldNames.length];
		for (int i=0; i<fieldNames.length; i++) {
			setters[i] = new FieldSetter(fieldNames[i]);
		}
	}
	
	@Override
	public boolean set(Object javaContext, Object propertyValue) {
		for (FieldSetter setter: setters) {
			setter.set(javaContext, propertyValue);
		}
		return true;
	}
	
}
