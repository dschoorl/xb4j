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
package info.rsdev.xb4j.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader.Marker;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.After;
import org.junit.Test;

public class RecordAndPlaybackXMLStreamReaderTest {

    private RecordAndPlaybackXMLStreamReader staxReader = null;

    private static final String SNIPPET = "<?xml version=\"1.0\"?>"
            + "<foo:bar xmlns:foo=\"urn:test/baz\">"
            + "<!--description-->"
            + "content text"
            + "<![CDATA[<greeting>Hello</greeting>]]>"
            + "other content"
            + "</foo:bar>";

    private static final QName BAR = new QName("urn:test/baz", "bar");

    @After
    public void teardown() throws Exception {
        if (staxReader != null) {
            staxReader.close(true);	//this also closes the underlying xmlstream
        }
        staxReader = null;
    }

    @Test
    public void notAtElementStartWhenReadingHasNotStartedYet() throws XMLStreamException, IOException {
        staxReader = makeReader(SNIPPET);
        assertTrue(staxReader.isNextAnElementStart(BAR));
    }

    @Test
    public void testReadAndRereadFirstElement() throws XMLStreamException, IOException {
        staxReader = makeReader(SNIPPET);

        Marker startMarker = staxReader.startRecording();
        staxReader.nextTag();
        QName firstElement = staxReader.getName();
        assertNotNull(firstElement);
        assertEquals("foo", firstElement.getPrefix());
        assertEquals(BAR, firstElement);
        assertNull(staxReader.getAttributes());
        staxReader.rewindAndPlayback(startMarker);
        staxReader.nextTag();
        assertEquals(firstElement, staxReader.getName());
    }

    @Test
    public void testMultiMarkerRewindAndPlayBack() throws XMLStreamException {
        staxReader = makeReader("<root><level1><level2><child>Dit is tekst</child></level2></level1></root>");

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
        staxReader = makeReader("<root><level1><level2><child>Dit is tekst</child></level2></level1></root>");

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

    @Test
    public void testReadAttributes() throws XMLStreamException {
        staxReader = makeReader("<r:root name=\"test\" test:type=\"string\" xmlns:test=\"http://test/ns\" xmlns:r=\"http://root/ns\" xmlns=\"http://unused/ns\">");

        Marker startMarker = staxReader.startRecording();
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("root", staxReader.getName().getLocalPart());
        assertEquals("http://root/ns", staxReader.getName().getNamespaceURI());

        Map<QName, String> attributes = staxReader.getAttributes();
        assertNotNull(attributes);
        assertEquals(2, attributes.size());
        assertEquals("test", attributes.get(new QName("name")));
        assertEquals("string", attributes.get(new QName("http://test/ns", "type")));

        //re-read the element with the attributes
        staxReader.rewindAndPlayback(startMarker);
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("root", staxReader.getName().getLocalPart());
        attributes = staxReader.getAttributes();
        assertEquals(2, attributes.size());
    }

    @Test
    public void testRereadElementContent() throws XMLStreamException {
        staxReader = makeReader("<objectd><last>Schoorl</last><first>Dave</first></objectd>");
        assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
        assertEquals("objectd", staxReader.getName().getLocalPart());
        for (int i = 0; i < 3; i++) {
            Marker contentsOfObjectD = staxReader.startRecording();
            assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
            assertEquals("last", staxReader.getName().getLocalPart());
            assertEquals("Schoorl", staxReader.getElementText());
            assertEquals(XMLStreamReader.END_ELEMENT, staxReader.nextTag());
            assertEquals(XMLStreamReader.START_ELEMENT, staxReader.nextTag());
            assertEquals("first", staxReader.getName().getLocalPart());
            assertEquals("Dave", staxReader.getElementText());
            staxReader.rewindAndPlayback(contentsOfObjectD);
        }
    }

    @Test
    public void consumeEndTagWhenSkippingOverContent() throws XMLStreamException {
        QName root = new QName("root");
        staxReader = makeReader("<root><level1><level2><child>Dit is tekst</child></level2></level1></root>");
        assertTrue(staxReader.isNextAnElementStart(root));
        assertNotNull(staxReader.skipToElementEnd());
        assertEquals(XMLStreamReader.END_ELEMENT, staxReader.getEvent());
        assertEquals(root, staxReader.getName());
    }

    @Test
    public void consumeEndTagWhenSkippingEmptyElement() throws XMLStreamException {
        QName root = new QName("root");
        staxReader = makeReader("<root />");
        assertTrue(staxReader.isNextAnElementStart(root));
        assertNotNull(staxReader.skipToElementEnd());
        assertEquals(XMLStreamReader.END_ELEMENT, staxReader.getEvent());
        assertEquals(root, staxReader.getName());
    }

    private RecordAndPlaybackXMLStreamReader makeReader(String snippet) throws XMLStreamException {
        XMLStreamReader myReader = XmlStreamFactory.makeReader(new StringReader(snippet));
        return new RecordAndPlaybackXMLStreamReader(myReader);
    }
}
