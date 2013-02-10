/* Copyright 2013 Red Star Development / Dave Schoorl
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
 * A test class without default constructor that must be passed two String values for construction
 * @author dschoorl
 */
public class ObjectD {
    
    private String firstName = null;
    
    private String lastName = null;
    
    public ObjectD(String first, String last) {
    	if (first == null) {
    		throw new NullPointerException("First name cannot be null");
    	}
    	if (last == null) {
    		throw new NullPointerException("Last cannot be null");
    	}
    	this.firstName = first;
    	this.lastName = last;
    }
    
    public String getFullName() {
        return String.format("%s, %s", lastName, firstName);
    }
    
}
