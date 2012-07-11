package info.rsdev.xb4j.model.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;

import org.junit.Test;

public class ElementTest {
	
    @Test
    public void testMarshallElementWithAttributesOnly() throws Exception {
    	StringWriter writer = new StringWriter();
    	SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
    	
    	Element element = new Element(new QName("Element"));
    	element.addAttribute(new Attribute(new QName("attribute")).setRequired(true), "name");
    	element.toXml(staxWriter, new JavaContext(new ObjectA("single")));
    	staxWriter.close();
    	
    	assertEquals("<Element attribute=\"single\"/>",writer.toString());
    }

	@Test
	public void testNoXmlElementAndNoContentGeneratesNoOutput() {
		Element element = (Element)new Element(Object.class);
		assertFalse(element.generatesOutput(new JavaContext(null)));
	}

	@Test
	public void testOptionalXmlElementNoContentGeneratesNoOutput() {
		Element element = (Element)new Element(new QName("optional")).setOptional(true);
		assertFalse(element.generatesOutput(new JavaContext(null)));
	}

	@Test
	public void testMandatoryXmlElementNoContentGeneratesOutput() {
		Element element = (Element)new Element(new QName("mandatory"));
		assertTrue(element.generatesOutput(new JavaContext(null)));
	}

}
