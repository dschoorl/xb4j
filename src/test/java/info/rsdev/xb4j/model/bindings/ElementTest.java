package info.rsdev.xb4j.model.bindings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import org.junit.Test;

public class ElementTest {

	@Test
	public void testNoXmlElementAndNoContentGeneratesNoOutput() {
		Element element = (Element)new Element(Object.class);
		assertFalse(element.generatesOutput(null));
	}

	@Test
	public void testOptionalXmlElementNoContentGeneratesNoOutput() {
		Element element = (Element)new Element(new QName("optional")).setOptional(true);
		assertFalse(element.generatesOutput(null));
	}

	@Test
	public void testMandatoryXmlElementNoContentGeneratesOutput() {
		Element element = (Element)new Element(new QName("mandatory"));
		assertTrue(element.generatesOutput(null));
	}

}
