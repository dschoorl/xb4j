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

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.bindings.ComplexType;
import info.rsdev.xb4j.model.bindings.Reference;
import info.rsdev.xb4j.model.bindings.Root;

import javax.xml.namespace.QName;

import org.junit.Before;

public class ReferenceMutabilityTest extends BaseBindingMutabilityTest<Reference> {

    @Before
    public void setUp() {
        BindingModel model = new BindingModel();
        Root root = new Root(new QName("root"), Object.class);
        immutableElement = new Reference(new QName("level1"), "identifier", "namespace");
        root.setChild(immutableElement);
        model.registerRoot(root);

        //add ComplexType to BindingModel that can be resolved as well...
        ComplexType type = new ComplexType("identifier", "namespace");
        model.registerComplexType(type, false);

        root.makeImmutable();	//this will resolve all Reference objects and replace them with a copy of the referenced ComplexType
    }

}
