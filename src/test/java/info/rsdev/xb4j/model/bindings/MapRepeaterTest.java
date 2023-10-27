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

import static info.rsdev.xb4j.model.bindings.SchemaOptions.NILLABLE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.bindings.action.Indexer;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.test.UnmarshallUtils;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.util.XmlStreamFactory;

/**
 *
 * @author Dave Schoorl
 */
class MapRepeaterTest {

    @Test
    void testMarshallValueMapNoContainerElement() {
        //fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        MapRepeater map = root.setChild(new MapRepeater(LinkedHashMap.class, false), "codes");
        map.setKeyValue(new SimpleType(new QName("key"), false), new SimpleType(new QName("value"), false));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addCode("1", "Een");
        instance.addCode("2", "Twee");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><key>1</key><value>Een</value><key>2</key><value>Twee</value></root>", result);
    }

    @Test
    void testMarshallRootAsMapType() {
        //fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), LinkedHashMap.class));
        MapRepeater map = root.setChild(new MapRepeater(false));
        map.setKeyValue(new SimpleType(new QName("key"), false), new SimpleType(new QName("value"), false));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Map<String, String> instance = new LinkedHashMap<>();
        instance.put("1", "Een");
        instance.put("2", "Twee");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><key>1</key><value>Een</value><key>2</key><value>Twee</value></root>", result);
    }

    @Test
    void testMarshallExpectParentBindingToProvideMapButNoMap() {
        //fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));	//Root binding must represent Map type, but does not
        MapRepeater map = root.setChild(new MapRepeater(false));
        map.setKeyValue(new SimpleType(new QName("key"), false), new SimpleType(new QName("value"), false));

        assertThrows(Xb4jMarshallException.class, () -> model.getXmlStreamer(ObjectTree.class, null).toXml(XmlStreamFactory.makeWriter(new ByteArrayOutputStream()), new ObjectTree()));
    }

    @Test
    void testMarshallValueMapWithContainerElement() {
        //fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        MapRepeater map = root.setChild(new MapRepeater(new QName("mapping"), LinkedHashMap.class, true), "codes");
        map.setKeyValue(new SimpleType(new QName("key"), false), new SimpleType(new QName("value"), false));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addCode("1", "Een");
        instance.addCode("2", "Twee");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><mapping><key>1</key><value>Een</value><key>2</key><value>Twee</value></mapping></root>", result);
    }

