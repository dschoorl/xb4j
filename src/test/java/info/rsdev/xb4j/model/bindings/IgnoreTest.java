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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;

public class IgnoreTest {
	
	@Before
	public void setup() throws Exception {
		BindingModel model = new BindingModel();
		Root root = new Root(new QName("root"), ObjectA.class);
		root.setChild(new Ignore(new QName("ignore-me")));
		model.register(root);
	}
	
	@Test
	public void testMarshallWithoutAccessors() {
		BindingModel model = new BindingModel();
		Root root = new Root(new QName("root"), ObjectA.class);
		root.addAttribute(new Attribute(new QName("name")), "name");
		root.setChild(new Ignore(new QName("ignore-me")));
		model.register(root);
		
		String snippet = "<root name='Repelsteeltje'>" +
						 "  <ignore-me please='true'><leaf /><nested><nested hasAttribute='true'><leaf /></nested></nested></ignore-me>" +
						 "</root>";
		
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("Repelsteeltje", ((ObjectA)instance).getAName());
	}
	
	@Test
	public void testMarshallWithAccessors() {
		BindingModel model = new BindingModel();
		Root root = new Root(new QName("root"), ObjectA.class);
		root.addAttribute(new Attribute(new QName("name")), "name");
		root.setChild(new Ignore(new QName("ignore-me")), "nonExistentProperty");
		model.register(root);
		
		String snippet = "<root name='Repelsteeltje'>" +
						 "  <ignore-me please='true'><leaf /><nested><nested hasAttribute='true'><leaf /></nested></nested></ignore-me>" +
						 "</root>";
		
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("Repelsteeltje", ((ObjectA)instance).getAName());
	}
	
	@Test
	public void testMarshallOptionally() {
		BindingModel model = new BindingModel();
		Root root = new Root(new QName("root"), ObjectA.class);
		root.addAttribute(new Attribute(new QName("name")), "name");
		root.setChild(new Ignore(new QName("ignore-me"), true), "nonExistentProperty");
		model.register(root);
		
		String snippet = "<root name='Repelsteeltje' />";
		
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("Repelsteeltje", ((ObjectA)instance).getAName());
	}
	
	@Test
	public void testUnmarshallWithAccessors() throws Exception {
		BindingModel model = new BindingModel();
		Root root = new Root(new QName("root"), ObjectA.class);
		root.addAttribute(new Attribute(new QName("name")), "name");
		root.setChild(new Ignore(new QName("ignore-me")), "nonExistentProperty");
		model.register(root);
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, new ObjectA("Repelsteeltje"));
        String expected = "<root name='Repelsteeltje' />";
        
        XMLAssert.assertXMLEqual(expected, stream.toString());
	}
	
	@Test
	public void testUnmarshallWithoutAccessors() throws Exception {
		BindingModel model = new BindingModel();
		Root root = new Root(new QName("root"), ObjectA.class);
		root.addAttribute(new Attribute(new QName("name")), "name");
		root.setChild(new Ignore(new QName("ignore-me")));
		model.register(root);
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, new ObjectA("Repelsteeltje"));
        String expected = "<root name='Repelsteeltje' />";
        
        XMLAssert.assertXMLEqual(expected, stream.toString());
	}
	
}
