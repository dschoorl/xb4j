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
package info.rsdev.xb4j.model;

import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

/**
 * Group a number of elements where ordering is fixed. Elements can be optional. When an element can occur more than once, you 
 * must wrap them inside a {@link CollectionBinding}. A sequence has no xml or java object eepresentation.
 * 
 * @author Dave Schoorl
 */
public class SequenceBinding extends AbstractBindingContainer {
	
	/**
	 * Create a new {@link SequenceBinding} which inherits it's element and javatype from it's parent
	 */
	public SequenceBinding() {
		setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
	}
	
	public SequenceBinding(Class<?> javaType) {
		setObjectCreator(new DefaultConstructor(javaType));
		setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
	}
    
}
