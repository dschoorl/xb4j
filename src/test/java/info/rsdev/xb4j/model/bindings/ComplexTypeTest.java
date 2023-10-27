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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
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
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.test.UnmarshallUtils;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.util.XmlStreamFactory;

class ComplexTypeTest {

    private BindingModel model = null;

    @BeforeEach
    public void setup() {
        Root root = new Root(new QName("root"), ObjectA.class);   //has element, but class comes from child
        root.setChild(new Reference("typeO", null, false), NoGetter.INSTANCE, NoSetter.INSTANCE);

        Root hoofdmap = new Root(new QName("directory"), ObjectTree.class);
        hoofdmap.setChild(new Reference(ObjectA.class, "typeO", null, false), "myObject");

        ComplexType complexType = new ComplexType("typeO", null, false);
        complexType.setChild(new SimpleType(new QName("name"), false), "name");

        model = new BindingModel();
        model.registerComplexType(complexType, true);
        model.registerRoot(root);
        model.registerRoot(hoofdmap);
    }

    @Test
    void testMarshallComplexType() {
        //marshall root
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new ObjectA("test");
        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        String result = stream.toString();
        assertEquals("<root><name>test</name></root>", result);

        //marshall hoofdmap
        stream = new ByteArrayOutputStream();
        instance = new ObjectTree().setMyObject(new ObjectA("test"));
        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        result = stream.toString();
        assertEquals("<directory><name>test</name></directory>", result);
    }

    @Test
    void testUnmarshallComplexType() {
        //Unmarshall ObjectA
        ByteArrayInputStream stream = new ByteArrayInputStream("<root><name>test</name></root>".getBytes());
        Object instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("test", ((ObjectA) instance).getAName());

        //unmarshall ObjectTree
        stream = new ByteArrayInputStream("<directory><name>test</name></directory>".getBytes());
        instance = model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree) instance;
        assertNotNull(tree.getMyObject());
        assertSame(ObjectA.class, tree.getMyObject().getClass());
        assertEquals("test", ((ObjectA) tree.getMyObject()).getAName());
    }

    @Test
    void testMarshallReferenceWithElement() {
        //Setup the binding model
        BindingModel myModel = new BindingModel();
        Root root = new Root(new QName("root"), ObjectA.class);
        root.setChild(new Reference(new QName("reference"), "complexType", null, false), NoGetter.INSTANCE, NoSetter.INSTANCE);
        myModel.registerRoot(root);

        ComplexType complexType = new ComplexType("complexType", null, false);
        complexType.setChild(new SimpleType(new QName("name"), false), "name");
        myModel.registerComplexType(complexType, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectA instance = new ObjectA("test");
        myModel.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertEquals("<root><reference><name>test</name></reference></root>", stream.toString());
    }

    @Test
    void ignoreMissingMandatoryChildrenWhenNilIsSetTrue() throws XMLStreamException {
        ComplexType nillableComplexType = new ComplexType(new QName("complex"), mock(IBinding.class), "complex", false, NILLABLE);

        UnmarshallResult result = UnmarshallUtils.unmarshall(nillableComplexType, "<complex xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        assertEquals(UnmarshallResult.NO_RESULT, result);
    }

    @Test
    void writeNilElementForNullValuedNillableBinding() throws Exception {
        ComplexType nillableComplexType = new ComplexType(new QName("complex"), mock(IBinding.class), "name", false, NILLABLE);

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableComplexType.toXml(staxWriter, new JavaContext(null));
        staxWriter.close();

        assertThat(writer.toString()).and("<complex xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />").areIdentical();
    }
    
    @Test
    void doNotwriteNillAttributeWhenNillableBindingEncountersAValue() throws Exception {
        ComplexType nillableComplexType = new ComplexType(new QName("complex"), mock(IBinding.class), "name", false, NILLABLE);

        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableComplexType.toXml(staxWriter, new JavaContext(new ObjectA("some_name")));
        staxWriter.close();
        
        assertThat(writer.toString()).and("<complex />").areIdentical();
    }
}
