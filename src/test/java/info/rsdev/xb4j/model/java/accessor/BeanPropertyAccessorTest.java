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
package info.rsdev.xb4j.model.java.accessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectC;
import info.rsdev.xb4j.test.SubclassedObjectA;

import java.util.LinkedList;

import org.junit.Test;

public class BeanPropertyAccessorTest {

    @Test
    public void testSetProtectedProperty() {
        BeanPropertyAccessor accessor = new BeanPropertyAccessor("aName");
        
        ObjectA context = new ObjectA("test");
        assertEquals("test", context.getAName());
        assertTrue(accessor.set(new JavaContext(context), "Hello"));
        assertEquals("Hello", context.getAName());
    }
    
    @Test
    public void testSetPrimitiveProperty() {
        BeanPropertyAccessor accessor = new BeanPropertyAccessor("initialized");
        
        ObjectC context = new ObjectC();
        assertFalse(context.isInitialized());
        assertTrue(accessor.set(new JavaContext(context), true));
        assertTrue(context.isInitialized());
    }
    
    @Test
    public void testSetSuperclassProperty() {
        BeanPropertyAccessor accessor = new BeanPropertyAccessor("aName");
        
        SubclassedObjectA context = new SubclassedObjectA("test");
        assertEquals("test", context.getAName());
        assertTrue(accessor.set(new JavaContext(context), "Hello"));
        assertEquals("Hello", context.getAName());
    }
    
    @Test
    public void testGetProtectedProperty() {
        BeanPropertyAccessor accessor = new BeanPropertyAccessor("details");
        
        LinkedList<String> details = new LinkedList<String>();
        details.add("the devil");
        
        ObjectC context = new ObjectC();
        context.setDetails(details);
        assertEquals(details, accessor.get(new JavaContext(context)).getContextObject());
    }
    
    @Test
    public void testGetPrimitiveBoolean() {
        BeanPropertyAccessor accessor = new BeanPropertyAccessor("initialized");
        
        ObjectC context = new ObjectC();
        context.setInitialized(true);
        
        Object property = accessor.get(new JavaContext(context)).getContextObject();
        assertNotNull(property);
        assertSame(Boolean.class, property.getClass()); //value is autoboxed? Why??
        assertEquals(Boolean.TRUE, property);
    }
    
    @Test
    public void testGetSuperclassProperty() {
        BeanPropertyAccessor accessor = new BeanPropertyAccessor("aName");
        
        SubclassedObjectA context = new SubclassedObjectA("test");
        assertEquals("test", accessor.get(new JavaContext(context)).getContextObject());
    }
    
}
