/* Copyright 2013 Red Star Development / Dave Schoorl
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
package info.rsdev.xb4j.model.bindings.aspects.mutability;

import info.rsdev.xb4j.exceptions.Xb4jMutabilityException;
import info.rsdev.xb4j.model.bindings.AbstractSingleBinding;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;

import javax.xml.namespace.QName;

import org.junit.Test;

public abstract class AbstractSingleBindingMutabilityTest<T extends AbstractSingleBinding> extends BaseBindingMutabilityTest<T> {

	@Test(expected=Xb4jMutabilityException.class)
	public void testCannotSetChild() {
		immutableElement.setChild(new Element(new QName("level2")));
	}
	
	@Test(expected=Xb4jMutabilityException.class)
	public void testCannotSetChildViaConvenienceMethod() {
		immutableElement.setChild(new Element(new QName("level2")), "someField");
	}
	
	@Test(expected=Xb4jMutabilityException.class)
	public void testCannotSetChildViaGetterSetter() {
		immutableElement.setChild(new Element(new QName("level2")), NoGetter.INSTANCE, NoSetter.INSTANCE);
	}
	
}
