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
        binding.addChild(new ElementBinding(new QName("urn:test/namespace", "child", "tst"), MyObject.class));
        model.bind(binding);
        
        byte[] buffer = "<tst:root xmlns:tst=\"urn:test/namespace\"><tst:child/></tst:root>".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        assertNotNull(((ObjectTree)instance).getMyObject());
    }
    
}
