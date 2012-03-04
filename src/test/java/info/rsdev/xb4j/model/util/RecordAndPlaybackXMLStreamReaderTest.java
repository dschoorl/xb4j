package info.rsdev.xb4j.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader.Marker;

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
        Marker startMarker = this.reader.startRecording();
        this.reader.nextTag();
        QName firstElement = this.reader.getName();
        assertNotNull(firstElement);
        assertEquals("bar", firstElement.getLocalPart());
        assertEquals("foo", firstElement.getPrefix());
        assertEquals("urn:test/baz", firstElement.getNamespaceURI());
        this.reader.rewindAndPlayback(startMarker);
        this.reader.nextTag();
        assertEquals(firstElement, this.reader.getName());
    }

    @Test
    public void testMultiMarkerRewindAndPlayBack() throws XMLStreamException {
        InputStream stream = new ByteArrayInputStream("<root><level1><level2><child>Dit is tekst</child></level2></level1></root>".getBytes());
        XMLStreamReader staxReader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        RecordAndPlaybackXMLStreamReader reader = new RecordAndPlaybackXMLStreamReader(staxReader);
        
        Marker startMarker = reader.startRecording();
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());
        assertEquals("root", reader.getName().getLocalPart());
        
        Marker level1Marker = reader.startRecording();
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());
        assertEquals("level1", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());  //goto start of level2
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());  //goto start of child
        assertEquals("child", reader.getName().getLocalPart());
        
        reader.rewindAndPlayback(level1Marker);
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());
        assertEquals("level1", reader.getName().getLocalPart());
        
        //we can still go back to the start marker
        reader.rewindAndPlayback(startMarker);
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());
        assertEquals("root", reader.getName().getLocalPart());
    }
    
    @Test
    public void testMultiMarker() throws XMLStreamException {
        InputStream stream = new ByteArrayInputStream("<root><level1><level2><child>Dit is tekst</child></level2></level1></root>".getBytes());
        XMLStreamReader staxReader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        RecordAndPlaybackXMLStreamReader reader = new RecordAndPlaybackXMLStreamReader(staxReader);
        
        Marker startMarker = reader.startRecording();
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());
        assertEquals("level1", reader.getName().getLocalPart());
        
        Marker level2Marker = reader.startRecording();
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());  //goto start of level2
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());  //goto start of child
        assertEquals("child", reader.getName().getLocalPart());
        
        //we can still go back to the start marker
        reader.stopRecording(level2Marker);
        assertTrue(reader.isMarkerObsolete(level2Marker));
        assertFalse(reader.isMarkerObsolete(startMarker));
        assertTrue(reader.isRecording());
        
        //continue reading from the stream (and recording)
        assertEquals("Dit is tekst", reader.getElementText());
        
        //We can now still go back to the root marker??
        reader.rewindAndPlayback(startMarker);
        assertTrue(reader.isMarkerObsolete(startMarker));
        assertFalse(reader.isRecording());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());
        assertEquals("root", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());
        assertEquals("level1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());  //goto start of level2
        assertEquals(XMLStreamReader.START_ELEMENT, reader.nextTag());  //goto start of child
        assertEquals("child", reader.getName().getLocalPart());
    }
}
