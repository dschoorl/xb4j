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
package info.rsdev.xb4j.model.java.constructor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.SubclassedObjectA;

import org.junit.Test;

public class DefaultConstructorTest {

    @Test
    public void testInstantiatePrivateDefaultConstructor() {
        DefaultConstructor constructor = new DefaultConstructor(ObjectA.class);
        Object instance = constructor.newInstance();
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
    }
    
    @Test(expected=Xb4jException.class)
    public void testNoDefaultConstructor() {
        new DefaultConstructor(SubclassedObjectA.class);    //has no default constructor
    }
    
    //TODO: test with anonymous inner classes?

}
