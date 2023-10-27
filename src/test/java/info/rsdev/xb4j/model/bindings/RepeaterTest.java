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
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

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
class RepeaterTest {

    @Test
    void testMarshallValueCollectionNoContainerElement() {
        // fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        Repeater collection = root.setChild(new Repeater(ArrayList.class, false), "messages");
        collection.setItem(new SimpleType(new QName("detail"), false));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("bericht1");
        instance.addMessage("bericht2");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><detail>bericht1</detail><detail>bericht2</detail></root>", result);
    }

    @Test
    void testMarshallRootAsCollectionType() {
        // fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ArrayList.class));
        Repeater collection = root.setChild(new Repeater(false));
        collection.setItem(new SimpleType(new QName("detail"), false));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ArrayList<String> instance = new ArrayList<>();
        instance.add("bericht1");
        instance.add("bericht2");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><detail>bericht1</detail><detail>bericht2</detail></root>", result);
    }

    @Test
    void testMarshallExpectParentBindingToProvideCollectionButNoCollection() {
        // fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        Repeater collection = root.setChild(new Repeater(false)); // Root binding must represent collection type, but does not
        collection.setItem(new SimpleType(new QName("detail"), false));

        assertThrows(Xb4jMarshallException.class, () -> model.getXmlStreamer(ObjectTree.class, null)
                .toXml(XmlStreamFactory.makeWriter(new ByteArrayOutputStream()), new ObjectTree()));
    }

    @Test
    void testMarshallValueCollectionWithContainerElement() {
        // fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        Repeater collection = root.setChild(new Repeater(new QName("collection"), ArrayList.class, true), "messages");
        collection.setItem(new SimpleType(new QName("detail"), false));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("bericht1");
        instance.addMessage("bericht2");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><collection><detail>bericht1</detail><detail>bericht2</detail></collection></root>", result);
    }

    @Test
    void testUnmarshallValueCollectionNoContainerElement() {
        // fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        Repeater collection = root.setChild(new Repeater(ArrayList.class, false), "messages");
        collection.setItem(new SimpleType(new QName("detail"), false));

        ByteArrayInputStream stream = new ByteArrayInputStream(
                "<root><detail>bericht1</detail><detail>bericht2</detail></root>".getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree) instance;
        assertNotNull(tree.getMessages());
        assertEquals(2, tree.getMessages().size());
        assertArrayEquals(new String[] { "bericht1", "bericht2" }, tree.getMessages().toArray());
    }

    @Test
    void testUnmarshallRootAsCollectionType() {
        // fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ArrayList.class));
        Repeater collection = root.setChild(new Repeater(false));
        collection.setItem(new SimpleType(new QName("detail"), false));

        ByteArrayInputStream stream = new ByteArrayInputStream(
                "<root><detail>bericht1</detail><detail>bericht2</detail></root>".getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ArrayList.class, instance.getClass());
        ArrayList<?> list = (ArrayList<?>) instance;
        assertEquals(2, list.size());
        assertEquals(Arrays.asList("bericht1", "bericht2"), list);
    }

    @Test
    void testUnmarshallExpectParentBindingToProvideCollectionButNoCollection() {
        // fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        Repeater collection = root.setChild(new Repeater(false)); // Root binding must represent collection type, but does not
        collection.setItem(new SimpleType(new QName("detail"), false));

        ByteArrayInputStream stream = new ByteArrayInputStream(
                "<root><detail>bericht1</detail><detail>bericht2</detail></root>".getBytes());
        assertThrows(Xb4jUnmarshallException.class, () -> model.toJava(XmlStreamFactory.makeReader(stream)));
    }

