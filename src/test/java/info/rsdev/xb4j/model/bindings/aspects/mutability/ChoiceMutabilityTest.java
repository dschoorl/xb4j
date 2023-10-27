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

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.exceptions.Xb4jMutabilityException;
import info.rsdev.xb4j.model.bindings.Attribute;
import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.chooser.PropertyNotNullChooser;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;

class ChoiceMutabilityTest extends BaseBindingMutabilityTest<Choice> {

    @BeforeEach
    public void setUp() {
        Root root = new Root(new QName("root"), Object.class);
        immutableElement = new Choice(new QName("level1"), false);
        root.setChild(immutableElement);
        root.makeImmutable();
    }

    @Test
    void testCannotAddOptionViaConvenienceMethod() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.addOption(new Element(new QName("level2"), false)));
    }

    @Test
    void testCannotAddOptionWithFieldnameAndChooser() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.addOption(new Element(new QName("level2"), false), "someField", new PropertyNotNullChooser("someField")));
    }

    @Test
    void testCannotAddOptionWithChooser() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.addOption(new Element(new QName("level2"), false), new PropertyNotNullChooser("someField")));
    }

    @Test
    @Override
    void testCannotAddAttributeViaConvenienceMethod() {
        assertThrows(Xb4jException.class, () -> immutableElement.addAttribute(new Attribute(new QName("number")), "hashcode"));
    }

    @Test
    @Override
    void testCannotAddAttributeWithGetterSetter() {
        assertThrows(Xb4jException.class, () -> immutableElement.addAttribute(new Attribute(new QName("number")), NoGetter.INSTANCE, NoSetter.INSTANCE));
    }

}
