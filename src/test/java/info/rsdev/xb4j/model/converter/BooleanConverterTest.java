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
import info.rsdev.xb4j.model.java.JavaContext;

import org.junit.Test;

public class BooleanConverterTest {

    @Test
    public void testToObject() {
    	JavaContext javaContext = null;	//not needed in BooleanConverter implementation
        assertNull(BooleanConverter.INSTANCE.toObject(javaContext, null));
        assertTrue(BooleanConverter.INSTANCE.toObject(javaContext, "true"));
        assertFalse(BooleanConverter.INSTANCE.toObject(javaContext, "false"));
    }
    
    @Test(expected=ValidationException.class)
    public void testToObjectNoBoolean() {
    	JavaContext javaContext = null;	//not needed in BooleanConverter implementation
        BooleanConverter.INSTANCE.toObject(javaContext, "some value");
    }
    
    @Test
    public void testToText() {
    	JavaContext javaContext = null;	//not needed in BooleanConverter implementation
        assertNull(BooleanConverter.INSTANCE.toText(javaContext, null));
        assertEquals("true", BooleanConverter.INSTANCE.toText(javaContext, Boolean.TRUE));
        assertEquals("true", BooleanConverter.INSTANCE.toText(javaContext, true));
        assertEquals("false", BooleanConverter.INSTANCE.toText(javaContext, Boolean.FALSE));
        assertEquals("false", BooleanConverter.INSTANCE.toText(javaContext, false));
    }
    
    @Test(expected=ValidationException.class)
    public void testToTextNoBoolean() {
    	JavaContext javaContext = null;	//not needed in BooleanConverter implementation
        BooleanConverter.INSTANCE.toText(javaContext, new Object());
    }
}
