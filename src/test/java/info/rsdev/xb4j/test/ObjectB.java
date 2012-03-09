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

public class ObjectB {
    
    private String value = null;
    
    @SuppressWarnings("unused")
    private ObjectB() {}
    
    public ObjectB(String value) {
        if (value == null) {
            throw new NullPointerException("Name cannot be null");
        }
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    protected void setValue(String newValue) {
        this.value = newValue;
    }

}
