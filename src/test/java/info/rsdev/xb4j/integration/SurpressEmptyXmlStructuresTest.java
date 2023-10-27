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
package info.rsdev.xb4j.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.Sequence;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ObjectC;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

class SurpressEmptyXmlStructuresTest {
    
    private Sequence koopsom = null;
    
    @BeforeEach
    public void setup() {
        //JavaContext object contains an ObjectTree
        Root root = new Root(new QName("tree"), ObjectC.class);
        koopsom = root.setChild(new Sequence(new QName("C"), true));
        Sequence bedrag = new Sequence(new QName("extraLevel"), false);
        bedrag.add(new SimpleType(new QName("number"), false), "max");
    }

    @Test
    void test() throws XMLStreamException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
        koopsom.toXml(new SimplifiedXMLStreamWriter(staxWriter), new JavaContext(new ObjectTree()));
        assertEquals("", stream.toString());
    }
    
}
