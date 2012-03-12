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
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Test;

/**
 *
 * @author Dave Schoorl
 */
public class SimpleBindingTest {
	
    @Test
    public void testMarshallingToEmptyElementNoNamespace() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        BindingModel model = new BindingModel();
        model.register(new Root(new QName("root"), Object.class));
        model.toXml(stream, instance);
        assertEquals("<root/>", stream.toString());
    }
    
    @Test
    public void testUnmarshallingFromEmptyElementNoNamespace() {
        byte[] buffer = "<root/>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        BindingModel model = new BindingModel();
        model.register(new Root(new QName("root"), Object.class));
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(Object.class, instance.getClass());
    }
    
    @Test
    public void testMarshallingToEmptyElementWithNamespace() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        BindingModel model = new BindingModel();
        model.register(new Root(new QName("urn:test/namespace", "root", "tst"), Object.class));
        model.toXml(stream, instance);
        assertEquals("<tst:root xmlns:tst=\"urn:test/namespace\"/>", stream.toString());
    }
    
    @Test
    public void testUnmarshallingFromEmptyElementWithNamespace() {
        byte[] buffer = "<tst:root xmlns:tst=\"urn:test/namespace\"/>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        BindingModel model = new BindingModel();
        model.register(new Root(new QName("urn:test/namespace", "root", "tst"), Object.class));
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(Object.class, instance.getClass());
    }
    
    @Test
    public void testUnmarshalFromNestedXmlWithNamespaces() {
        BindingModel model = new BindingModel();
        Root binding = new Root(new QName("urn:test/namespace", "root", "tst"), ObjectTree.class);
        binding.setChild(new Element(new QName("urn:test/namespace", "child", "tst"), ObjectA.class), "myObject");
        model.register(binding);
        
        byte[] buffer = "<tst:root xmlns:tst=\"urn:test/namespace\"><tst:child/></tst:root>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        assertNotNull(((ObjectTree)instance).getMyObject());
    }
    
    @Test
    public void testMarshallNestedBinding() throws Exception {
        BindingModel model = new BindingModel();
        Root binding = new Root(new QName("urn:test/namespace", "root", "tst"), ObjectTree.class);
        binding.setChild(new Element(new QName("urn:test/namespace", "child", "tst"), ObjectA.class), "myObject");
        model.register(binding);
        
        ObjectTree instance = new ObjectTree();
        instance.setMyObject(new ObjectA("test"));
        String expected = "<tst:root xmlns:tst=\"urn:test/namespace\">" +
                          "<tst:child/>" +
                          "</tst:root>";
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, instance);
        assertEquals(expected, stream.toString());
    }
    
    @Test
    public void testMarshallValue() throws Exception {
        BindingModel model = new BindingModel();
        Root binding = new Root(new QName("urn:test/namespace", "myobject", "tst"), ObjectA.class);
        binding.setChild(new SimpleType(new QName("name")), "name");
        model.register(binding);
        
        ObjectA instance = new ObjectA("test");
        
        String expected = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>test</name></tst:myobject>";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, instance);
        assertEquals(expected, stream.toString());
    }
    
    @Test
    public void testUnmarshallValue() throws Exception {
        BindingModel model = new BindingModel();
        Root binding = new Root(new QName("urn:test/namespace", "myobject", "tst"), ObjectA.class);
        binding.setChild(new SimpleType(new QName("name")), "name");
        model.register(binding);
        
        byte[] buffer = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>test</name></tst:myobject>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("test", ((ObjectA)instance).getName());
    }
}
