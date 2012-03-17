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
package info.rsdev.xb4j.model.converter;

import info.rsdev.xb4j.exceptions.Xb4jException;

/**
 * Convenience class. It does not perform any conversion; it only casts an Object to a String and a String to an Object.
 * When the Object is not a String, an exception is thrown. The existence of this class can make operations nullsafe, 
 * without performing a null check.
 * 
 * @author Dave Schoorl
 */
public class NOPConverter implements IValueConverter {
	
	public static final NOPConverter INSTANCE = new NOPConverter();
	
	@Override
	public Object toObject(String value) {
		if (value == null) { return null; }
		return value;
	}
	
	@Override
	public String toText(Object value) {
		if (value == null) { return null; }
		if (!(value instanceof String)) {
			throw new Xb4jException(String.format("Expected a %s, but encountered a %s", String.class.getName(), 
					value.getClass().getName()));
		}
		return (String)value;
	}
	
	public Class<?> getJavaType() {
	    return String.class;
	}
	
}
