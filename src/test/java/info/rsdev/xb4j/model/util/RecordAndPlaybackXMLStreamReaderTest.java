package info.rsdev.xb4j.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RecordAndPlaybackXMLStreamReaderTest {
    
    private RecordAndPlaybackXMLStreamReader reader = null;
    
    private InputStream stream = null;
    
    @Before
    public void setup() throws Exception {
        String xml = "<?xml version=\"1.0\"?>" + 
                     "<foo:bar xmlns:foo=\"urn:test/baz\">" +
        		        "<!--description-->" +
        		        "content text" +
        		        "<![CDATA[<greeting>Hello</greeting>]]>" +
        		        "other content" +
        		     "</foo:bar>";
        this.stream = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader staxReader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        this.reader = new RecordAndPlaybackXMLStreamReader(staxReader);
    }
    
    @After
    public void tearDown() throws Exception {
        this.reader.close();
        this.stream.close();
    }
    
    @Test
    public void testReadAndRereadFirstElement() throws XMLStreamException {
        this.reader.startRecording();
        this.reader.nextTag();
        QName firstElement = this.reader.getName();
        assertNotNull(firstElement);
        assertEquals("bar", firstElement.getLocalPart());
        assertEquals("foo", firstElement.getPrefix());
        assertEquals("urn:test/baz", firstElement.getNamespaceURI());
        this.reader.rewindAndPlayback();
        this.reader.nextTag();
        assertEquals(firstElement, this.reader.getName());
    }

}
