/* Copyright 2012 Red Star Development / Dave Schoorl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        RecordAndPlaybackXMLStreamReader staxReader = new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream));
        
        Marker startMarker = staxReader.startRecording();
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("root", staxReader.getName().getLocalPart());
        
        Marker level1Marker = staxReader.startRecording();
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("level1", staxReader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());  //goto start of level2
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());  //goto start of child
        assertEquals("child", staxReader.getName().getLocalPart());
        
        staxReader.rewindAndPlayback(level1Marker);
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("level1", staxReader.getName().getLocalPart());
        
        //we can still go back to the start marker
        staxReader.rewindAndPlayback(startMarker);
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("root", staxReader.getName().getLocalPart());
    }
    
    @Test
    public void testMultiMarker() throws XMLStreamException {
        InputStream stream = new ByteArrayInputStream("<root><level1><level2><child>Dit is tekst</child></level2></level1></root>".getBytes());
        RecordAndPlaybackXMLStreamReader staxReader = new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream));
        
        Marker startMarker = staxReader.startRecording();
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("root", staxReader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("level1", staxReader.getName().getLocalPart());
        
        Marker level2Marker = staxReader.startRecording();
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());  //goto start of level2
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());  //goto start of child
        assertEquals("child", staxReader.getName().getLocalPart());
        
        //we can still go back to the start marker
        staxReader.stopRecording(level2Marker);
        assertTrue(staxReader.isMarkerObsolete(level2Marker));
        assertFalse(staxReader.isMarkerObsolete(startMarker));
        assertTrue(staxReader.isRecording());
        
        //continue reading from the stream (and recording)
        assertEquals("Dit is tekst", staxReader.getElementText());
        
        //We can now still go back to the root marker??
        staxReader.rewindAndPlayback(startMarker);
        assertTrue(staxReader.isMarkerObsolete(startMarker));
        assertFalse(staxReader.isRecording());
        
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("root", staxReader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("level1", staxReader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());  //goto start of level2
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());  //goto start of child
        assertEquals("child", staxReader.getName().getLocalPart());
    }
}
