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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import info.rsdev.xb4j.exceptions.ValidationException;
import info.rsdev.xb4j.model.converter.BooleanConverter;

import org.junit.Test;

public class BooleanConverterTest {

    @Test
    public void testToObject() {
        assertNull(BooleanConverter.INSTANCE.toObject(null));
        assertTrue(BooleanConverter.INSTANCE.toObject("true"));
        assertFalse(BooleanConverter.INSTANCE.toObject("false"));
    }
    
    @Test(expected=ValidationException.class)
    public void testToObjectNoBoolean() {
        BooleanConverter.INSTANCE.toObject("some value");
    }
    
    @Test
    public void testToText() {
        assertNull(BooleanConverter.INSTANCE.toText(null));
        assertEquals("true", BooleanConverter.INSTANCE.toText(Boolean.TRUE));
        assertEquals("true", BooleanConverter.INSTANCE.toText(true));
        assertEquals("false", BooleanConverter.INSTANCE.toText(Boolean.FALSE));
        assertEquals("false", BooleanConverter.INSTANCE.toText(false));
    }
    
    @Test(expected=ValidationException.class)
    public void testToTextNoBoolean() {
        BooleanConverter.INSTANCE.toText(new Object());
    }
}
