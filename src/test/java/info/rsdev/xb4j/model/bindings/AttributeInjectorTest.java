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
import info.rsdev.xb4j.model.bindings.action.IMarshallingAction;
import info.rsdev.xb4j.model.converter.NullConverter;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.test.FixedValueTestAction;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;

import org.junit.Before;
import org.junit.Test;

public class AttributeInjectorTest {
	
	private IMarshallingAction action = null;
	
	private StringWriter writer = null;
	
	private SimplifiedXMLStreamWriter staxWriter = null;	
	
	@Before
	public void setup() throws Exception {
		action = new FixedValueTestAction();
        writer = new StringWriter();
        staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
	}
	
	@Test
	public void testToXml() throws Exception {
		Root root = new Root(new QName("Root"), Object.class);
		SimpleType simpleElement = root.setChild(new SimpleType(new QName("Simple"), NullConverter.INSTANCE));
		simpleElement.addAttribute(new AttributeInjector(new QName("attribute"), action), NoGetter.INSTANCE, NoSetter.INSTANCE);
		simpleElement.toXml(staxWriter, new JavaContext(new ObjectA("true")));
		staxWriter.close();
		
		assertEquals("<Simple attribute=\"Fixed value\"/>", this.writer.toString());
	}
	
}
