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
package info.rsdev.xb4j.model.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.model.java.JavaContext;

class IntegerConverterTest {

    private JavaContext mockContext = null;

    @BeforeEach
    public void setup() {
        this.mockContext = mock(JavaContext.class);
    }

    @AfterEach
    public void teardown() {
        verifyNoInteractions(mockContext);    //JavaContext is not used by this converter
    }

    @Test
    void testToObjectWithPadding() {
        assertEquals("01", new IntegerConverter(NoValidator.INSTANCE, 2).toText(mockContext, 1));
    }

    @Test
    void nullValuesAreNotValidated() {
        assertNull(new IntegerConverter(NoValidator.INSTANCE, 2).toObject(mockContext, null));
    }

    @Test
    void emptyStringsAreTreatedAsNullValues() {
        assertNull(IntegerConverter.ZERO_OR_POSITIVE.toObject(mockContext, ""));
    }

}
