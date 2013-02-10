package info.rsdev.xb4j.model.java.constructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectD;
import info.rsdev.xb4j.test.XMLStreamReaderFactory;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.junit.After;
import org.junit.Test;

public class SimpleArgsConstructorTest {
	
	public RecordAndPlaybackXMLStreamReader staxReader = null;
	
	@After
	public void teardown() throws Exception {
		if (this.staxReader != null) {
			staxReader.close(true);	//this also closes the underlying xmlstream
		}
		staxReader = null;
	}

	@Test
	public void testCreateObjectWithNextXmlElementInStream() throws XMLStreamException {
		ArgsConstructor constructor = new ArgsConstructor(ObjectA.class, new QName("name"));
		staxReader = XMLStreamReaderFactory.newReader("<objecta><name>Repelsteeltje</name></objecta>");
		staxReader.nextTag();	//kickoff reading the xml stream
		Object instance = constructor.newInstance(staxReader);
		assertNotNull(instance);
		assertSame(ObjectA.class, instance.getClass());
		assertEquals("Repelsteeltje", ((ObjectA)instance).getAName());
	}
	
	@Test
	public void testCreateObjectFromXmlElementInStreamSkippingSome() throws XMLStreamException {
		ArgsConstructor constructor = new ArgsConstructor(ObjectA.class, new QName("name"));
		staxReader = XMLStreamReaderFactory.newReader("<objecta><empty /><name>Paardenbloem</name><empty /></objecta>");
		staxReader.nextTag();	//kickoff reading the xml stream
		Object instance = constructor.newInstance(staxReader);
		assertNotNull(instance);
		assertSame(ObjectA.class, instance.getClass());
		assertEquals("Paardenbloem", ((ObjectA)instance).getAName());
	}
	
	@Test
	public void testCreateObjectFromTwoXmlElementsInStream() throws XMLStreamException {
		ArgsConstructor constructor = new ArgsConstructor(ObjectD.class, new QName("first"), new QName("last"));
		staxReader = XMLStreamReaderFactory.newReader("<objectd><first>Dave</first><last>Schoorl</last></objectd>");
		staxReader.nextTag();	//kickoff reading the xml stream
		Object instance = constructor.newInstance(staxReader);
		assertNotNull(instance);
		assertSame(ObjectD.class, instance.getClass());
		assertEquals("Schoorl, Dave", ((ObjectD)instance).getFullName());
	}
	
	@Test
	public void testCreateObjectFromTwoXmlElementsInStreamReversedOrder() throws XMLStreamException {
		ArgsConstructor constructor = new ArgsConstructor(ObjectD.class, new QName("first"), new QName("last"));
		staxReader = XMLStreamReaderFactory.newReader("<objectd><last>Schoorl</last><first>Dave</first></objectd>");
		staxReader.nextTag();	//kickoff reading the xml stream
		Object instance = constructor.newInstance(staxReader);
		assertNotNull(instance);
		assertSame(ObjectD.class, instance.getClass());
		assertEquals("Schoorl, Dave", ((ObjectD)instance).getFullName());
	}
}
