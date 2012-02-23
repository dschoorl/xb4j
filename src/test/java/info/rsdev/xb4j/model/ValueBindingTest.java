package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import info.rsdev.xb4j.test.MyObject;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

public class ValueBindingTest {
	
	@Test
	public void testMarshallValue() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ElementBinding binding = new ElementBinding(new QName("urn:test/namespace", "root", "tst"), MyObject.class);
        binding.addChild(new ElementBinding(new QName("child"), String.class), "name");
		
        MyObject instance = new MyObject("test");
        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
        MarshallingContext context = new MarshallingContext(staxWriter);
        context.marshall(binding, instance);
        
        String expected = "<tst:root xmlns:tst=\"urn:test/namespace\">test</tst:root>";
		assertEquals(expected, stream.toString());
	}
	
}
