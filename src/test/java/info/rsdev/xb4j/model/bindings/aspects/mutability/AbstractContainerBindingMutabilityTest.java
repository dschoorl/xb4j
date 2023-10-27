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

import static org.junit.jupiter.api.Assertions.*;
import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jMutabilityException;
import info.rsdev.xb4j.model.bindings.AbstractContainerBinding;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;

abstract class AbstractContainerBindingMutabilityTest<T extends AbstractContainerBinding> extends BaseBindingMutabilityTest<T> {

    @Test
    void testCannotAddBinding() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.add(new Element(new QName("level2"), false)));
    }

    @Test
    void testCannotAddBindingViaConvenienceMethod() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.add(new Element(new QName("level2"), false), "someField"));
    }

    @Test
    void testCannotAddBindingViaGetterSetter() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.add(new Element(new QName("level2"), false), NoGetter.INSTANCE, NoSetter.INSTANCE));
    }

}
