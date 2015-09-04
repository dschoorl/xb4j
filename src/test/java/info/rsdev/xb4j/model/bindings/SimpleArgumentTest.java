/* Copyright 2015 Red Star Development / Dave Schoorl
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

import static org.junit.Assert.*;
import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.converter.IntegerConverter;
import info.rsdev.xb4j.model.java.constructor.ArgsConstructor;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.test.ObjectB;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.util.XmlStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

public class SimpleArgumentTest {
    
    private static final QName GETAL = new QName("getal");

    private BindingModel model = null;
    
    @Before
    public void setUp() throws Exception {
        model = new BindingModel();
        Root root = (new Root(new QName("root"), ObjectTree.class));
        Sequence objectB = root.setChild(new Sequence((IElementFetchStrategy)null, new ArgsConstructor(ObjectB.class, GETAL)), "myObject");
        objectB.add(new SimpleArgument(GETAL, IntegerConverter.INSTANCE).setOptional(true), "value");
        model.register(root);
    }

    @Test
    public void readIntegerFromXmlAndUseInConstructor() throws IOException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream("<root><getal>123</getal></root>".getBytes("UTF-8"))) {
            ObjectTree instance = (ObjectTree) model.toJava(XmlStreamFactory.makeReader(stream));
            assertNotNull(instance);
            assertSame(ObjectB.class, instance.getMyObject().getClass());
            assertEquals(Integer.valueOf("123"), ((ObjectB)instance.getMyObject()).getValue());
        }
    }

}
