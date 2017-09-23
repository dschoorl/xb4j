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

import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.bindings.action.Indexer;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.util.XmlStreamFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Dave Schoorl
 */
public class MapRepeaterTest {

    @Test
    public void testMarshallValueMapNoContainerElement() {
        //fixture
        Root root = new Root(new QName("root"), ObjectTree.class);
        MapRepeater map = root.setChild(new MapRepeater(LinkedHashMap.class), "codes");
        map.setKeyValue(new SimpleType(new QName("key")), new SimpleType(new QName("value")));
        BindingModel model = new BindingModel().register(root);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addCode("1", "Een");
        instance.addCode("2", "Twee");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><key>1</key><value>Een</value><key>2</key><value>Twee</value></root>", result);
    }

    @Test
    public void testMarshallRootAsMapType() {
        //fixture
        Root root = new Root(new QName("root"), LinkedHashMap.class);
        MapRepeater map = root.setChild(new MapRepeater());
        map.setKeyValue(new SimpleType(new QName("key")), new SimpleType(new QName("value")));
        BindingModel model = new BindingModel().register(root);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Map<String, String> instance = new LinkedHashMap<>();
        instance.put("1", "Een");
        instance.put("2", "Twee");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><key>1</key><value>Een</value><key>2</key><value>Twee</value></root>", result);
    }

    @Test(expected = Xb4jMarshallException.class)
    public void testMarshallExpectParentBindingToProvideMapButNoMap() {
        //fixture
        Root root = new Root(new QName("root"), ObjectTree.class);	//Root binding must represent Map type, but does not
        MapRepeater map = root.setChild(new MapRepeater());
        map.setKeyValue(new SimpleType(new QName("key")), new SimpleType(new QName("value")));
        BindingModel model = new BindingModel().register(root);

        model.getXmlStreamer(ObjectTree.class, null).toXml(XmlStreamFactory.makeWriter(new ByteArrayOutputStream()), new ObjectTree());
    }

    @Test
    public void testMarshallValueMapWithContainerElement() {
        //fixture
        Root root = new Root(new QName("root"), ObjectTree.class);
        MapRepeater map = root.setChild(new MapRepeater(new QName("mapping"), LinkedHashMap.class), "codes");
        map.setKeyValue(new SimpleType(new QName("key")), new SimpleType(new QName("value")));
        BindingModel model = new BindingModel().register(root);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addCode("1", "Een");
        instance.addCode("2", "Twee");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><mapping><key>1</key><value>Een</value><key>2</key><value>Twee</value></mapping></root>", result);
    }

    @Test
    public void testUnmarshallValueMapNoContainerElement() {
        //fixture
        Root root = new Root(new QName("root"), ObjectTree.class);
        MapRepeater map = root.setChild(new MapRepeater(LinkedHashMap.class), "codes");
        map.setKeyValue(new SimpleType(new QName("key")), new SimpleType(new QName("value")));
        BindingModel model = new BindingModel().register(root);

        ByteArrayInputStream stream = new ByteArrayInputStream("<root><key>1</key><value>Een</value><key>2</key><value>Twee</value></root>".getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree) instance;
        assertNotNull(tree.getCodes());
        assertEquals(2, tree.getCodes().size());
        assertArrayEquals(new String[]{"1", "2"}, tree.getCodes().keySet().toArray());
        assertArrayEquals(new String[]{"Een", "Twee"}, tree.getCodes().values().toArray());
    }

