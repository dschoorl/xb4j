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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ChinesePerson;
import info.rsdev.xb4j.test.UnmarshallUtils;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.util.XmlStreamFactory;

class RecursorTest {

    private static final class FamilyTree {

        private ChinesePerson child = null; //one child politics

        public FamilyTree() {
        }

        public ChinesePerson getChild() {
            return this.child;
        }

        public ChinesePerson setChild(ChinesePerson child) {
            this.child = child;
            return child;
        }

        public int getFamilyTreeDepth() {
            if (child == null) {
                return 0;
            }
            return child.getFamilyTreeDepth();
        }
    }

    private BindingModel model = null;

    @BeforeEach
    public void setup() {
        model = new BindingModel();
        Root root = new Root(new QName("Stamboom"), FamilyTree.class);
        Recursor childRecursor = root.setChild(new Recursor(new QName("Kind"), ChinesePerson.class, "child", true), "child");
        childRecursor.addAttribute(new Attribute(new QName("Voornaam")), "firstName");
        childRecursor.addAttribute(new Attribute(new QName("Achternaam")), "sirName");
        model.registerRoot(root);
    }

    @Test
    void testMarshallNoRecurringElement() throws Exception {
        FamilyTree tree = new FamilyTree();
        assertEquals(0, tree.getFamilyTreeDepth());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.getXmlStreamer(tree.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), tree);
        String expected = "<Stamboom />";

        assertThat(stream.toString()).and(expected)
            .ignoreWhitespace()
            .areIdentical();
    }

    @Test
    void testMarshallSingleRecurringElement() throws Exception {
        FamilyTree tree = new FamilyTree();
        tree.setChild(new ChinesePerson("Mao", "Zedong"));
        assertEquals(1, tree.getFamilyTreeDepth());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.getXmlStreamer(tree.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), tree);
        String expected = "<Stamboom>"
                + "  <Kind Voornaam=\"Mao\" Achternaam=\"Zedong\" />"
                + "</Stamboom>";

        assertThat(stream.toString()).and(expected).ignoreWhitespace().areIdentical();
    }

    @Test
    void testMarshallMultipleRecurringElements() throws Exception {
        FamilyTree tree = new FamilyTree();
        tree.setChild(new ChinesePerson("Mao", "Zedong").setChild(new ChinesePerson("Mao Anqing", "Zedong").setChild(new ChinesePerson("Mao Xinyu", "Zedong"))));
        assertEquals(3, tree.getFamilyTreeDepth());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.getXmlStreamer(tree.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), tree);
        String expected = "<Stamboom>"
                + "  <Kind Voornaam=\"Mao\" Achternaam=\"Zedong\">"
                + "    <Kind Voornaam=\"Mao Anqing\" Achternaam=\"Zedong\">"
                + "      <Kind Voornaam=\"Mao Xinyu\" Achternaam=\"Zedong\" />"
                + "    </Kind>"
                + "  </Kind>"
                + "</Stamboom>";

        assertThat(stream.toString()).and(expected).ignoreWhitespace().areIdentical();
    }

    @Test
    void testUnmarshallNoRecurringElement() {
        String snippet = "<Stamboom />";
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(FamilyTree.class, instance.getClass());
        FamilyTree tree = (FamilyTree) instance;
        assertEquals(0, tree.getFamilyTreeDepth());
    }

    @Test
    void testUnmarshallSingleRecurringElement() {
        String snippet = "<Stamboom>"
                + "  <Kind Voornaam=\"Mao\" Achternaam=\"Zedong\" />"
                + "</Stamboom>";
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(FamilyTree.class, instance.getClass());
        FamilyTree tree = (FamilyTree) instance;
        assertEquals(1, tree.getFamilyTreeDepth());
        assertEquals("Mao", tree.getChild().getFirstName());
        assertEquals("Zedong", tree.getChild().getSirName());
    }

    @Test
    void testUnmarshallMultipleRecurringElements() {
        String snippet = "<Stamboom>"
                + "  <Kind Voornaam=\"Mao\" Achternaam=\"Zedong\">"
                + "    <Kind Voornaam=\"Mao Anqing\" Achternaam=\"Zedong\">"
                + "      <Kind Voornaam=\"Mao Xinyu\" Achternaam=\"Zedong\" />"
                + "    </Kind>"
                + "  </Kind>"
                + "</Stamboom>";
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(FamilyTree.class, instance.getClass());
        FamilyTree tree = (FamilyTree) instance;
        assertEquals(3, tree.getFamilyTreeDepth());
        assertEquals("Mao", tree.getChild().getFirstName());
        assertEquals("Zedong", tree.getChild().getSirName());
        assertNull(tree.getChild().getParent());
        assertEquals("Mao Anqing", tree.getChild().getChild().getFirstName());
        assertEquals("Mao", tree.getChild().getChild().getParent().getFirstName());
        assertEquals("Mao Xinyu", tree.getChild().getChild().getChild().getFirstName());
        assertEquals("Mao Anqing", tree.getChild().getChild().getChild().getParent().getFirstName());
    }

    @Test
    void ignoreMissingMandatoryItemWhenNilIsSetTrue() throws XMLStreamException {
        Recursor nillableRecursor = new Recursor(new QName("recurse"), ChinesePerson.class, "child", false, NILLABLE);

        UnmarshallResult result = UnmarshallUtils.unmarshall(nillableRecursor, "<recurse xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        assertEquals(UnmarshallResult.NO_RESULT, result);
    }
    
    @Test
    void writeNilElementForNullValuedNillableBinding() throws Exception {
        Recursor nillableRecursor = new Recursor(new QName("recurse"), ChinesePerson.class, "child", false, NILLABLE);

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableRecursor.toXml(staxWriter, new JavaContext(null));
        staxWriter.close();

        assertThat(writer.toString()).and("<recurse xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />").areIdentical();    }
    
    @Test
    void doNotwriteNillAttributeWhenNillableBindingEncountersAValue() throws Exception {
        Recursor nillableRecursor = new Recursor(new QName("recurse"), ChinesePerson.class, "child", false, NILLABLE);

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableRecursor.toXml(staxWriter, new JavaContext(new ChinesePerson("Jet", "Ski")));
        staxWriter.close();

        assertThat(writer.toString()).and("<recurse />").areIdentical();
    }
}
