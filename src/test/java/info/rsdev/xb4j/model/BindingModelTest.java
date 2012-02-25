package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.test.MyObject;
import info.rsdev.xb4j.test.ObjectTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Test;

/**
 *
 * @author Dave Schoorl
 */
public class BindingModelTest {
    @Test
    public void testMarshallingToEmptyElementNoNamespace() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        BindingModel model = new BindingModel();
        model.bind(new ElementBinding(new QName("root"), Object.class));
        model.toXml(stream, instance);
        assertEquals("<root/>", stream.toString());
    }
    
    @Test
    public void testUnmarshallingFromEmptyElementNoNamespace() {
        byte[] buffer = "<root/>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        BindingModel model = new BindingModel();
        model.bind(new ElementBinding(new QName("root"), Object.class));
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(Object.class, instance.getClass());
    }
    
    @Test
    public void testMarshallingToEmptyElementWithNamespace() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        BindingModel model = new BindingModel();
        model.bind(new ElementBinding(new QName("urn:test/namespace", "root", "tst"), Object.class));
        model.toXml(stream, instance);
        assertEquals("<tst:root xmlns:tst=\"urn:test/namespace\"/>", stream.toString());
    }
    
    @Test
    public void testUnmarshallingFromEmptyElementWithNamespace() {
        byte[] buffer = "<tst:root xmlns:tst=\"urn:test/namespace\"/>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        BindingModel model = new BindingModel();
        model.bind(new ElementBinding(new QName("urn:test/namespace", "root", "tst"), Object.class));
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(Object.class, instance.getClass());
    }
    
    @Test
    public void testUnmarshalFromNestedXmlWithNamespaces() {
        BindingModel model = new BindingModel();
        ElementBinding binding = new ElementBinding(new QName("urn:test/namespace", "root", "tst"), ObjectTree.class);
        binding.addChild(new ElementBinding(new QName("urn:test/namespace", "child", "tst"), MyObject.class), "myObject");
        model.bind(binding);
        
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
        ElementBinding binding = new ElementBinding(new QName("urn:test/namespace", "root", "tst"), ObjectTree.class);
        binding.addChild(new ElementBinding(new QName("urn:test/namespace", "child", "tst"), MyObject.class), "myObject");
        model.bind(binding);
        
        ObjectTree instance = new ObjectTree();
        instance.setMyObject(new MyObject("test"));
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
        ElementBinding binding = new ElementBinding(new QName("urn:test/namespace", "myobject", "tst"), MyObject.class);
        binding.addChild(new ValueBinding(new QName("name")), "name");
        model.bind(binding);
        
        MyObject instance = new MyObject("test");
        
        String expected = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>test</name></tst:myobject>";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, instance);
        assertEquals(expected, stream.toString());
    }
    
    @Test
    public void testUnmarshallValue() throws Exception {
        BindingModel model = new BindingModel();
        ElementBinding binding = new ElementBinding(new QName("urn:test/namespace", "myobject", "tst"), MyObject.class);
        binding.addChild(new ValueBinding(new QName("name")), "name");
        model.bind(binding);
        
        byte[] buffer = "<tst:myobject xmlns:tst=\"urn:test/namespace\"><name>test</name></tst:myobject>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(MyObject.class, instance.getClass());
        assertEquals("test", ((MyObject)instance).getName());
    }
}
