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
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.converter.NullConverter;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.test.UnmarshallUtils;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.util.XmlStreamFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Dave Schoorl
 */
public class SimpleTypeTest {

    private static final QName UNQ_ROOT = new QName("root");
    private static final QName Q_ROOT = new QName("urn:test/namespace", "root", "tst");
    private static final QName Q_TREE = new QName("urn:test/namespace/tree", "tree", "tst");
    private static final QName Q_MYOBJECT = new QName("urn:test/namespace", "myobject", "tst");

    private BindingModel model = null;
    private Root treeBinding = null;
    private Root rootBinding = null;
    private Root nestedBinding = null;

    @Before
    public void setupModel() {
        model = new BindingModel();
        model.registerRoot(new Root(Q_ROOT, Object.class));

        rootBinding = new Root(UNQ_ROOT, Object.class);
        model.registerRoot(rootBinding);

        treeBinding = new Root(Q_TREE, ObjectTree.class);
        model.registerRoot(treeBinding);

        nestedBinding = new Root(Q_MYOBJECT, ObjectA.class);
        nestedBinding.setChild(new SimpleType(new QName("name"), false), "name");
        model.registerRoot(nestedBinding);

    }

    @Test
    public void testMarshallingToEmptyElementNoNamespace() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        model.getXmlStreamer(instance.getClass(), UNQ_ROOT).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals("<root/>", stream.toString());
    }

    @Test
    public void testUnmarshallingFromEmptyElementNoNamespace() {
        byte[] buffer = "<root/>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(Object.class, instance.getClass());
    }

    @Test
    public void testMarshallingToEmptyElementWithNamespace() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        model.getXmlStreamer(instance.getClass(), Q_ROOT).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals("<tst:root xmlns:tst=\"urn:test/namespace\"/>", stream.toString());
    }

    @Test
    public void testUnmarshallingFromEmptyElementWithNamespace() {
        byte[] buffer = "<tst:root xmlns:tst=\"urn:test/namespace\"/>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(Object.class, instance.getClass());
    }

    @Test
    public void testUnmarshalFromNestedXmlWithNamespaces() {
        treeBinding.setChild(new Element(new QName("urn:test/namespace/tree", "child", "tst"), ObjectA.class, false), "myObject");

        byte[] buffer = "<tst:tree xmlns:tst=\"urn:test/namespace/tree\"><tst:child/></tst:tree>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        assertNotNull(((ObjectTree) instance).getMyObject());
    }

    @Test
    public void testMarshallNestedBinding() throws Exception {
        treeBinding.setChild(new Element(new QName("urn:test/namespace/tree", "child", "tst"), ObjectA.class, false), "myObject");

        ObjectTree instance = new ObjectTree();
        instance.setMyObject(new ObjectA("test"));
        String expected = "<tst:tree xmlns:tst=\"urn:test/namespace/tree\">" +
                          "<tst:child/>" +
                          "</tst:tree>";

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals(expected, stream.toString());
    }

    @Test
    public void testMarshallValue() throws Exception {
        ObjectA instance = new ObjectA("test");

        String expected = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>test</name></tst:myobject>";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals(expected, stream.toString());
    }

    @Test
    public void testUnmarshallValue() throws Exception {
        byte[] buffer = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>test</name></tst:myobject>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("test", ((ObjectA) instance).getAName());
    }

    @Test
    public void testMarshallValueWithDiacritics() throws Exception {
        ObjectA instance = new ObjectA("Garçon");

        String expected = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>Garçon</name></tst:myobject>";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals(expected, stream.toString());
    }

    @Test
    public void testUnmarshallValueWithDiacritics() throws Exception {
        byte[] buffer = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>Garçon</name></tst:myobject>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("Garçon", ((ObjectA) instance).getAName());
    }

    @Test
    public void testMarshallMandatorySimpleTypeNoTextWithAttributes() throws Exception {
        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        SimpleType simple = rootBinding.setChild(new SimpleType(new QName("Simple"), NullConverter.INSTANCE, false));
        simple.addAttribute(new Attribute(new QName("name")), "name");
        simple.toXml(staxWriter, new JavaContext(new ObjectA("soul")));
        staxWriter.close();

        assertEquals("<Simple name=\"soul\"/>", writer.toString());
    }

    @Test
    public void testOptionalSimpleTypeNoContentGeneratesNoOutput() {
        IBinding simple = new SimpleType(new QName("optional"), true);
        assertSame(OutputState.NO_OUTPUT, simple.generatesOutput(new JavaContext(null)));
    }

    @Test
    public void testManadatorySimpleTypeNoContentWillColaborate() {
        IBinding simple = new SimpleType(new QName("mandatory"), false);
        assertSame(OutputState.COLLABORATE, simple.generatesOutput(new JavaContext(null)));
    }

    @Test
    public void testOptionalSimpleTypeWithContentGeneratesOutput() {
        IBinding simple = new SimpleType(new QName("optional"), true);
        assertSame(OutputState.HAS_OUTPUT, simple.generatesOutput(new JavaContext("a value")));
    }

    @Test
    public void ignoreMissingMandatoryContentWhenNilIsSetTrue() throws XMLStreamException {
        SimpleType nillableSimpleType = new SimpleType(new QName("simple"), false, NILLABLE);

        UnmarshallResult result = UnmarshallUtils.unmarshall(nillableSimpleType, "<simple xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        assertEquals(UnmarshallResult.NO_RESULT, result);
    }
    
    @Test
    public void writeNilElementForNullValuedNillableBinding() throws Exception {
        SimpleType nillableSimpleType = new SimpleType(new QName("simple"), false, NILLABLE);
        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableSimpleType.toXml(staxWriter, new JavaContext(null));
        staxWriter.close();

        assertXMLEqual("<simple xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />", writer.toString());
    }
    
    @Test
    public void doNotwriteNillAttributeWhenNillableBindingEncountersAValue() throws Exception {
        SimpleType nillableSimpleType = new SimpleType(new QName("simple"), false, NILLABLE);
        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableSimpleType.toXml(staxWriter, new JavaContext("name"));
        staxWriter.close();

        assertXMLEqual("<simple>name</simple>", writer.toString());
    }
}
