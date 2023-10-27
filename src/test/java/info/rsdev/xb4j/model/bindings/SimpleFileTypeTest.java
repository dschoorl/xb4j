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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.test.ObjectF;
import info.rsdev.xb4j.test.UnmarshallUtils;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.util.XmlStreamFactory;

/**
 *
 * @author Dave Schoorl
 */
class SimpleFileTypeTest {

    /**
     * A Base64 representation of a zipfile that contains one plain text file,
     * hw.txt, which contains the text 'Hello world!'.
     */
    private static final String BASE64_ENCODED_ZIPFILE = "UEsDBBQACAAIAKdM/kAAAAAAAAAAAAAAAAAGABAAaHcudHh0VVgMAMY5FlCqORZQNwgAAHu/e79H"
            + "ak5OvkJ5flFOiiIAUEsHCG4nTAgRAAAADwAAAFBLAwQKAAAAAAC1TP5AAAAAAAAAAAAAAAAACQAQ"
            + "AF9fTUFDT1NYL1VYDADGORZQxjkWUDcINwhQSwMEFAAIAAgAp0z+QAAAAAAAAAAAAAAAABEAEABf"
            + "X01BQ09TWC8uX2h3LnR4dFVYDADGORZQqjkWUDcIAABjYBVjZ2BiYPBNTFbwD1aIUIACkBgDJxAb"
            + "AbECEIP4QUDMEOIaEaIYlJzBgAMAAFBLBwiXxlzCLwAAAFIAAABQSwECFQMUAAgACACnTP5AbidM"
            + "CBEAAAAPAAAABgAMAAAAAAAAAABAtIEAAAAAaHcudHh0VVgIAMY5FlCqORZQUEsBAhUDCgAAAAAA"
            + "tUz+QAAAAAAAAAAAAAAAAAkADAAAAAAAAAAAQP1BVQAAAF9fTUFDT1NYL1VYCADGORZQxjkWUFBL"
            + "AQIVAxQACAAIAKdM/kCXxlzCLwAAAFIAAAARAAwAAAAAAAAAAEC0gYwAAABfX01BQ09TWC8uX2h3"
            + "LnR4dFVYCADGORZQqjkWUFBLBQYAAAAAAwADAM4AAAAKAQAAAAA=";

    private static final File ZIPFILE = new File(new File("."), "src/test/resources/info/rsdev/xb4j/model/bindings/archive.zip");

    private BindingModel model = null;

    @BeforeEach
    void setUp() throws Exception {
        this.model = new BindingModel();
        Root root = new Root(new QName("Root"), ObjectF.class);
        SimpleFileType fileType = root.setChild(new SimpleFileType(new QName("File"), false), "file");
        fileType.addAttribute(new Attribute(new QName("Encoding")), "xmlEncoding");
        fileType.addAttribute(new StaticAttribute(new QName("Name"), "temp.zip"), NoGetter.INSTANCE, NoSetter.INSTANCE);
        fileType.addAttribute(new StaticAttribute(new QName("MimeType"), "application/octet-stream"), NoGetter.INSTANCE,
                NoSetter.INSTANCE);
        this.model.registerRoot(root);
    }

    @Test
    void testMarshallFileElement() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectF instance = new ObjectF(ZIPFILE.getCanonicalFile(), SimpleFileType.BASE64_CODING);
        model.getXmlStreamer(instance.getClass(), null).toXml(XmlStreamFactory.makeWriter(stream), instance);
        assertThat(stream.toString()).and("<Root><File Encoding=\"base64\" Name=\"temp.zip\" MimeType=\"application/octet-stream\">"
                .concat(BASE64_ENCODED_ZIPFILE).concat("</File></Root>")).ignoreWhitespace().areIdentical();
    }

    @Test
    void testUnmarshallFileElement() throws Exception {
        byte[] buffer = "<Root><File>".concat(BASE64_ENCODED_ZIPFILE).concat("</File></Root>").getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        Object instance = this.model.toJava(XmlStreamFactory.makeReader(stream));
        assertNotNull(instance);
        assertSame(ObjectF.class, instance.getClass());

        // Check the zipfile
        File file = ((ObjectF) instance).getFile();
        assertNotNull(file);
        file.deleteOnExit(); // cleanup when tests are finished

        // do binary file-to-file comparison
        assertTrue(FileUtils.contentEquals(ZIPFILE, file));
    }

    @Test
    void ignoreMissingMandatoryItemWhenNilIsSetTrue() throws XMLStreamException {
        SimpleFileType nillableSimpleFileType = new SimpleFileType(new QName("file"), false, NILLABLE);

        UnmarshallResult result = UnmarshallUtils.unmarshall(nillableSimpleFileType,
                "<file xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        assertEquals(UnmarshallResult.NO_RESULT, result);
    }

    @Test
    void writeNilElementForNullValuedNillableBinding() throws Exception {
        SimpleFileType nillableSimpleFileType = new SimpleFileType(new QName("file"), false, NILLABLE);
        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(
                XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableSimpleFileType.toXml(staxWriter, new JavaContext(null));
        staxWriter.close();

        assertThat(writer.toString()).and("<file xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />")
                .areIdentical();
    }

    @Test
    void doNotwriteNillAttributeWhenNillableBindingEncountersAValue() throws Exception {
        SimpleFileType nillableSimpleFileType = new SimpleFileType(new QName("file"), false, NILLABLE);
        StringWriter writer = new StringWriter();
        SimplifiedXMLStreamWriter staxWriter = new SimplifiedXMLStreamWriter(
                XMLOutputFactory.newInstance().createXMLStreamWriter(writer));

        nillableSimpleFileType.toXml(staxWriter, new JavaContext(ZIPFILE));
        staxWriter.close();
        assertThat(writer.toString()).and("<file>" + BASE64_ENCODED_ZIPFILE + "</file>").areIdentical();
    }
}
