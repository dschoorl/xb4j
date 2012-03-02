package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Test;

/**
 *
 * @author Dave Schoorl
 */
public class SimpleBindingModelTest {
	
    @Test
    public void testMarshallingToEmptyElementNoNamespace() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        BindingModel model = new BindingModel();
        model.register(new RootBinding(new QName("root"), Object.class));
        model.toXml(stream, instance);
        assertEquals("<root/>", stream.toString());
    }
    
    @Test
    public void testUnmarshallingFromEmptyElementNoNamespace() {
        byte[] buffer = "<root/>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        BindingModel model = new BindingModel();
        model.register(new RootBinding(new QName("root"), Object.class));
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(Object.class, instance.getClass());
    }
    
    @Test
    public void testMarshallingToEmptyElementWithNamespace() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        BindingModel model = new BindingModel();
        model.register(new RootBinding(new QName("urn:test/namespace", "root", "tst"), Object.class));
        model.toXml(stream, instance);
        assertEquals("<tst:root xmlns:tst=\"urn:test/namespace\"/>", stream.toString());
    }
    
    @Test
    public void testUnmarshallingFromEmptyElementWithNamespace() {
        byte[] buffer = "<tst:root xmlns:tst=\"urn:test/namespace\"/>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        BindingModel model = new BindingModel();
        model.register(new RootBinding(new QName("urn:test/namespace", "root", "tst"), Object.class));
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(Object.class, instance.getClass());
    }
    
    @Test
    public void testUnmarshalFromNestedXmlWithNamespaces() {
        BindingModel model = new BindingModel();
        RootBinding binding = new RootBinding(new QName("urn:test/namespace", "root", "tst"), ObjectTree.class);
        binding.setChild(new ElementBinding(new QName("urn:test/namespace", "child", "tst"), ObjectA.class), "myObject");
        model.register(binding);
        
        byte[] buffer = "<tst:root xmlns:tst=\"urn:test/namespace\"><tst:child/></tst:root>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        assertNotNull(((ObjectTree)instance).getMyObject());
    }
    
    @Test
    public void testMarshallNestedBinding() throws Exception {
        BindingModel model = new BindingModel();
        RootBinding binding = new RootBinding(new QName("urn:test/namespace", "root", "tst"), ObjectTree.class);
        binding.setChild(new ElementBinding(new QName("urn:test/namespace", "child", "tst"), ObjectA.class), "myObject");
        model.register(binding);
        
        ObjectTree instance = new ObjectTree();
        instance.setMyObject(new ObjectA("test"));
        String expected = "<tst:root xmlns:tst=\"urn:test/namespace\">" +
                          "<tst:child/>" +
                          "</tst:root>";
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, instance);
        assertEquals(expected, stream.toString());
    }
    
    @Test
    public void testMarshallValue() throws Exception {
        BindingModel model = new BindingModel();
        RootBinding binding = new RootBinding(new QName("urn:test/namespace", "myobject", "tst"), ObjectA.class);
        binding.setChild(new SimpleTypeBinding(new QName("name")), "name");
        model.register(binding);
        
        ObjectA instance = new ObjectA("test");
        
        String expected = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>test</name></tst:myobject>";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, instance);
        assertEquals(expected, stream.toString());
    }
    
    @Test
    public void testUnmarshallValue() throws Exception {
        BindingModel model = new BindingModel();
        RootBinding binding = new RootBinding(new QName("urn:test/namespace", "myobject", "tst"), ObjectA.class);
        binding.setChild(new SimpleTypeBinding(new QName("name")), "name");
        model.register(binding);
        
        byte[] buffer = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>test</name></tst:myobject>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("test", ((ObjectA)instance).getName());
    }
}
