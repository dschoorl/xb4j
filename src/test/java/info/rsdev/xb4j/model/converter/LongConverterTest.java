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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import info.rsdev.xb4j.model.java.JavaContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LongConverterTest {

    private JavaContext mockContext = null;

    @Before
    public void setup() {
        this.mockContext = mock(JavaContext.class);
    }

    @After
    public void teardown() {
        verifyZeroInteractions(mockContext);    //JavaContext is not used by this converter
    }

    @Test
    public void nullValuesAreNotValidated() {
        assertNull(LongConverter.POSITIVE.toObject(mockContext, null));
    }

    @Test
    public void emptyStringsAreTreatedAsNullValues() {
        assertNull(LongConverter.POSITIVE.toObject(mockContext, ""));
    }

}
