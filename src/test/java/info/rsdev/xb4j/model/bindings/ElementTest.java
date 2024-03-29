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
package info.rsdev.xb4j.model.bindings;

import static info.rsdev.xb4j.model.bindings.SchemaOptions.NILLABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.FieldSetter;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.test.UnmarshallUtils;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

class ElementTest {

    RecordAndPlaybackXMLStreamReader mockReader = null;
    IBinding mockBinding = null;

    @BeforeEach
    public void setup() {
        mockReader = mock(RecordAndPlaybackXMLStreamReader.class);
        mockBinding = mock(IBinding.class);
    }

    @Test
    void testMarshallElementWithAttributesOnly() throws Exception {
        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        Root root = new Root(new QName("Root"), Object.class);
        Element element = root.setChild(new Element(new QName("Element"), false));
        element.addAttribute(new Attribute(new QName("attribute")).setRequired(true), "name");
        element.toXml(staxWriter, new JavaContext(new ObjectA("single")));
        staxWriter.close();

        assertEquals("<Element attribute=\"single\"/>", writer.toString());
    }

    @Test
    void testNoXmlElementAndNoContentGeneratesNoOutput() {
        Element element = new Element(Object.class, false);
        assertSame(OutputState.NO_OUTPUT, element.generatesOutput(new JavaContext(null)));
    }

    @Test
    void testOptionalXmlElementNoContentGeneratesNoOutput() {
        Element element = new Element(new QName("optional"), true);
        assertSame(OutputState.NO_OUTPUT, element.generatesOutput(new JavaContext(null)));
    }

    @Test
    void testMandatoryXmlElementNoContentGeneratesOutput() {
        Element element = new Element(new QName("mandatory"), false);
        assertSame(OutputState.HAS_OUTPUT, element.generatesOutput(new JavaContext(null)));
    }

    /* When an {@link Element} has no child binding, but creates a new Java object and has a setter, the new object is set on
     * the parent JavaContext.
     */
    @Test
    void setNewObjectOnJavaContextObject() throws XMLStreamException {
        JavaContext context = new JavaContext(new ObjectTree());
        Element element = (Element) new Element(ObjectA.class, false).setSetter(new FieldSetter("myObject"));
        UnmarshallResult result = element.unmarshall(mockReader, context);
        assertFalse(result.mustHandleUnmarshalledObject());
    }

    /* When an {@link Element} has no child binding, but creates a new Java object but has no setter, the new object is passed on
     * to it's parent binding to handle.
     */
    @Test
    void passNewObjectOnToParent() throws XMLStreamException {
        JavaContext context = new JavaContext(new ObjectTree());
        Element element = new Element(ObjectA.class, false);
        UnmarshallResult result = element.unmarshall(mockReader, context);
        assertTrue(result.mustHandleUnmarshalledObject());
        assertNotNull(result.getUnmarshalledObject());
        assertSame(ObjectA.class, result.getUnmarshalledObject().getClass());
    }

    /* When an {@link Element} has no child binding, but creates a new Java object but has no setter, the new object is passed on
     * to it's parent binding to handle.
     */
    @Test
    void setResultOnParentContext() throws XMLStreamException {
        JavaContext context = new JavaContext(new ObjectTree());
        when(mockBinding.toJava(any(), any())).thenReturn(UnmarshallResult.NO_RESULT);
        Element element = new Element(ObjectA.class, false);
        element.setChild(mockBinding);
        UnmarshallResult result = element.unmarshall(mockReader, context);
    }

    @Test
    void ignoreMissingMandatoryChildWhenNilIsSetTrue() throws XMLStreamException {
        Element nillableElement = new Element(new QName("elem"), ObjectA.class, false, NILLABLE);
        nillableElement.setChild(new SimpleType(new QName("mandatory"), false));

        UnmarshallResult result = UnmarshallUtils.unmarshall(nillableElement, "<elem xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        assertEquals(UnmarshallResult.NO_RESULT, result);
    }
    
    @Test
    void writeNilElementForNullValuedNillableBinding() throws Exception {
        Element nillableElement = new Element(new QName("elem"), ObjectA.class, false, NILLABLE);
        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableElement.toXml(staxWriter, new JavaContext(null));
        staxWriter.close();

        assertThat(writer.toString()).and("<elem xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />").areIdentical();
    }
    
    @Test
    void doNotwriteNillAttributeWhenNillableBindingEncountersAValue() throws Exception {
        Element nillableElement = new Element(new QName("elem"), ObjectA.class, false, NILLABLE);
        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableElement.toXml(staxWriter, new JavaContext(new ObjectA("name")));
        staxWriter.close();

        assertThat(writer.toString()).and("<elem/>").areIdentical();
    }
}
