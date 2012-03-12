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
import info.rsdev.xb4j.model.bindings.ComplexType;
import info.rsdev.xb4j.model.bindings.Reference;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

public class ComplexTypeTest {
    
    private BindingModel model = null;
    
    @Before
    public void setup() {
        Root root = new Root(new QName("root"), ObjectA.class);   //has element, but class comes from child
        root.setChild(new Reference("typeO", null), NoGetter.INSTANCE, NoSetter.INSTANCE);
        
        //bind complextype to other xml element (same javaclass) -- this is currently not supported by BindingModel
        Root hoofdmap = new Root(new QName("directory"), ObjectTree.class);   //has element, but class comes from child
        hoofdmap.setChild(new Reference(ObjectA.class, "typeO", null), "myObject");	//Must create ObjectA when marshalling
        
        ComplexType complexType = new ComplexType("typeO", null);
        complexType.setChild(new SimpleType(new QName("name")), "name");
        
        
        model = new BindingModel();
        model.register(complexType, true);
        model.register(root);
        model.register(hoofdmap);
    }
	
    @Test 
    public void testMarshallComplexType() {
        //marshall root
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new ObjectA("test");
        model.toXml(stream, instance);
        String result = stream.toString();
        assertEquals("<root><name>test</name></root>", result);
        
        //marshall hoofdmap
        stream = new ByteArrayOutputStream();
        instance = new ObjectTree().setMyObject(new ObjectA("test"));
        model.toXml(stream, instance);
        result = stream.toString();
        assertEquals("<directory><name>test</name></directory>", result);
    }
    
    @Test
    public void testUnmarshallComplexType() {
    	//Unmarshall ObjectA
        ByteArrayInputStream stream = new ByteArrayInputStream("<root><name>test</name></root>".getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("test", ((ObjectA)instance).getName());
        
        //unmarshall ObjectTree
        stream = new ByteArrayInputStream("<directory><name>test</name></directory>".getBytes());
        instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree)instance;
        assertNotNull(tree.getMyObject());
        assertEquals("test", tree.getMyObject().getName());
    }
    
}
