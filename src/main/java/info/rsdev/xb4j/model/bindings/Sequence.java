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
package info.rsdev.xb4j.model.bindings;

import javax.xml.namespace.QName;

import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

/**
 * Group a number of elements where ordering is fixed. Elements can be optional. When an element can occur more than once, you 
 * must wrap them inside a {@link Repeater}.
 * 
 * @author Dave Schoorl
 */
public class Sequence extends AbstractBindingContainer {
	
	/**
	 * Create a new {@link Sequence} which inherits it's element and javatype from it's parent
	 */
	public Sequence() {
		super(NoElementFetchStrategy.INSTANCE, null);
	}
	
    public Sequence(QName element) {
    	super(new DefaultElementFetchStrategy(element), null);
    }

	public Sequence(Class<?> javaType) {
		super(NoElementFetchStrategy.INSTANCE, new DefaultConstructor(javaType));
	}
    
    public Sequence(QName element, Class<?> javaType) {
    	super(new DefaultElementFetchStrategy(element), new DefaultConstructor(javaType));
    }
    
    @Override
    public Sequence setOptional(boolean isOptional) {
    	super.setOptional(isOptional);
    	return this;
    }

}
