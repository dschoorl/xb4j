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
package info.rsdev.xb4j.model.bindings;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.exceptions.Xb4jMutabilityException;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;

public abstract class AbstractAttributeMutabilityTest<T extends AbstractAttribute> {

    protected T immutableAttribute = null;

    @Test
    protected void testCannotSetGetter() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableAttribute.setGetter(NoGetter.INSTANCE));
    }

    @Test
    protected void testCannotSetSetter() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableAttribute.setSetter(NoSetter.INSTANCE));
    }

    @Test
    protected void testCannotSetRequired() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableAttribute.setRequired(true));
    }

    @Test
    protected void testCannotSetDefault() {
        assertThrows(Xb4jException.class, () -> immutableAttribute.setDefault("defaultValue"));
    }

}
