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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectC;
import info.rsdev.xb4j.test.UnmarshallUtils;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.util.XmlStreamFactory;

class SequenceTest {

    @Test
    void testMarshallMultipleElementsNoNamespace() {
        BindingModel model = new BindingModel();
        Root root = model.registerRoot(new Root(new QName("root"), ObjectC.class));
        Sequence sequence = root.setChild(new Sequence(false));
        sequence.add(new SimpleType(new QName("naam"), false), "name");
        sequence.add(new SimpleType(new QName("omschrijving"), false), "description");

        ObjectC instance = new ObjectC().setAName("tester").setDescription("Ik test dingen");

        String expected = "<root><naam>tester</naam><omschrijving>Ik test dingen</omschrijving></root>";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals(expected, stream.toString());
    }

    @Test
    void testUnmarshallIncompleteSequence() throws Exception {
        Root root = new Root(new QName("root"), ObjectC.class);
        Sequence sequence = root.setChild(new Sequence(false));
        sequence.add(new SimpleType(new QName("name"), false), "name");
        sequence.add(new SimpleType(new QName("description"), false), "description");

        ByteArrayInputStream stream = new ByteArrayInputStream(
                "<root><name>Jan</name><initialized>true</initialized></root>".getBytes());
        RecordAndPlaybackXMLStreamReader staxReader = new RecordAndPlaybackXMLStreamReader(
                XMLInputFactory.newInstance().createXMLStreamReader(stream));
        UnmarshallResult result = root.toJava(staxReader, new JavaContext(null));
        assertNotNull(result);
        assertFalse(result.isUnmarshallSuccessful());
        assertNull(result.getUnmarshalledObject());
        assertEquals("Mandatory element not encountered in xml: description", result.getErrorMessage());
    }

    @Test
    void testOutputSequenceWithNullJavaContextAndMandatoryChild() {
        Root root = new Root(new QName("container"), Object.class);
        Sequence sequence = root.setChild(new Sequence(true));
        sequence.add(new SimpleType(new QName("name"), true), "name");
        sequence.add(new SimpleType(new QName("description"), false), "description"); // mandatory: will output empty description
                                                                                      // tag??
        assertSame(OutputState.NO_OUTPUT, sequence.generatesOutput(new JavaContext(null)));
    }

    @Test
    void testOutputEmptyOptionalSequence() throws Exception {
        Root root = new Root(new QName("container"), Object.class);
        Sequence sequence = root.setChild(new Sequence(true));
        sequence.add(new SimpleType(new QName("name"), true), "name");
        sequence.add(new SimpleType(new QName("description"), true), "description");
        assertSame(OutputState.NO_OUTPUT, sequence.generatesOutput(new JavaContext(new ObjectC())));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
        sequence.toXml(new SimplifiedXMLStreamWriter(staxWriter), new JavaContext(new ObjectC()));
        assertEquals("", stream.toString());
    }

    @Test
    void surpressEmptyOptionalSequenceWithMandatoryChild() {
        Root root = new Root(new QName("container"), ObjectC.class);
        Sequence sequence = root.setChild(new Sequence(true));
        sequence.add(new SimpleType(new QName("name"), true), "name");
        sequence.add(new SimpleType(new QName("description"), true), "description"); // mandatory: will output empty description
                                                                                     // tag??
        assertSame(OutputState.NO_OUTPUT, sequence.generatesOutput(new JavaContext(new ObjectC())));
    }

    @Test
    void ignoreMissingMandatoryChildrenWhenNilIsSetTrue() throws XMLStreamException {
        Sequence nillableSequence = new Sequence(new QName("seq"), ObjectA.class, false, NILLABLE);
        nillableSequence.add(new SimpleType(new QName("mandatory"), false), "name");

        UnmarshallResult result = UnmarshallUtils.unmarshall(nillableSequence,
                "<seq xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        assertEquals(UnmarshallResult.NO_RESULT, result);
    }

    @Test
    void writeNilElementForNullValuedNillableBinding() throws Exception {
        Sequence nillableSequence = new Sequence(new QName("seq"), ObjectA.class, false, NILLABLE);
        nillableSequence.add(new SimpleType(new QName("mandatory"), false), "name");

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(
                XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableSequence.toXml(staxWriter, new JavaContext(null));
        staxWriter.close();

        assertThat(writer.toString()).and("<seq xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />")
                .areIdentical();
    }

    @Test
    void doNotwriteNillAttributeWhenNillableBindingEncountersAValue() throws Exception {
        Sequence nillableSequence = new Sequence(new QName("seq"), ObjectA.class, false, NILLABLE);
        nillableSequence.add(new SimpleType(new QName("mandatory"), false), "name");

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(
                XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableSequence.toXml(staxWriter, new JavaContext(new ObjectA("name")));
        staxWriter.close();

        assertThat(writer.toString()).and("<seq><mandatory>name</mandatory></seq>").areIdentical();
    }
}
