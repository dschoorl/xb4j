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
package info.rsdev.xb4j.model.bindings;

import static info.rsdev.xb4j.model.bindings.SchemaOptions.NILLABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;
import info.rsdev.xb4j.test.UnmarshallUtils;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;

/**
 * Test the helper methods that have been implemented on the {@link AbstractBinding} class.
 *
 * @author Dave Schoorl
 */
public class AbstractBindingTest {

    /**
     * Dummy implementation: this test is supposed to call the helper methods on {@link AbstractBinding}
     */
    private class MyAbstractBinding extends AbstractBinding {

        public MyAbstractBinding(IElementFetchStrategy elementFetcher, ICreator objectCreator, boolean isOptional, Enum<? extends BindOption>... options) {
            super(elementFetcher, objectCreator, isOptional, options);
        }

        @Override
        public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public OutputState generatesOutput(JavaContext javaContext) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void resolveReferences() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    @Test
    public void unmarshallToNoResultForEmptyNilElement() throws XMLStreamException {
        AbstractBinding bindingUnderTest = new MyAbstractBinding(() -> new QName("emptyNil"), mock(ICreator.class), true, NILLABLE);

        //test-setup: position staxReader directly after start of element test
        RecordAndPlaybackXMLStreamReader staxReader = UnmarshallUtils.getStaxReader("<emptyNil />");
        staxReader.isNextAnElementStart(bindingUnderTest.getElement());

        assertEquals(UnmarshallResult.NO_RESULT, bindingUnderTest.handleNil(staxReader));
    }

    @Test
    public void unmarshallToNoResultForNillElementContainingCommentsOnly() throws XMLStreamException {
        AbstractBinding bindingUnderTest = new MyAbstractBinding(() -> new QName("commentedNil"), mock(ICreator.class), true, NILLABLE);

        //test-setup: position staxReader directly after start of element test
        RecordAndPlaybackXMLStreamReader staxReader = UnmarshallUtils.getStaxReader("<commentedNil><!-- no values known --></commentedNil>");
        staxReader.isNextAnElementStart(bindingUnderTest.getElement());

        assertEquals(UnmarshallResult.NO_RESULT, bindingUnderTest.handleNil(staxReader));
    }

    @Test
    public void failUnmarshallIfNillElementHasSimpleContent() throws XMLStreamException {
        AbstractBinding bindingUnderTest = new MyAbstractBinding(() -> new QName("nilSimpleContent"), mock(ICreator.class), true, NILLABLE);

        //test-setup: position staxReader directly after start of element test
        RecordAndPlaybackXMLStreamReader staxReader = UnmarshallUtils.getStaxReader("<nilSimpleContent>simple content</nilSimpleContent>");
        staxReader.isNextAnElementStart(bindingUnderTest.getElement());

        Xb4jUnmarshallException actual = assertThrows(Xb4jUnmarshallException.class, () -> bindingUnderTest.handleNil(staxReader));
        assertEquals("Nil element <nilSimpleContent> cannot contain content", actual.getMessage());
    }

    @Test
    public void failUnmarshallIfNillElementHasComplexContent() throws XMLStreamException {
        AbstractBinding bindingUnderTest = new MyAbstractBinding(() -> new QName("nilComplexContent"), mock(ICreator.class), true, NILLABLE);

        //test-setup: position staxReader directly after start of element test
        RecordAndPlaybackXMLStreamReader staxReader = UnmarshallUtils.getStaxReader("<nilComplexContent><complex>content</complex></nilComplexContent>");
        staxReader.isNextAnElementStart(bindingUnderTest.getElement());

        Xb4jUnmarshallException actual = assertThrows(Xb4jUnmarshallException.class, () -> bindingUnderTest.handleNil(staxReader));
        assertEquals("Nil element <nilComplexContent> cannot contain content", actual.getMessage());
    }

    @Test
    public void aNillableBindingMustHaveOptionAndQnameSet() {
        AbstractBinding bindingUnderTest = new MyAbstractBinding(() -> new QName("nillableElement"), mock(ICreator.class), true, NILLABLE);
        assertTrue(bindingUnderTest.isNillable());
    }

    @Test
    public void aBindingIsNotNillableWithoutNillableOptionSet() {
        AbstractBinding bindingUnderTest = new MyAbstractBinding(() -> new QName("nillableElement"), mock(ICreator.class), true);
        assertFalse(bindingUnderTest.isNillable());
    }

    @Test
    public void aBindingIsNotNillableWithoutQNameSet() {
        AbstractBinding bindingUnderTest = new MyAbstractBinding(NoElementFetchStrategy.INSTANCE, mock(ICreator.class), true, NILLABLE);
        assertFalse(bindingUnderTest.isNillable());
    }

    @Test
    public void markNillableElementWithNillAttributeSetTrueAsNill() throws XMLStreamException {
        AbstractBinding nillableBinding = new MyAbstractBinding(() -> new QName("nillableElement"), mock(ICreator.class), true, NILLABLE);

        //test-setup: position staxReader directly after start of element test
        RecordAndPlaybackXMLStreamReader staxReader = UnmarshallUtils.getStaxReader("<nillableElement xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        staxReader.isNextAnElementStart(nillableBinding.getElement());

        assertTrue(nillableBinding.isNil(staxReader));
    }

    @Test
    public void doNotMarkNillableElementWithoutNillAttributeAsNill() throws XMLStreamException {
        AbstractBinding nillableBinding = new MyAbstractBinding(() -> new QName("nillableElement"), mock(ICreator.class), true, NILLABLE);

        //test-setup: position staxReader directly after start of element test
        RecordAndPlaybackXMLStreamReader staxReader = UnmarshallUtils.getStaxReader("<nillableElement />");
        staxReader.isNextAnElementStart(nillableBinding.getElement());

        assertFalse(nillableBinding.isNil(staxReader));
    }

    @Test
    public void doNotMarkNillableElementWithNillAttributeSetFalseAsNill() throws XMLStreamException {
        AbstractBinding nillableBinding = new MyAbstractBinding(() -> new QName("nillableElement"), mock(ICreator.class), true, NILLABLE);

        //test-setup: position staxReader directly after start of element test
        RecordAndPlaybackXMLStreamReader staxReader = UnmarshallUtils.getStaxReader("<nillableElement xsi:nil='false' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        staxReader.isNextAnElementStart(nillableBinding.getElement());

        assertFalse(nillableBinding.isNil(staxReader));
    }

    @Test
    public void doNotMarkElementWithNillAttributeSetTrueAsNillWhenNotNillable() throws XMLStreamException {
        AbstractBinding notNillableBinding = new MyAbstractBinding(() -> new QName("notNillableElement"), mock(ICreator.class), true);

        //test-setup: position staxReader directly after start of element test
        RecordAndPlaybackXMLStreamReader staxReader = UnmarshallUtils.getStaxReader("<notNillableElement xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' />");
        staxReader.isNextAnElementStart(notNillableBinding.getElement());

        assertFalse(notNillableBinding.isNillable());
        
        Xb4jUnmarshallException actual = assertThrows(Xb4jUnmarshallException.class, () -> notNillableBinding.isNil(staxReader));
        assertEquals("Found unexpected nil-attribute on xml element <notNillableElement>. Consider adding the NILLABLE option to the binding", actual.getMessage());
    }

}
