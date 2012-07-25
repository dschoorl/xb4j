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
package info.rsdev.xb4j.model.bindings.action;

import info.rsdev.xb4j.exceptions.Xb4jException;

/**
 * 
 * @author Dave Schoorl
 */
public abstract class AbstractAction {
	
	/**
	 * Cast the context to the expected type
	 * @param context
	 * @param type
	 * @return
	 * @throws NullPointerException when the context is null
	 * @throws IllegalArgumentException when the context is not of the expected type
	 */
	@SuppressWarnings("unchecked")
	public static <T> T narrow(Object context, Class<T> type) throws Xb4jException {
		if (type == null) {
			throw new NullPointerException("Expected type cannot be null");
		}
		if (context == null) {
			throw new NullPointerException(String.format("Context object of type %s cannot be null", type.getName()));
		}
		if (!type.isAssignableFrom(context.getClass())) {
			throw new Xb4jException(String.format("Expected object of type %s, but encountered type %s", type, context.getClass().getName()));
		}
		return (T)context;
	}
	
}
