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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.test.ObjectTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.junit.Test;

/**
 * 
 * @author Dave Schoorl
 */
public class RepeaterTest {
	
	@Test
	public void testMarshallValueCollectionNoContainerElement() {
		//fixture
        Root root = new Root(new QName("root"), ObjectTree.class);
        Repeater collection = (Repeater)root.setChild(new Repeater(ArrayList.class), "messages");
        collection.setItem(new SimpleType(new QName("detail")));
        BindingModel model = new BindingModel().register(root);
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("bericht1");
        instance.addMessage("bericht2");
        
        model.toXml(stream, instance);
        String result = stream.toString();
        assertEquals("<root><detail>bericht1</detail><detail>bericht2</detail></root>", result);
	}
	
    @Test
    public void testMarshallValueCollectionWithContainerElement() {
        //fixture
        Root root = new Root(new QName("root"), ObjectTree.class);
        Repeater collection = (Repeater)root.setChild(new Repeater(new QName("collection"), ArrayList.class), "messages");
        collection.setItem(new SimpleType(new QName("detail")));
        BindingModel model = new BindingModel().register(root);
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("bericht1");
        instance.addMessage("bericht2");
        
        model.toXml(stream, instance);
        String result = stream.toString();
        assertEquals("<root><collection><detail>bericht1</detail><detail>bericht2</detail></collection></root>", result);
    }
    
	@Test
	public void testUnmarshallValueCollectionNoContainerElement() {
		//fixture
        Root root = new Root(new QName("root"), ObjectTree.class);
        Repeater collection = (Repeater)root.setChild(new Repeater(ArrayList.class), "messages");
        collection.setItem(new SimpleType(new QName("detail")));
        BindingModel model = new BindingModel().register(root);
        
        ByteArrayInputStream stream = new ByteArrayInputStream("<root><detail>bericht1</detail><detail>bericht2</detail></root>".getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree)instance;
        assertNotNull(tree.getMessages());
        assertEquals(2, tree.getMessages().size());
        assertArrayEquals(new String[] {"bericht1", "bericht2"}, tree.getMessages().toArray());
	}
	
    @Test
    public void testUnmarshallValueCollectionWithContainerElement() {
        //fixture
        Root root = new Root(new QName("root"), ObjectTree.class);
        Repeater collection = (Repeater)root.setChild(new Repeater(new QName("collection"), ArrayList.class), "messages");
        collection.setItem(new SimpleType(new QName("detail")));
        BindingModel model = new BindingModel().register(root);
        
        ByteArrayInputStream stream = new ByteArrayInputStream("<root><collection><detail>bericht1</detail><detail>bericht2</detail></collection></root>".getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree)instance;
        assertNotNull(tree.getMessages());
        assertEquals(2, tree.getMessages().size());
        assertArrayEquals(new String[] {"bericht1", "bericht2"}, tree.getMessages().toArray());
    }
    
    @Test
    public void testRepeaterNoCollectionNoElementGeneratesNoOutput() {
    	Repeater repeater = new Repeater(ArrayList.class);
    	assertFalse(repeater.generatesOutput(null));
    }
    
    @Test
    public void testRepeaterEmptyCollectionNoElementGeneratesNoOutput() {
    	Repeater repeater = new Repeater(ArrayList.class);
    	assertFalse(repeater.generatesOutput(new ArrayList<String>()));
    }
    
    @Test
    public void testRepeaterEmptyCollectionOptionalElementGeneratesNoOutput() {
    	Repeater repeater = new Repeater(new QName("optional"), ArrayList.class, true);
    	assertFalse(repeater.generatesOutput(new ArrayList<String>()));
    }
    
    @Test
    public void testRepeaterEmptyCollectionMandatoryElementGeneratesNoOutput() {
    	Repeater repeater = new Repeater(new QName("mandatory"), ArrayList.class, false);
    	assertTrue(repeater.generatesOutput(new ArrayList<String>()));
    }
}
