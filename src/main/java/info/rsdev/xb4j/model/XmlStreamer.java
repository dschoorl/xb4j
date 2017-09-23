/* Copyright 2013 Red Star Development / Dave Schoorl
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
package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.UnmarshallResult;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader.Marker;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Stream xml to a specific Java class or the Java class to xml. The binding has been completely resolved and instances of this
 * class are threadsafe.
 *
 * @author Dave Schoorl
 */
public class XmlStreamer {

    private Root binding = null;

    public XmlStreamer(Root binding) {
        if (binding == null) {
            throw new NullPointerException("Root binding cannot be null");
        }
        this.binding = binding;
    }

    /**
     * <p>
     * Read Java object tree from the given xml stream. At least the first start element is read, in order to determine if this
     * {@link XmlStreamer} knows how to construct the given Java object tree for that element. When this {@link XmlStreamer} does
     * not know how to handle the element, an {@link Xb4jException} is thrown. At least part of the xml stream will be consumed
     * nonetheless.</p>
     * <p>
     * The {@link XMLStreamReader} is not closed; that is the responsibility of the caller.</p>
     *
     * @param staxReader the xml stream reader
     * @return the Java object tree read from the xml stream
     * @throws Xb4jException when something went wrong during unmarshalling of the xml stream
     */
    public Object toJava(XMLStreamReader staxReader) {
        RecordAndPlaybackXMLStreamReader rpbReader = null;
        try {
            rpbReader = new RecordAndPlaybackXMLStreamReader(staxReader);
            Marker startMarker = rpbReader.startRecording();
            if (rpbReader.nextTag() == XMLStreamReader.START_ELEMENT) {
                QName element = rpbReader.getName();
                rpbReader.rewindAndPlayback(startMarker);
                if (binding.getElement().equals(element)) {
                    UnmarshallResult result = binding.toJava(rpbReader, new JavaContext(null));
                    if (result.isUnmarshallSuccessful()) {
                        return result.getUnmarshalledObject();
                    } else {
                        throw new Xb4jException(result.getErrorMessage());
                    }

                } else {
                    throw new Xb4jException(String.format("%s does not know how to unmarshall xml element %s", binding, element));
                }
            }
        } catch (XMLStreamException e) {
            throw new Xb4jException("Exception occured when reading from xml stream", e);
        } finally {
            if (rpbReader != null) {
                rpbReader.close();
            }
        }
        return null;
    }

    /**
     * Marshall a Java instance into xml representation
     *
     * @param staxWriter the {@link XMLStreamWriter} to write the xml to
     * @param instance the Java object to marshall
     */
    public void toXml(XMLStreamWriter staxWriter, Object instance) {
        try {
            SimplifiedXMLStreamWriter simpleWriter = new SimplifiedXMLStreamWriter(staxWriter);
            binding.toXml(simpleWriter, new JavaContext(instance));
            simpleWriter.close();
        } catch (XMLStreamException e) {
            throw new Xb4jException("Exception occured when writing object to xml stream", e);
        }
    }

    public Class<?> getJavaType() {
        return binding.getJavaType();
    }

    public QName getElement() {
        return binding.getElement();
    }

    public BindingModel getModel() {
        return binding.getModel();
    }

}
