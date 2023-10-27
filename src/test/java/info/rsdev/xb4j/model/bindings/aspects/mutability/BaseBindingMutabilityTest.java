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

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jMutabilityException;
import info.rsdev.xb4j.model.bindings.Attribute;
import info.rsdev.xb4j.model.bindings.IBinding;
import info.rsdev.xb4j.model.bindings.action.StoreInContext;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;

abstract class BaseBindingMutabilityTest<T extends IBinding> {

    protected T immutableElement = null;

    @Test
    void testCannotAddAttributeViaConvenienceMethod() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.addAttribute(new Attribute(new QName("number")), "hashcode"));
    }

    @Test
    void testCannotAddAttributeWithGetterSetter() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.addAttribute(new Attribute(new QName("number")), NoGetter.INSTANCE, NoSetter.INSTANCE));
    }

    @Test
    void testCannotAddAction() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.addAction(new StoreInContext("myKey", Object.class)));
    }

    @Test
    void testCannotSetGetter() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.setGetter(NoGetter.INSTANCE));
    }

    @Test
    void testCannotSetParent() {
        IBinding root = immutableElement.getParent();
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.setParent(root));
    }

    @Test
    void testCannotSetSetter() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.setSetter(NoSetter.INSTANCE));
    }

}
