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
	public void setUp() throws Exception {
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
