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
package info.rsdev.xb4j.model.bindings.chooser;

import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectC;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Dave Schoorl
 */
public class ContextTypesEqualTest {
    
    private ContextTypesEqual chooserUnderTest = null;
    
    @Before
    public void setup() {
        this.chooserUnderTest = new ContextTypesEqual(ObjectA.class);
    }
    
    @Test
    public void doNotMatchOnSubtypes() {
        assertFalse(chooserUnderTest.matches(new JavaContext(new ObjectC())));
    }
    
    @Test
    public void matchOnSameType() {
        assertTrue(chooserUnderTest.matches(new JavaContext(new ObjectA("a"))));
    }
    
}
