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
package info.rsdev.xb4j.test;

/**
 * Simple helper class that is subject of marshall/unmarshall in tests. This class has an immutable public API and contains a single 
 * Integer attribute.
 * 
 * @author Dave Schoorl
 */
public class ObjectB implements ITestSubject {
    
    private Integer value = null;
    
    @SuppressWarnings("unused")
    private ObjectB() {}
    
    public ObjectB(Integer value) {
        if (value == null) {
            throw new NullPointerException("Value cannot be null");
        }
        this.value = value;
    }
    
    public Integer getValue() {
        return this.value;
    }
    
    protected void setValue(Integer newValue) {
        this.value = newValue;
    }

    @Override
    public String toString() {
        return "ObjectB [value=" + value + "]";
    }

}
