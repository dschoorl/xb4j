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
 * Expose the index of the current context object into the sequential collection of the parent binding.
 * @author Dave Schoorl
 */
public class Indexer implements IMarshallingAction {
	
	public static final Indexer INSTANCE = new Indexer();
	
	private Indexer() {}
	
	/**
	 * This action obtains the index of the current context object into a sequential collection of it's parent binding. When the
	 * parent is not a collection, null is returned.
	 */
	@Override
	public String execute(JavaContext javaContext) throws Xb4jMarshallException {
		int index = javaContext.getIndexInCollection();
		if (index < 0) {
			return null;
		}
		return String.valueOf(index);
	}
	
}
