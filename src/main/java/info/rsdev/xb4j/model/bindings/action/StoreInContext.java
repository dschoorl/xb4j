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

import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.model.java.JavaContext;

/**
 * Store the context object into the Map with external context objects for later use 
 * 
 * @author Dave Schoorl
 */
public class StoreInContext implements IPhasedAction {
	
	private final String keyIntoExternalContext;
	
	public StoreInContext(String keyName) {
		if (keyName == null) {
			throw new NullPointerException("The keyName cannot be null");
		}
		if (keyName.length() == 0) {
			throw new IllegalArgumentException("The keyName cannot be empty");
		}
		this.keyIntoExternalContext = keyName;
	}
	
	@Override
	public void execute(JavaContext javaContext) throws Xb4jMarshallException {
		javaContext.set(this.keyIntoExternalContext, javaContext.getContextObject());
	}

	@Override
	public boolean executeAt(ExecutionPhase currentPhase) {
		return (currentPhase == ExecutionPhase.AFTER_OBJECT_CREATION) || (currentPhase == ExecutionPhase.BEFORE_MARSHALLING);
	}
	
}
