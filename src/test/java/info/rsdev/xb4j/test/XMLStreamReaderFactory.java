package info.rsdev.xb4j.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;

import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;

public abstract class XMLStreamReaderFactory {
	
	private XMLStreamReaderFactory() {}
	
	/**
	 * A {@link RecordAndPlaybackXMLStreamReader} is created that will can read the xmlContent. It is the 
	 * responsibility of the user to close the resources, maybe in a teardown method of a test.
	 * 
	 * @param xmlContent
	 * @return
	 */
	public static final RecordAndPlaybackXMLStreamReader newReader(String xmlContent) {
		try {
			InputStream stream = new ByteArrayInputStream(xmlContent.getBytes());
			return new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
