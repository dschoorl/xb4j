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
package info.rsdev.xb4j.model.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.ValidationException;
import info.rsdev.xb4j.model.java.JavaContext;

public class EnumConverterTest {

    private static enum TestEnum {
        TESTON, TESTOFF
    }

    private EnumConverter converter = null;

    private JavaContext mockContext = null;

    @BeforeEach
    public void setup() {
        this.converter = new EnumConverter(TestEnum.class);
        this.mockContext = mock(JavaContext.class);
    }

    @AfterEach
    public void teardown() {
        verifyNoInteractions(mockContext);    //JavaContext is not used by this converter
    }

    @Test
    public void testToText() {
        assertEquals("TESTON", converter.toText(null, TestEnum.TESTON));
    }

    @Test
    public void testToJava() {
        assertEquals(TestEnum.TESTOFF, converter.toObject(null, "TESTOFF"));
    }

    @Test
    public void toTextOnNoneEnumType() {
        assertThrows(ValidationException.class, () -> converter.toText(null, "I am a String, not an enum value"));
    }

}
