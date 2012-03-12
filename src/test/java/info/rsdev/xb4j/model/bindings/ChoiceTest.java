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
import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.model.java.InstanceOfChooser;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

public class ChoiceTest {
	
	@Test
	public void testMarshallChoiceNoNamespaces() throws Exception {
		Choice choice = new Choice();
		choice.addChoice(new SimpleType(new QName("elem1")), "name", new InstanceOfChooser(ObjectA.class));
		choice.addChoice(new SimpleType(new QName("elem2")), "value", new InstanceOfChooser(ObjectB.class));
		
		ObjectA instanceA = new ObjectA("test");
		String expected = "<elem1>test</elem1>";
		
        StringWriter writer = new StringWriter();
        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        choice.toXml(new SimplifiedXMLStreamWriter(staxWriter), instanceA);
        assertEquals(expected, writer.toString());
        
		ObjectB instanceB = new ObjectB("test");
		expected = "<elem2>test</elem2>";
		
        writer = new StringWriter();
        staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        choice.toXml(new SimplifiedXMLStreamWriter(staxWriter), instanceB);
        assertEquals(expected, writer.toString());
	}
	
	@Test
	public void testUnmarshallChoiceNoNamespaces() throws Exception {
		Choice choice = new Choice();
		choice.addChoice(new SimpleType(new QName("elem1")), "name", new InstanceOfChooser(ObjectA.class));
		choice.addChoice(new SimpleType(new QName("elem2")), "value", new InstanceOfChooser(ObjectB.class));
		
		//unmarshall first option
		ByteArrayInputStream stream = new ByteArrayInputStream("<elem1>test1</elem1>".getBytes());
		RecordAndPlaybackXMLStreamReader staxWriter = new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream));
		ObjectA javaContext = new ObjectA("");
		choice.toJava(staxWriter, javaContext);
		assertEquals("test1", javaContext.getName());
		
		//unmarshall second option
		stream = new ByteArrayInputStream("<elem2>test2</elem2>".getBytes());
		staxWriter = new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream));
		ObjectB javaContextB = new ObjectB("");
		choice.toJava(staxWriter, javaContextB);
		assertEquals("test2", javaContextB.getValue());
	}
	
}
