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
package info.rsdev.xb4j.model.bindings.aspects;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.bindings.ComplexType;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.bindings.ElementInjector;
import info.rsdev.xb4j.model.bindings.IBinding;
import info.rsdev.xb4j.model.bindings.Ignore;
import info.rsdev.xb4j.model.bindings.MapRepeater;
import info.rsdev.xb4j.model.bindings.Recursor;
import info.rsdev.xb4j.model.bindings.Reference;
import info.rsdev.xb4j.model.bindings.Repeater;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.SchemaOptions;
import info.rsdev.xb4j.model.bindings.Sequence;
import info.rsdev.xb4j.model.bindings.SimpleArgument;
import info.rsdev.xb4j.model.bindings.SimpleFileType;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.model.bindings.action.IMarshallingAction;
import info.rsdev.xb4j.model.converter.IValueConverter;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.test.ChinesePerson;
import info.rsdev.xb4j.util.file.IFileOutputStrategy;
import info.rsdev.xb4j.util.file.IXmlCodingFactory;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.namespace.QName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Make sure that the options added to a constructor can be queried later on.
 * @author Dave Schoorl
 */
@RunWith(Parameterized.class)
public class OptionsSupportTest {
    
    private static final SchemaOptions OPTION = SchemaOptions.NILLABLE;
    
    private final IBinding bindingUnderTest;
    
    public OptionsSupportTest(IBinding implementation) {
        this.bindingUnderTest = implementation;
    }
    
    @Parameters
    public static Object[] createBindingInstances() {
        //The weakness of this test is that it is easy to forget to add new constructors or new IBinding-implementations
        return new Object [] {
            new Choice(true, OPTION),
            new Choice(new QName("choice"), true, OPTION),
            new ComplexType(new QName("complextype"), mock(IBinding.class), "bla", true, OPTION),
            new ComplexType("ident", "namespace", true, OPTION),
            new Element(new QName("element"), true, OPTION),
            new Element(Object.class, true, OPTION),
            new Element(new QName("element"), Object.class, true, OPTION),
            new Element(new QName("element"), mock(ICreator.class), true, OPTION),
            new Element(mock(IElementFetchStrategy.class), mock(ICreator.class), true, OPTION),
            new ElementInjector(new QName("elementinjector"), mock (IMarshallingAction.class), true, OPTION),
            new Ignore(new QName("ignore"), true, OPTION),
            new MapRepeater(true, OPTION),
            new MapRepeater(HashMap.class, true, OPTION),
            new MapRepeater(new QName("maprepeater"), HashMap.class, true, OPTION),
            new Recursor(new QName("recursor"), ChinesePerson.class, "firstChild", true, OPTION),
            new Reference("ident", "namespace", true, OPTION),
            new Reference(new QName("reference"), Object.class, "ident", "namespace", true, OPTION),
            new Reference(Object.class, "ident", "namespace", true, OPTION),
            new Reference(new QName("reference"), "ident", "namespace", true, OPTION),
            new Repeater(true, OPTION),
            new Repeater(ArrayList.class, true, OPTION),
            new Repeater(new QName("repeater"), ArrayList.class, true, OPTION),
            new Root(new QName("root"), Object.class, OPTION),
            new Sequence(true, OPTION),
            new Sequence(mock(IElementFetchStrategy.class), mock(ICreator.class), true, OPTION),
            new Sequence(new QName("sequence"),true, OPTION),
            new Sequence(Object.class,true, OPTION),
            new Sequence(new QName("sequence"), Object.class, true, OPTION),
            new Sequence(new QName("sequence"), mock(ICreator.class),true, OPTION),
            new SimpleArgument(new QName("simpleargument"), true, OPTION),
            new SimpleArgument(new QName("simpleargument"), mock(IValueConverter.class), true, OPTION),
            new SimpleFileType(new QName("simplefiletye"), true, OPTION),
            new SimpleFileType(new QName("simplefiletype"), mock(IFileOutputStrategy.class), true, OPTION),
            new SimpleFileType(new QName("simplefiletype"), mock(IFileOutputStrategy.class), mock(IXmlCodingFactory.class), true, OPTION),
            new SimpleType(new QName("simpletype"), true, OPTION),
            new SimpleType(new QName("simpletype"), mock(IValueConverter.class), true, OPTION)
        };
    }
    
    @Test
    public void isOptionPresent() {
        assertTrue(bindingUnderTest.hasOption(OPTION));
    }
    
}
