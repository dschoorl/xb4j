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

import static info.rsdev.xb4j.model.bindings.action.AbstractAction.narrow;
import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.model.java.JavaContext;

/**
 * Store the context object into the Map with external context objects for later use 
 * 
 * @author Dave Schoorl
 */
public class StoreInContext implements IPhasedAction {
	
	private final String keyIntoExternalContext;
	
	private final Class<?> expectedType;
	
	/**
	 * Create a new {@link StoreInContext} instance that will store the current context object into the Map
	 * with external context objects under the given keyName.
	 * @param keyName the name of the key to store the current context under
	 * @param expectedType the type of the context object to store
	 */
	public StoreInContext(String keyName, Class<?> expectedType) {
		if (keyName == null) {
			throw new NullPointerException("The keyName cannot be null");
		}
		if (keyName.length() == 0) {
			throw new IllegalArgumentException("The keyName cannot be empty");
		}
		if (expectedType == null) {
			throw new NullPointerException("The expected type cannot be null");
		}
		this.keyIntoExternalContext = keyName;
		this.expectedType = expectedType;
	}
	
	@Override
	public JavaContext execute(JavaContext javaContext) throws Xb4jMarshallException {
		Object contextObject = javaContext.getContextObject();
		javaContext.set(this.keyIntoExternalContext, narrow(contextObject, expectedType));
		return javaContext;
	}

	@Override
	public boolean executeAt(ExecutionPhase currentPhase) {
		return (currentPhase == ExecutionPhase.BEFORE_MARSHALLING) || (currentPhase == ExecutionPhase.AFTER_UNMARSHALLING);
	}
	
}