    @Test
    void testUnmarshallValueMapNoContainerElement() {
        //fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        MapRepeater map = root.setChild(new MapRepeater(LinkedHashMap.class, false), "codes");
        map.setKeyValue(new SimpleType(new QName("key"), false), new SimpleType(new QName("value"), false));

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
    void testUnmarshallRootAsMapType() {
        //fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), LinkedHashMap.class));
        MapRepeater map = root.setChild(new MapRepeater(false));
        map.setKeyValue(new SimpleType(new QName("key"), false), new SimpleType(new QName("value"), false));

        ByteArrayInputStream stream = new ByteArrayInputStream("<root><key>1</key><value>Een</value><key>2</key><value>Twee</value></root>".getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(LinkedHashMap.class, instance.getClass());
        Map<?, ?> mappings = (Map<?, ?>) instance;
        assertEquals(2, mappings.size());
        assertArrayEquals(new String[]{"1", "2"}, mappings.keySet().toArray());
        assertArrayEquals(new String[]{"Een", "Twee"}, mappings.values().toArray());
    }

    @Test
    void testUnmarshallExpectParentBindingToProvideMapButNoMap() {
        //fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));	//Root binding must represent Map type, but does not
        MapRepeater map = root.setChild(new MapRepeater(false));
        map.setKeyValue(new SimpleType(new QName("key"), false), new SimpleType(new QName("value"), false));

        ByteArrayInputStream stream = new ByteArrayInputStream("<root><key>1</key><value>Een</value><key>2</key><value>Twee</value></root>".getBytes());
        assertThrows(Xb4jUnmarshallException.class, () -> model.toJava(XmlStreamFactory.makeReader(stream)));
    }

    @Test
    void testUnmarshallValueMapWithContainerElement() {
        //fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        MapRepeater map = root.setChild(new MapRepeater(new QName("mapping"), LinkedHashMap.class, true), "codes");
        map.setKeyValue(new SimpleType(new QName("key"), false), new SimpleType(new QName("value"), false));

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
    void testRepeaterNoMapNoElementGeneratesNoOutput() {
        MapRepeater repeater = new MapRepeater(LinkedHashMap.class, false);
        assertSame(OutputState.NO_OUTPUT, repeater.generatesOutput(new JavaContext(null)));
    }

    @Test
    void testRepeaterEmptyMapNoElementGeneratesNoOutput() {
        MapRepeater repeater = new MapRepeater(LinkedHashMap.class, false);
        assertSame(OutputState.NO_OUTPUT, repeater.generatesOutput(new JavaContext(new LinkedHashMap<>())));
    }

    @Test
    void testRepeaterEmptyMapOptionalElementGeneratesNoOutput() {
        MapRepeater repeater = new MapRepeater(new QName("optional"), LinkedHashMap.class, true);
        assertSame(OutputState.NO_OUTPUT, repeater.generatesOutput(new JavaContext(new LinkedHashMap<>())));
    }

    @Test
    void testRepeaterEmptyCollectionMandatoryElementGeneratesNoOutput() {
        Repeater repeater = new Repeater(new QName("mandatory"), ArrayList.class, false);
        assertSame(OutputState.HAS_OUTPUT, repeater.generatesOutput(new JavaContext(new ArrayList<>())));
    }

    @Test
    @Disabled("TBD: rewrite for MapRepeater and use index as key or value?")
    void testMarshallCollectionIndexAsAttribute() throws Exception {
        //work on a collection of Strings
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        Repeater collection = root.setChild(new Repeater(new QName("collection"), ArrayList.class, true), "messages");
        SimpleType item = collection.setItem(new SimpleType(new QName("item"), false));
        item.addAttribute(new AttributeInjector(new QName("seqnr"), Indexer.INSTANCE), NoGetter.INSTANCE, NoSetter.INSTANCE);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("string1");
        instance.addMessage("string2");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><collection><item seqnr=\"0\">string1</item><item seqnr=\"1\">string2</item></collection></root>", result);
    }

    @Test
    @Disabled("TBD: rewrite for MapRepeater and use index as key or value?")
    void testMarshallCollectionIndexAsElement() throws Exception {
        BindingModel model = new BindingModel();
        Root root = new Root(new QName("root"), ObjectTree.class);
        Repeater collection = root.setChild(new Repeater(new QName("collection"), ArrayList.class, true), "messages");
        Sequence content = collection.setItem(new Sequence(new QName("item"), false));
        content.add(new SimpleType(new QName("value"), false));
        content.add(new ElementInjector(new QName("seqnr"), Indexer.INSTANCE, false));
        model.registerRoot(root);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("string1");
        instance.addMessage("string2");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><collection><item><value>string1</value><seqnr>0</seqnr></item><item><value>string2</value><seqnr>1</seqnr></item></collection></root>", result);
    }

    @Test
    void ignoreMissingMandatoryItemWhenNilIsSetTrue() throws XMLStreamException {
        MapRepeater nillableMapRepeater = new MapRepeater(new QName("map"), TreeMap.class, false, NILLABLE);
        nillableMapRepeater.setKeyValue(new SimpleType(new QName("key"), false),
                                     new SimpleType(new QName("value"), false));

        UnmarshallResult result = UnmarshallUtils.unmarshall(nillableMapRepeater, "<map xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        assertEquals(UnmarshallResult.NO_RESULT, result);
    }
     @Test
    void writeNilElementForNullValuedNillableBinding() throws Exception {
        MapRepeater nillableMapRepeater = new MapRepeater(new QName("map"), TreeMap.class, false, NILLABLE);
        nillableMapRepeater.setKeyValue(new SimpleType(new QName("key"), false),
                                     new SimpleType(new QName("value"), false));

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableMapRepeater.toXml(staxWriter, new JavaContext(null));
        staxWriter.close();

        assertThat(writer.toString()).and("<map xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />").areIdentical();
    }
}
