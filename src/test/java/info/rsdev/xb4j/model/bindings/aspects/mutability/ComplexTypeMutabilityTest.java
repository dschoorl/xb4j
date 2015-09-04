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

import static org.junit.Assert.assertNull;
import info.rsdev.xb4j.exceptions.Xb4jMutabilityException;
import info.rsdev.xb4j.model.bindings.Attribute;
import info.rsdev.xb4j.model.bindings.ComplexType;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

public class ComplexTypeMutabilityTest extends AbstractSingleBindingMutabilityTest<ComplexType>{

	@Before
	public void setUp() {
		immutableElement = new ComplexType("identifier", "namespace");
		immutableElement.makeImmutable();
	}
	
	@Test(expected=Xb4jMutabilityException.class) @Override
	public void testCannotSetParent() {
		assertNull(immutableElement.getParent());
		immutableElement.setParent(new Element(new QName("ghost")));
	}
	
	@Test(expected=Xb4jMutabilityException.class) @Override
	public void testCannotAddAttributeWithGetterSetter() {
		Root root = new Root(new QName("root"), Object.class);
		ComplexType type = new ComplexType(new QName("complex"), root, "hashcode");
		root.makeImmutable();
		type.addAttribute(new Attribute(new QName("number")), NoGetter.INSTANCE, NoSetter.INSTANCE);
	}

}
