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
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.util.XmlStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;

public class IgnoreTest {
    
    private BindingModel model = null;
    private Root root = null;
	
	@Before
	public void setup() throws Exception {
		model = new BindingModel();
		root = new Root(new QName("root"), ObjectA.class);
        root.addAttribute(new Attribute(new QName("name")).setRequired(false), "name");
		model.register(root);
	}
	
	@Test
	public void testMarshallWithoutAccessors() {
	    //the root is only made read-only after first use (marshall/unmarshall)
        root.setChild(new Ignore(new QName("ignore-me")));
        
		String snippet = "<root name='Repelsteeltje'>" +
						 "  <ignore-me please='true'><leaf /><nested><nested hasAttribute='true'><leaf /></nested></nested></ignore-me>" +
						 "</root>";
		
        unmarshallAndAssert(snippet);
	}
	
	@Test
	public void testMarshallWithAccessors() {
        //the root is only made read-only after first use (marshall/unmarshall)
		root.setChild(new Ignore(new QName("ignore-me")), "nonExistentProperty");
		
		String snippet = "<root name='Repelsteeltje'>" +
						 "  <ignore-me please='true'><leaf /><nested><nested hasAttribute='true'><leaf /></nested></nested></ignore-me>" +
						 "</root>";
		unmarshallAndAssert(snippet);
	}
	
	@Test
	public void testMarshallOptionally() {
        //the root is only made read-only after first use (marshall/unmarshall)
		root.setChild(new Ignore(new QName("ignore-me"), true), "nonExistentProperty");
		
		unmarshallAndAssert("<root name='Repelsteeltje' />");
	}
	
	@Test
	public void testMarshallIgnoreRepeatedly() {
        //the root is only made read-only after first use (marshall/unmarshall)
        Sequence content = root.setChild(new Sequence());
        content.add(new Ignore(new QName("ignore-me"), true), "nonExistentProperty");
        content.add(new SimpleType(new QName("name")), "name"); //test it with a trailing sibling that must not be ignored
	    
        String snippet = "<root>" +
                "  <ignore-me>please</ignore-me>" + 
                "  <ignore-me>too</ignore-me>" +
                "  <name>Repelsteeltje</name>" +
                "</root>";
        unmarshallAndAssert(snippet);

	}
	
	@Test
	public void testUnmarshallWithAccessors() throws Exception {
        //the root is only made read-only after first use (marshall/unmarshall)
		root.setChild(new Ignore(new QName("ignore-me")), "nonExistentProperty");
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.getXmlStreamer(ObjectA.class, null).toXml(XmlStreamFactory.makeWriter(stream), new ObjectA("Repelsteeltje"));
        String expected = "<root name='Repelsteeltje' />";
        
        XMLAssert.assertXMLEqual(expected, stream.toString());
	}
	
	@Test
	public void testUnmarshallWithoutAccessors() throws Exception {
        //the root is only made read-only after first use (marshall/unmarshall)
		root.setChild(new Ignore(new QName("ignore-me")));
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.getXmlStreamer(ObjectA.class, null).toXml(XmlStreamFactory.makeWriter(stream), new ObjectA("Repelsteeltje"));
        String expected = "<root name='Repelsteeltje' />";
        
        XMLAssert.assertXMLEqual(expected, stream.toString());
	}
	
	private void unmarshallAndAssert(String snippet) {
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("Repelsteeltje", ((ObjectA)instance).getAName());

	}
}
