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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.test.ObjectA;

class StaticAttributeTest {
    
    private BindingModel model = null;
    private Root root = null;
    
    @BeforeEach
    void setupModelWithObjectABinding() {
        model = new BindingModel();
        root = new Root(new QName("A"), ObjectA.class);
        model.registerRoot(root);
    }

    @Test
    void outputOfOptionalAttributeWithAValueDependsOnParentsOutputState() {
        assertEquals(OutputState.COLLABORATE, getAttributeUnderTest().generatesOutput(new JavaContext("value")));
    }

    @Test 
    void outputOfOptionalAttributeWithoutAValueDependsOnParentsOutputState(){
        assertEquals(OutputState.COLLABORATE, getAttributeUnderTest().generatesOutput(new JavaContext(null)));
    }

    @Test
    void defaultValueIsNotSupported() {
        assertThrows(Xb4jException.class, () -> getAttributeUnderTest().setDefault("default"));
    }

    @Test 
    void outputStaticValueWithEmptyRequiredAttribute() {
        //if the JavaContext does not provide a value, an required attribute generates output anyway
        StaticAttribute attributeUnderTest = getAttributeUnderTest().setRequired(true);
        assertEquals(OutputState.HAS_OUTPUT, attributeUnderTest.generatesOutput(new JavaContext(null)));
    }
    
    private StaticAttribute getAttributeUnderTest() {
        StaticAttribute attributeUnderTest = new StaticAttribute(new QName("x"), "static");
        attributeUnderTest.setGetter(NoGetter.INSTANCE);
        root.addAttribute(attributeUnderTest, NoGetter.INSTANCE, NoSetter.INSTANCE);
        return attributeUnderTest;
    }

}
