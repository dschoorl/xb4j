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

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.bindings.ComplexType;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.bindings.Reference;
import info.rsdev.xb4j.model.bindings.Repeater;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.Sequence;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.util.XmlStreamFactory;
import java.io.StringReader;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ReferenceInRepeaterTest {
    
    private BindingModel model = null;

    @Before
    public void setupBinding() {
        Root root = new Root(new QName("root"), ObjectTree.class);
        Sequence content = root.setChild(new Sequence());
        Element a = content.add(new Element(new QName("a"), ObjectA.class), "myObject");
        a.setChild(new SimpleType(new QName("name")), "name");
        
        Repeater messages = content.add(new Repeater(ArrayList.class), "messages");
        messages.setItem(new Reference("tekstMeldingType", null));
        
        ComplexType textMessageType = new ComplexType("tekstMeldingType", null);
        textMessageType.setChild(new SimpleType(new QName("message")));
        
        model = new BindingModel();
        model.register(root);
        model.register(textMessageType, true);
    }
    
    @Test
    public void unmarshall() {
        Object result = model.toJava(XmlStreamFactory.makeReader(new StringReader(REPEATING_VALUES)));
        assertNotNull(result);
        assertSame(ObjectTree.class, result.getClass());
        assertEquals(3, ((ObjectTree)result).getMessages().size());
    }
    
//    @Test
//    public void marshall() {
//        
//    }
    
    private static final String REPEATING_VALUES =
       "<root>\n" +
       "  <a><name>subject</name></a>\n" +
       "  <message>eerste</message>" +
       "  <message>2</message>" +
       "  <message>presto finito</message>" +
       "</root>";
    
}
