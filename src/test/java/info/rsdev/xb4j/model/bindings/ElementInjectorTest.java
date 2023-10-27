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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.model.bindings.action.IMarshallingAction;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.FixedValueTestAction;
import info.rsdev.xb4j.test.NullValueTestAction;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

class ElementInjectorTest {

    private IMarshallingAction action = null;

    private StringWriter writer = null;

    private SimplifiedXMLStreamWriter staxWriter = null;

    @BeforeEach
    public void setUp() throws Exception {
        action = new FixedValueTestAction();
        writer = new StringWriter();
        staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
    }

    @Test
    void testInjectText() throws Exception {
        ElementInjector xmlInjector = new ElementInjector(new QName("Injected"), action, false);
        xmlInjector.toXml(staxWriter, new JavaContext(new ObjectA("true")));
        staxWriter.close();

        assertEquals("<Injected>Fixed value</Injected>", this.writer.toString());
    }

    @Test
    void testInjectOptionalElementNoText() throws Exception {
        ElementInjector xmlInjector = new ElementInjector(new QName("Injected"), new NullValueTestAction(), true);
        xmlInjector.toXml(staxWriter, new JavaContext(new ObjectA("true")));
        staxWriter.close();

        assertEquals("", this.writer.toString());
    }

    @Test
    void testInjectMandatoryElementNoText() throws Exception {
        ElementInjector xmlInjector = new ElementInjector(new QName("Injected"), new NullValueTestAction(), false);
        assertThrows(Xb4jMarshallException.class, () -> xmlInjector.toXml(staxWriter, new JavaContext(new ObjectA("true"))));
    }

    @Test
    void testInjectMandatoryElementNoTextWithAttributes() throws Exception {
        Root root = new Root(new QName("Root"), Object.class);
        ElementInjector xmlInjector = root.setChild(new ElementInjector(new QName("Injected"), new NullValueTestAction(), false));
        xmlInjector.addAttribute(new Attribute(new QName("attributes")), "name");
        xmlInjector.toXml(staxWriter, new JavaContext(new ObjectA("true")));
        staxWriter.close();

        assertEquals("<Injected attributes=\"true\"/>", this.writer.toString());
    }
}
