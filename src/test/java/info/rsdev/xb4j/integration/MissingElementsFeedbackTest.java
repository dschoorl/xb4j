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

import static info.rsdev.xb4j.test.UnmarshallUtils.unmarshall;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.bindings.Repeater;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.Sequence;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.model.bindings.UnmarshallResult;
import info.rsdev.xb4j.test.ObjectC;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the usability of feedback messages when mandatory elements are missing
 *
 * @author Dave Schoorl
 */
class MissingElementsFeedbackTest {
    private static final QName ROOT = new QName("root");
    private static final QName CONTAINER = new QName("container");
    private static final QName REPEATER = new QName("repeater");
    private static final QName FIRST = new QName("first");
    private static final QName SECOND = new QName("second");
    private static final QName OPTION1 = new QName("option1");
    private static final QName OPTION2 = new QName("option2");

    private ObjectC contextObject = null;
    private BindingModel modelUnderTest = null;
    private Sequence container = null;

    @BeforeEach
    void setUp() {
        modelUnderTest = new BindingModel();
        Root root = modelUnderTest.registerRoot(new Root(ROOT, ObjectC.class));
        container = root.setChild(new Sequence(CONTAINER, false));
        container.add(new SimpleType(FIRST, false), "name");

        contextObject = new ObjectC();
    }

    @Test
    void informUserOfMissingFirstMandatorySimpleElement() throws XMLStreamException {
        UnmarshallResult result = unmarshall(container, contextObject, "<container />");
        assertTrue(result.isError());
        assertEquals(UnmarshallResult.MISSING_MANDATORY_ERROR, result.getErrorCode());
        assertEquals("Mandatory element not encountered in xml: first", result.getErrorMessage());
    }

    @Test
    void informUserOfAdditionalMissingMandatorySimpleElement() throws XMLStreamException {
        //test fixture: add a second (mandatory) element to the container
        container.add(new SimpleType(SECOND, false), "description");
        assertMandatoryElementMissing(SECOND, unmarshall(container, contextObject, "<container><first>name</first></container>"));
    }

    @Test
    void informUserOfMissingMandatoryChoice() throws XMLStreamException {
        //test fixture: add a second (mandatory) element to the container
        Choice choice = container.add(new Choice(SECOND, false), "description");
        choice.addOption(new SimpleType(OPTION1, false));
        choice.addOption(new SimpleType(OPTION2, false));
        assertMandatoryElementMissing(SECOND, unmarshall(container, contextObject, "<container><first>name</first></container>"));
    }

    @Test
    void informUserOfMissingMandatoryOption() throws XMLStreamException {
        //test fixture: add a second (mandatory) element to the container
        Choice choice = container.add(new Choice(SECOND, false), "description");
        choice.addOption(new SimpleType(OPTION1, false));
        choice.addOption(new SimpleType(OPTION2, false));
        assertMandatoryOptionMissing(unmarshall(container, contextObject, "<container><first>name</first><second /></container>"));
    }

    @Test
    void informUserOfMissingMandatoryChoiceInOptionalRepeater() throws XMLStreamException {
        Repeater repeater = container.add(new Repeater(REPEATER, ArrayList.class, true), "details");
        Choice choice = repeater.setItem(new Choice(SECOND, false));
        choice.addOption(new SimpleType(OPTION1, true));
        choice.addOption(new SimpleType(OPTION2, true));

        String xmlSnippet = "<container>"
                + "<first>first</first>"
                + "<repeater><option1><bomb /></option1></repeater>"
                + "</container>";
        assertMandatoryElementMissing(SECOND, unmarshall(container, contextObject, xmlSnippet));
    }

    @Test
    void informUserOfMissingMandatorySequence() throws XMLStreamException {
        Sequence childSequence = container.add(new Sequence(CONTAINER, false));
        childSequence.add(new SimpleType(OPTION1, false));
        childSequence.add(new SimpleType(OPTION2, false));
        container.add(new SimpleType(SECOND, true));

        String xmlSnippet = "<container><first>name</first><second /></container>";
        assertMandatoryElementMissing(CONTAINER, unmarshall(container, contextObject, xmlSnippet));
    }

    private void assertMandatoryElementMissing(QName missingElement, UnmarshallResult result) {
        assertTrue(result.isError());
        assertEquals(UnmarshallResult.MISSING_MANDATORY_ERROR, result.getErrorCode());
        assertEquals("Mandatory element not encountered in xml: " + missingElement, result.getErrorMessage());
    }

    private void assertMandatoryOptionMissing(UnmarshallResult result) {
        assertTrue(result.isError());
        assertEquals(UnmarshallResult.MISSING_MANDATORY_ERROR, result.getErrorCode());
        assertThat(result.getErrorMessage(), startsWith("No matching option found in xml for mandatory Choice"));
    }
}
