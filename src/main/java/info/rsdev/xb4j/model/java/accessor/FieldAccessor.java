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

import info.rsdev.xb4j.model.java.JavaContext;

import java.lang.reflect.Field;

/**
 * Get or set the value of a class property by accessing it's {@link Field} by fieldname
 * @author Dave Schoorl
 */
public class FieldAccessor implements ISetter, IGetter {
	
	private FieldGetter getter = null;
	
	private FieldSetter setter = null;
	
	public FieldAccessor(String fieldName) {
		getter = new FieldGetter(fieldName);
		setter = new FieldSetter(fieldName);
	}
	
	@Override
	public boolean set(JavaContext javaContext, Object propertyValue) {
		return setter.set(javaContext, propertyValue);
	}
	
	@Override
	public JavaContext get(JavaContext contextInstance) {
		return getter.get(contextInstance);
	}
	
}