    @Test
    public void testUnmarshallRootAsMapType() {
        //fixture
        Root root = new Root(new QName("root"), LinkedHashMap.class);
        MapRepeater map = root.setChild(new MapRepeater());
        map.setKeyValue(new SimpleType(new QName("key")), new SimpleType(new QName("value")));
        BindingModel model = new BindingModel().register(root);

        ByteArrayInputStream stream = new ByteArrayInputStream("<root><key>1</key><value>Een</value><key>2</key><value>Twee</value></root>".getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(LinkedHashMap.class, instance.getClass());
        Map<?, ?> mappings = (Map<?, ?>) instance;
        assertEquals(2, mappings.size());
        assertArrayEquals(new String[]{"1", "2"}, mappings.keySet().toArray());
        assertArrayEquals(new String[]{"Een", "Twee"}, mappings.values().toArray());
    }

    @Test(expected = Xb4jUnmarshallException.class)
    public void testUnmarshallExpectParentBindingToProvideMapButNoMap() {
        //fixture
        Root root = new Root(new QName("root"), ObjectTree.class);	//Root binding must represent Map type, but does not
        MapRepeater map = root.setChild(new MapRepeater());
        map.setKeyValue(new SimpleType(new QName("key")), new SimpleType(new QName("value")));
        BindingModel model = new BindingModel().register(root);

        ByteArrayInputStream stream = new ByteArrayInputStream("<root><key>1</key><value>Een</value><key>2</key><value>Twee</value></root>".getBytes());
        model.toJava(XmlStreamFactory.makeReader(stream));
    }

    @Test
    public void testUnmarshallValueMapWithContainerElement() {
        //fixture
        Root root = new Root(new QName("root"), ObjectTree.class);
        MapRepeater map = root.setChild(new MapRepeater(new QName("mapping"), LinkedHashMap.class), "codes");
        map.setKeyValue(new SimpleType(new QName("key")), new SimpleType(new QName("value")));
        BindingModel model = new BindingModel().register(root);

        ByteArrayInputStream stream = new ByteArrayInputStream("<root><mapping><key>1</key><value>Een</value><key>2</key><value>Twee</value></mapping></root>".getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree) instance;
        assertNotNull(tree.getCodes());
        assertEquals(2, tree.getCodes().size());
        assertArrayEquals(new String[]{"1", "2"}, tree.getCodes().keySet().toArray());
        assertArrayEquals(new String[]{"Een", "Twee"}, tree.getCodes().values().toArray());
    }

    @Test
    public void testRepeaterNoMapNoElementGeneratesNoOutput() {
        MapRepeater repeater = new MapRepeater(LinkedHashMap.class);
        assertFalse(repeater.generatesOutput(new JavaContext(null)));
    }

    @Test
    public void testRepeaterEmptyMapNoElementGeneratesNoOutput() {
        MapRepeater repeater = new MapRepeater(LinkedHashMap.class);
        assertFalse(repeater.generatesOutput(new JavaContext(new LinkedHashMap<>())));
    }

    @Test
    public void testRepeaterEmptyMapOptionalElementGeneratesNoOutput() {
        MapRepeater repeater = new MapRepeater(new QName("optional"), LinkedHashMap.class, true);
        assertFalse(repeater.generatesOutput(new JavaContext(new LinkedHashMap<>())));
    }

    @Test
    public void testRepeaterEmptyCollectionMandatoryElementGeneratesNoOutput() {
        Repeater repeater = new Repeater(new QName("mandatory"), ArrayList.class, false);
        assertTrue(repeater.generatesOutput(new JavaContext(new ArrayList<>())));
    }

    @Test
    @Ignore("TBD: rewrite for MapRepeater and use index as key or value?")
    public void testMarshallCollectionIndexAsAttribute() throws Exception {
        //work on a collection of Strings
        BindingModel model = new BindingModel();
        Root root = new Root(new QName("root"), ObjectTree.class);
        Repeater collection = root.setChild(new Repeater(new QName("collection"), ArrayList.class), "messages");
        SimpleType item = collection.setItem(new SimpleType(new QName("item")));
        item.addAttribute(new AttributeInjector(new QName("seqnr"), Indexer.INSTANCE), NoGetter.INSTANCE, NoSetter.INSTANCE);
        model.register(root);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("string1");
        instance.addMessage("string2");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><collection><item seqnr=\"0\">string1</item><item seqnr=\"1\">string2</item></collection></root>", result);
    }

    @Test
    @Ignore("TBD: rewrite for MapRepeater and use index as key or value?")
    public void testMarshallCollectionIndexAsElement() throws Exception {
        BindingModel model = new BindingModel();
        Root root = new Root(new QName("root"), ObjectTree.class);
        Repeater collection = root.setChild(new Repeater(new QName("collection"), ArrayList.class), "messages");
        Sequence content = collection.setItem(new Sequence(new QName("item")));
        content.add(new SimpleType(new QName("value")));
        content.add(new ElementInjector(new QName("seqnr"), Indexer.INSTANCE));
        model.register(root);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("string1");
        instance.addMessage("string2");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><collection><item><value>string1</value><seqnr>0</seqnr></item><item><value>string2</value><seqnr>1</seqnr></item></collection></root>", result);
    }

}