    @Test
    void testUnmarshallValueCollectionWithContainerElement() {
        // fixture
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectTree.class));
        Repeater collection = root.setChild(new Repeater(new QName("collection"), ArrayList.class, true), "messages");
        collection.setItem(new SimpleType(new QName("detail"), false));

        ByteArrayInputStream stream = new ByteArrayInputStream(
                "<root><collection><detail>bericht1</detail><detail>bericht2</detail></collection></root>".getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree) instance;
        assertNotNull(tree.getMessages());
        assertEquals(2, tree.getMessages().size());
        assertArrayEquals(new String[] { "bericht1", "bericht2" }, tree.getMessages().toArray());
    }

    @Test
    void testRepeaterNoCollectionNoElementGeneratesNoOutput() {
        Repeater repeater = new Repeater(ArrayList.class, false);
        assertSame(OutputState.NO_OUTPUT, repeater.generatesOutput(new JavaContext(null)));
    }

    @Test
    void testRepeaterEmptyCollectionNoElementGeneratesNoOutput() {
        Repeater repeater = new Repeater(ArrayList.class, false);
        assertSame(OutputState.NO_OUTPUT, repeater.generatesOutput(new JavaContext(new ArrayList<>())));
    }

    @Test
    void testRepeaterEmptyCollectionOptionalElementGeneratesNoOutput() {
        Repeater repeater = new Repeater(new QName("optional"), ArrayList.class, true);
        assertSame(OutputState.NO_OUTPUT, repeater.generatesOutput(new JavaContext(new ArrayList<>())));
    }

    @Test
    void testRepeaterEmptyCollectionMandatoryElementGeneratesNoOutput() {
        Repeater repeater = new Repeater(new QName("mandatory"), ArrayList.class, false);
        assertSame(OutputState.HAS_OUTPUT, repeater.generatesOutput(new JavaContext(new ArrayList<>())));
    }

    @Test
    void testMarshallCollectionIndexAsAttribute() throws Exception {
        // work on a collection of Strings
        BindingModel model = new BindingModel();
        Root root = new Root(new QName("root"), ObjectTree.class);
        Repeater collection = root.setChild(new Repeater(new QName("collection"), ArrayList.class, true), "messages");
        SimpleType item = collection.setItem(new SimpleType(new QName("item"), false));
        item.addAttribute(new AttributeInjector(new QName("seqnr"), Indexer.INSTANCE), NoGetter.INSTANCE, NoSetter.INSTANCE);
        model.registerRoot(root);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("string1");
        instance.addMessage("string2");

        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><collection><item seqnr=\"0\">string1</item><item seqnr=\"1\">string2</item></collection></root>",
                result);
    }

    @Test
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
        assertEquals(
                "<root><collection><item><value>string1</value><seqnr>0</seqnr></item><item><value>string2</value><seqnr>1</seqnr></item></collection></root>",
                result);
    }

    @Test
    void ignoreMissingMandatoryItemWhenNilIsSetTrue() throws XMLStreamException {
        Repeater nillableRepeater = new Repeater(new QName("elem"), ArrayList.class, false, NILLABLE);
        nillableRepeater.setItem(new SimpleType(new QName("mandatory"), false));

        UnmarshallResult result = UnmarshallUtils.unmarshall(nillableRepeater,
                "<elem xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        assertEquals(UnmarshallResult.NO_RESULT, result);
    }

    @Test
    void writeNilElementForNullValuedNillableBinding() throws Exception {
        Repeater nillableRepeater = new Repeater(new QName("elem"), ArrayList.class, false, NILLABLE);
        nillableRepeater.setItem(new SimpleType(new QName("mandatory"), false));

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(
                XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableRepeater.toXml(staxWriter, new JavaContext(null));
        staxWriter.close();

        assertThat(writer.toString()).and("<elem xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />")
                .areIdentical();
    }

    @Test
    void doNotwriteNillAttributeWhenNillableBindingEncountersAValue() throws Exception {
        Repeater nillableRepeater = new Repeater(new QName("elem"), ArrayList.class, false, NILLABLE);
        nillableRepeater.setItem(new SimpleType(new QName("mandatory"), false));

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(
                XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        List<String> collection = Arrays.asList("some_value");
        nillableRepeater.toXml(staxWriter, new JavaContext(collection));
        staxWriter.close();

        assertThat(writer.toString()).and("<elem><mandatory>some_value</mandatory></elem>").areIdentical();
    }
}
