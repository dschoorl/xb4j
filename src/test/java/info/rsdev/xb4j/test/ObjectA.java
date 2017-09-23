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
 * Simple helper class that is subject of marshall/unmarshall in tests. This class has an immutable API and contains a single String
 * attribute.
 *
 * @author Dave Schoorl
 */
public class ObjectA implements ITestSubject {

    private String name = null;

    ObjectA() {
    }

    public ObjectA(String name) {
        this.name = name;
    }

    public String getAName() {
        return this.name;
    }

    protected ObjectA setAName(String newName) {
        if (newName == null) {
            throw new NullPointerException("Name cannot be null");
        }
        this.name = newName;
        return this;
    }

    @Override
    public String toString() {
        return "ObjectA [name=" + name + "]";
    }

}
