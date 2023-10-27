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

import org.junit.jupiter.api.BeforeEach;

import info.rsdev.xb4j.model.bindings.Ignore;
import info.rsdev.xb4j.model.bindings.Root;

public class IgnoreMutabilityTest extends BaseBindingMutabilityTest<Ignore> {

    @BeforeEach
    public void setUp() {
        Root root = new Root(new QName("root"), Object.class);
        immutableElement = new Ignore(new QName("level1"), false);
        root.setChild(immutableElement);
        root.makeImmutable();
    }

}
