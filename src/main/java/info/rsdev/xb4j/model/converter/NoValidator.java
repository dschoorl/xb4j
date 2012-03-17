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

import info.rsdev.xb4j.exceptions.ValidationException;

/**
 * This {@link IValidator} performs no validation and will never throw a {@link ValidationException}
 *  
 * @author Dave Schoorl
 */
public class NoValidator implements IValidator {
	
	/**
	 * Singleton version of this implementation
	 */
	public static final NoValidator INSTANCE = new NoValidator();
	
	/**
	 * Do not create a new instance every time, but reuse {@link #INSTANCE}
	 */
	private NoValidator() {}
	
	@Override
	public <T> T isValid(T instance) throws ValidationException {
		return instance;
	}
	
}
