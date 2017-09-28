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
package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ITestSubject;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author Dave Schoorl
 */
public class IBindingTest {
    
    private IBinding mockBinding = null;
    private static final Map<String, Object> map = Collections.emptyMap();
    
    @Before
    public void setup() {
        mockBinding = mock(IBinding.class);
        doCallRealMethod().when(mockBinding).validateContextObject(any());
    }

    @Test
    public void theContextObjectMustMatchTheBindingsJavatype() {
        JavaContext javaContext = new JavaContext(new ObjectA("A"), map);
        when(mockBinding.getProperty(javaContext)).thenReturn(javaContext);
        when(mockBinding.getJavaType()).thenReturn(ObjectA.class);
        mockBinding.validateContextObject(javaContext);    //no exception means a succesfull test
    }
    
    @Test
    public void orTheContextObjectIsASubtypeOfJavatype() {
        JavaContext javaContext = new JavaContext(new ObjectA("A"), map);
        when(mockBinding.getProperty(javaContext)).thenReturn(javaContext);
        when(mockBinding.getJavaType()).thenReturn(ITestSubject.class);
        mockBinding.validateContextObject(javaContext);    //no exception means a succesfull test
    }
    
    @Test(expected=Xb4jException.class)
    public void failWhenContextTypeIsNotASubtypeOfTheJavatype() {
        JavaContext javaContext = new JavaContext(new ObjectB(100), map);
        when(mockBinding.getProperty(javaContext)).thenReturn(javaContext);
        when(mockBinding.getJavaType()).thenReturn(ObjectA.class);
        mockBinding.validateContextObject(javaContext);
    }
    
    @Test(expected=Xb4jException.class)
    public void failWhenTheContextObjectIsNotASubtypeOfJavatype() {
        JavaContext javaContext = new JavaContext(new Object(), map);
        when(mockBinding.getProperty(javaContext)).thenReturn(javaContext);
        when(mockBinding.getJavaType()).thenReturn(ITestSubject.class);
        mockBinding.validateContextObject(javaContext);
    }
    
    @Test
    public void aBindingIsNotRequiredToHaveAJavaType() {
        JavaContext javaContext = new JavaContext(new ObjectA("A"), map);
        when(mockBinding.getProperty(javaContext)).thenReturn(javaContext);
        when(mockBinding.getJavaType()).thenReturn(null);
        mockBinding.validateContextObject(javaContext);    //no exception means a succesfull test
    }
    
}
