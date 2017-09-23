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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import info.rsdev.xb4j.test.ObjectTree;

import javax.xml.namespace.QName;

import org.junit.Test;

public class UnmarshallResultTest {

    @Test
    public void testMissingOptionalElement() {
        assertTrue(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.isMissingOptional());
        assertTrue(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.isUnmarshallSuccessful());
        assertFalse(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.mustHandleUnmarshalledObject());
        assertNull(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.getUnmarshalledObject());
        assertNull(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.getErrorMessage());
    }

    @Test
    public void testMissingMandatoryElement() {
        UnmarshallResult missingMandatory = UnmarshallResult.newMissingElement(new Element(new QName("where-am-i")));
        assertFalse(missingMandatory.isMissingOptional());
        assertFalse(missingMandatory.isUnmarshallSuccessful());
        assertFalse(missingMandatory.mustHandleUnmarshalledObject());
        assertNull(missingMandatory.getUnmarshalledObject());
        assertNotNull(missingMandatory.getErrorMessage());
    }

    @Test
    public void testReturnUnmarshalledAndUnhandledObject() {
        UnmarshallResult unhandledResponse = new UnmarshallResult(new ObjectTree());
        assertFalse(unhandledResponse.isMissingOptional());
        assertTrue(unhandledResponse.isUnmarshallSuccessful());
        assertTrue(unhandledResponse.mustHandleUnmarshalledObject());
        assertNotNull(unhandledResponse.getUnmarshalledObject());
        assertNull(unhandledResponse.getErrorMessage());
    }

    @Test
    public void testReturnUnmarshalledAndHandledObject() {
        UnmarshallResult unhandledResponse = new UnmarshallResult(new ObjectTree(), true);
        assertFalse(unhandledResponse.isMissingOptional());
        assertTrue(unhandledResponse.isUnmarshallSuccessful());
        assertFalse(unhandledResponse.mustHandleUnmarshalledObject());
        assertNotNull(unhandledResponse.getUnmarshalledObject());
        assertNull(unhandledResponse.getErrorMessage());
    }
}
