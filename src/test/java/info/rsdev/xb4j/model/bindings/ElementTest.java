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

import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.FieldSetter;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import java.io.StringWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElementTest {

    RecordAndPlaybackXMLStreamReader mockReader = null;
    IBinding mockBinding = null;

    @Before
    public void setup() {
        mockReader = mock(RecordAndPlaybackXMLStreamReader.class);
        mockBinding = mock(IBinding.class);
    }

    @Test
    public void testMarshallElementWithAttributesOnly() throws Exception {
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
    public void testNoXmlElementAndNoContentGeneratesNoOutput() {
        Element element = new Element(Object.class, false);
        assertSame(OutputState.NO_OUTPUT, element.generatesOutput(new JavaContext(null)));
    }

    @Test
    public void testOptionalXmlElementNoContentGeneratesNoOutput() {
        Element element = new Element(new QName("optional"), true);
        assertSame(OutputState.NO_OUTPUT, element.generatesOutput(new JavaContext(null)));
    }

    @Test
    public void testMandatoryXmlElementNoContentGeneratesOutput() {
        Element element = new Element(new QName("mandatory"), false);
        assertSame(OutputState.HAS_OUTPUT, element.generatesOutput(new JavaContext(null)));
    }

    /* When an {@link Element} has no child binding, but creates a new Java object and has a setter, the new object is set on 
     * the parent JavaContext.
     */
    @Test
    public void setNewObjectOnJavaContextObject() throws XMLStreamException {
        JavaContext context = new JavaContext(new ObjectTree());
        Element element = (Element) new Element(ObjectA.class, false).setSetter(new FieldSetter("myObject"));
        UnmarshallResult result = element.unmarshall(mockReader, context);
        assertFalse(result.mustHandleUnmarshalledObject());
    }

    /* When an {@link Element} has no child binding, but creates a new Java object but has no setter, the new object is passed on
     * to it's parent binding to handle.
     */
    @Test
    public void passNewObjectOnToParent() throws XMLStreamException {
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
    public void setResultOnParentContext() throws XMLStreamException {
        JavaContext context = new JavaContext(new ObjectTree());
        when(mockBinding.toJava(any(), any())).thenReturn(UnmarshallResult.NO_RESULT);
        Element element = new Element(ObjectA.class, false);
        element.setChild(mockBinding);
        UnmarshallResult result = element.unmarshall(mockReader, context);
    }
}
