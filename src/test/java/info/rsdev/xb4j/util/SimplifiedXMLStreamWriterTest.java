package info.rsdev.xb4j.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class SimplifiedXMLStreamWriterTest {

    private StringWriter writer = null;

    private XMLStreamWriter staxWriter = null;

    private SimplifiedXMLStreamWriter simpleWriter = null;

    @BeforeEach
    public void setUp() throws Exception {
        writer = new StringWriter();
        staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        simpleWriter = new SimplifiedXMLStreamWriter(staxWriter);
    }

    @Test
    void testWriteEmptyElement() throws Exception {
        simpleWriter.writeElement(new QName("Root"), true);
        staxWriter.writeEndDocument();	//this will be done by the binding  model
        assertEquals("<Root/>", writer.toString());
    }

    @Test
    void testWriteEmptyElementWithNamespace() throws Exception {
        simpleWriter.writeElement(new QName("http://namespace/one", "Root"), true);
        staxWriter.writeEndDocument();  //this will be done by the binding  model
        assertEquals("<ns0:Root xmlns:ns0=\"http://namespace/one\"/>", writer.toString());
    }

    @Test
    void testWriteElementWithText() throws Exception {
        simpleWriter.writeElement(new QName("Root"), false);
        simpleWriter.writeContent("not empty");
        staxWriter.writeEndDocument();	//this will be done by the binding  model
        assertEquals("<Root>not empty</Root>", writer.toString());
    }

    @Test
    void testWriteAttribute() throws Exception {
        QName elementName = new QName("Root");
        simpleWriter.writeElement(elementName, true);
        simpleWriter.writeAttribute(elementName, new QName("attrib"), "true");
        staxWriter.writeEndDocument();	//this will be done by the binding  model
        assertEquals("<Root attrib=\"true\"/>", writer.toString());
    }

    @Test
    void testWriteAttributeWithNamespace() throws Exception {
        QName elementName = new QName("Root");
        simpleWriter.writeElement(elementName, true);
        simpleWriter.writeAttribute(elementName, new QName("http://ns/attrib", "attrib", "zz"), "true");
        staxWriter.writeEndDocument();  //this will be done by the binding  model
        assertEquals("<Root xmlns:zz=\"http://ns/attrib\" zz:attrib=\"true\"/>", writer.toString());
    }

    @Test
    void testWriteElementAndAttributeWithGeneratedNamespaces() throws Exception {
        QName elementName = new QName("http://ns/elem", "Root");
        simpleWriter.writeElement(elementName, true);
        simpleWriter.writeAttribute(elementName, new QName("http://ns/attrib", "attrib"), "falze");
        staxWriter.writeEndDocument();  //this will be done by the binding  model
        assertEquals("<ns0:Root xmlns:ns0=\"http://ns/elem\" xmlns:ns1=\"http://ns/attrib\" ns1:attrib=\"falze\"/>", writer.toString());
    }

}
