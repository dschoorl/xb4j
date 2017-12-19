/*
 * Copyright 2017 Red Star Development.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.xb4j.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import info.rsdev.xb4j.model.bindings.IBinding;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.UnmarshallResult;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.XmlStreamFactory;
import java.io.StringReader;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Dave Schoorl
 */
public interface UnmarshallUtils {

    /**
     * Boilerplate code to process the provided xmlSnippet into a Java object using the provided binding. The unmarshal
     * process will start without a contextObject, meaning: it is the responsibility of the binding to create all appropriate
     * Java objects.
     *
     * @param binding the {@link IBinding} that defines how the xml binds to Java. This {@link IBinding} must
     *        have a {@link Root} as parent
     * @param xmlSnippet the xml snippet to process
     * @return the {@link UnmarshallResult}
     * @throws XMLStreamException propagate exceptions that occur during the processing of the xml snippet
     */
    static UnmarshallResult unmarshall(IBinding binding, String xmlSnippet) throws XMLStreamException {
        return unmarshall(binding, (Object)null, xmlSnippet);
    }

    static <T> T unmarshall(IBinding binding, Class<T> expectedType, String xmlSnippet) throws XMLStreamException {
        return getContextObject(expectedType, unmarshall(binding, (Object)null, xmlSnippet));
    }

    /**
     * Boilerplate code to process the provided xmlSnippet into a Java object using the provided binding. The unmarshal process
     * will use the provided as it's root Java object to bind to.
     *
     * @param binding the {@link IBinding} that defines how the xml binds to Java. This {@link IBinding} must
     *        have a {@link Root} as parent
     * @param xmlSnippet the xml snippet to process
     * @param contextObject the contextObject o bind the xml unmarshaling to. When not provided, the binding is supposed to create
     *      all necessary Java instances
     * @return the {@link UnmarshallResult}
     * @throws XMLStreamException propagate exceptions that occur during the processing of the xml snippet
     */
    static UnmarshallResult unmarshall(IBinding binding, Object contextObject, String xmlSnippet) throws XMLStreamException {
        StringReader reader = new StringReader(xmlSnippet);
        RecordAndPlaybackXMLStreamReader staxWriter = new RecordAndPlaybackXMLStreamReader(XmlStreamFactory.makeReader(reader));
        return binding.toJava(staxWriter, new JavaContext(contextObject));
    }

    /**
     * Assert that the unmarshaling was successful and the result equals the given expectedObject instance
     *
     * @param expectedObject the expected instance of the unmarshaling
     * @param actualResult the {@link UnmarshallResult}
     */
    static void assertResult(Object expectedObject, UnmarshallResult actualResult) {
        assertTrue("Unexpected failure: " + actualResult, actualResult.isUnmarshallSuccessful());
        assertSame(expectedObject.getClass(), actualResult.getUnmarshalledObject().getClass());
        assertEquals(expectedObject, actualResult.getUnmarshalledObject());
    }

    /**
     * Assert that the unmarshaling was successful and the result equals the given expectedType instance
     *
     * @param <T>
     * @param expectedType the expected instance of the unmarshaling
     * @param actualResult the {@link UnmarshallResult}
     * @return
     */
    static <T> T getContextObject(Class<T> expectedType, UnmarshallResult actualResult) {
        assertTrue("Unexpected failure: " + actualResult, actualResult.isUnmarshallSuccessful());
        assertSame(expectedType, actualResult.getUnmarshalledObject().getClass());
        return (T)actualResult.getUnmarshalledObject();
    }

    static RecordAndPlaybackXMLStreamReader getStaxReader(String xmlSnippet) throws XMLStreamException {
        StringReader reader = new StringReader(xmlSnippet);
        return new RecordAndPlaybackXMLStreamReader(XmlStreamFactory.makeReader(reader));
    }

}
