package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import info.rsdev.xb4j.test.MyObject;
import info.rsdev.xb4j.test.ObjectTree;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

public class MarshallingContextTest {

    @Test
    public void testMarshallNestedBinding() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ElementBinding binding = new ElementBinding(new QName("urn:test/namespace", "root", "tst"), ObjectTree.class);
        binding.addChild(new ElementBinding(new QName("urn:test/namespace", "child", "tst"), MyObject.class));
        
        ObjectTree instance = new ObjectTree();
        instance.setMyObject(new MyObject("test"));
        String expected = "<tst:root xmlns:tst=\"urn:test/namespace\">" +
                          "<tst:child/>" +
                          "</tst:root>";
        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
        MarshallingContext context = new MarshallingContext(staxWriter);
        context.marshall(binding, instance);
        assertEquals(expected, stream.toString());
    }
}
