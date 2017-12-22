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
import static org.junit.Assert.assertSame;

import info.rsdev.xb4j.model.bindings.chooser.ContextInstanceOf;
import info.rsdev.xb4j.model.converter.IntegerConverter;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;
import info.rsdev.xb4j.test.UnmarshallUtils;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.junit.Before;
import org.junit.Test;

public class ChoiceTest {

    private Choice choice = null;

    @Before
    public void setup() {
        Root root = new Root(new QName("sigh"), Object.class);
        choice = root.setChild(new Choice(false));
        choice.addOption(new SimpleType(new QName("elem1"), false), "name", new ContextInstanceOf(ObjectA.class));
        choice.addOption(new SimpleType(new QName("elem2"), IntegerConverter.INSTANCE, false), "value", new ContextInstanceOf(ObjectB.class));
    }

    @Test
    public void testMarshallChoiceNoNamespaces() throws Exception {
        ObjectA instanceA = new ObjectA("test");
        String expected = "<elem1>test</elem1>";

        StringWriter writer = new StringWriter();
        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        choice.toXml(new SimplifiedXMLStreamWriter(staxWriter), new JavaContext(instanceA));
        assertEquals(expected, writer.toString());

        ObjectB instanceB = new ObjectB(42);
        expected = "<elem2>42</elem2>";

        writer = new StringWriter();
        staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        choice.toXml(new SimplifiedXMLStreamWriter(staxWriter), new JavaContext(instanceB));
        assertEquals(expected, writer.toString());
    }

    @Test
    public void testUnmarshallChoiceNoNamespaces() throws Exception {
        //unmarshall first option
        ByteArrayInputStream stream = new ByteArrayInputStream("<elem1>test1</elem1>".getBytes());
        RecordAndPlaybackXMLStreamReader staxWriter = new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream));
        ObjectA contextObject = new ObjectA("");
        choice.toJava(staxWriter, new JavaContext(contextObject));
        assertEquals("test1", contextObject.getAName());

        //unmarshall second option
        stream = new ByteArrayInputStream("<elem2>84</elem2>".getBytes());
        staxWriter = new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream));
        ObjectB contextObjectB = new ObjectB(42);
        choice.toJava(staxWriter, new JavaContext(contextObjectB));
        assertEquals(new Integer(84), contextObjectB.getValue());
    }

    @Test
    public void testChoiceWithoutContentNorElementGeneratesNoOutput() {
        assertSame(OutputState.NO_OUTPUT, choice.generatesOutput(new JavaContext(null)));
    }

    @Test
    public void testChoiceWithoutContentWithMandatoryElementGeneratesOutput() {
        choice = new Choice(new QName("mandatory"), false);
        assertSame(OutputState.HAS_OUTPUT, choice.generatesOutput(new JavaContext(null)));
    }

    @Test
    public void testChoiceWithoutContentWithOptionalElementGeneratesNoOutput() {
        choice = new Choice(new QName("optional"), true);
        assertSame(OutputState.NO_OUTPUT, choice.generatesOutput(new JavaContext(null)));
    }

    @Test
    public void ignoreMissingMandatoryChildrenWhenNilIsSetTrue() throws XMLStreamException {
        Choice nillableChoice = new Choice(new QName("elem"), false, NILLABLE);
        nillableChoice.addOption(new SimpleType(new QName("option"), false), "name", new ContextInstanceOf(ObjectA.class));

        UnmarshallResult result = UnmarshallUtils.unmarshall(nillableChoice, "<elem xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        assertEquals(UnmarshallResult.NO_RESULT, result);
    }
    
    @Test
    public void writeNilElementForNullValuedNillableBinding() throws Exception {
        Choice nillableChoice = new Choice(new QName("elem"), false, NILLABLE);
        nillableChoice.addOption(new SimpleType(new QName("option"), false), "name", new ContextInstanceOf(ObjectA.class));

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableChoice.toXml(staxWriter, new JavaContext(null));
        staxWriter.close();

        assertXMLEqual("<elem xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />", writer.toString());
    }
    
    @Test
    public void doNotwriteNillAttributeWhenNillableBindingEncountersAValue() throws Exception {
        Choice nillableChoice = new Choice(new QName("elem"), false, NILLABLE);
        nillableChoice.addOption(new SimpleType(new QName("option"), false), "name", new ContextInstanceOf(ObjectA.class));

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableChoice.toXml(staxWriter, new JavaContext(new ObjectA("some_name")));
        staxWriter.close();

        assertXMLEqual("<elem><option>some_name</option></elem>", writer.toString());
    }
}
