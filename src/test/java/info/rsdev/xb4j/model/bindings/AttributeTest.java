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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.util.XmlStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import org.junit.Before;

import org.junit.Test;

public class AttributeTest {
    
    private BindingModel model = null;
    private Root root = null;
    
    @Before
    public void setupModelWithObjectABinding() {
        model = new BindingModel();
        root = new Root(new QName("A"), ObjectA.class);
        model.register(root);
    }

    @Test
    public void testMarshallSingleAttributeNoNamespace() {
        //Setup the test
        root.addAttribute(new Attribute(new QName("name")), "name");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectA instance = new ObjectA("test");
        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals("<A name=\"test\"/>", stream.toString());
    }

    @Test
    public void testUnmarshallSingleAttributeNoNamespace() {
        //Setup the test
        root.addAttribute(new Attribute(new QName("name")), "name");

        byte[] buffer = "<A name=\"test\"/>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        ObjectA a = (ObjectA) instance;
        assertEquals("test", a.getAName());
    }

    @Test
    public void testMarshallSingleAttributeWithNamespace() {
        //Setup the test
        root.addAttribute(new Attribute(new QName("http://attrib/ns", "name", "test")), "name");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectA instance = new ObjectA("test");
        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals("<A xmlns:test=\"http://attrib/ns\" test:name=\"test\"/>", stream.toString());
    }
    
    @Test
    public void marshallEmptyElementWithNamespaceContainingAttributesInDifferentNamespace() {
        QName rootQName = new QName("http://namespace/A", "A", "a");
        Root myRoot = new Root(rootQName, ObjectA.class);
        myRoot.addAttribute(new Attribute(new QName("http://attrib/ns", "name", "attr")), "name");
        IAttribute simpleAttrib = new AttributeInjector(new QName("http://attrib/ns", "type"), (JavaContext ctx) -> "simple");
        myRoot.addAttribute(simpleAttrib, NoGetter.INSTANCE, NoSetter.INSTANCE);
        model.register(myRoot);
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectA instance = new ObjectA("test");
        model.getXmlStreamer(instance.getClass(), rootQName).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals("<a:A xmlns:a=\"http://namespace/A\" xmlns:attr=\"http://attrib/ns\" attr:name=\"test\" attr:type=\"simple\"/>", stream.toString());
    }
    
    @Test 
    public void outputOptionalAttributeWhenItHasAValue() {
        assertEquals(OutputState.HAS_OUTPUT, getAttributeUnderTest().generatesOutput(new JavaContext("value")));
    }

    @Test 
    public void doNotOutputOptionalAttributeWithoutValue() {
        assertEquals(OutputState.NO_OUTPUT, getAttributeUnderTest().generatesOutput(new JavaContext(null)));
    }

    @Test 
    public void outputEmptyOptionalAttributeWithDefaultValue() {
        Attribute attributeUnderTest = getAttributeUnderTest().setDefault("default");
        assertEquals(OutputState.HAS_OUTPUT, attributeUnderTest.generatesOutput(new JavaContext(null)));
    }

    @Test 
    public void outputEmptyRequiredAttribute() {
        Attribute attributeUnderTest = getAttributeUnderTest().setRequired(true);
        assertEquals(OutputState.HAS_OUTPUT, attributeUnderTest.generatesOutput(new JavaContext(null)));
    }
    
    private Attribute getAttributeUnderTest() {
        Attribute attributeUnderTest = new Attribute(new QName("x"));
        attributeUnderTest.setGetter(NoGetter.INSTANCE);
        root.addAttribute(attributeUnderTest, NoGetter.INSTANCE, NoSetter.INSTANCE);
        return attributeUnderTest;
    }

}
