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

public class ChinesePerson {

    private String firstName = null;

    private String sirName = null;

    private ChinesePerson parent = null;

    private ChinesePerson child = null; //one child politics

    protected ChinesePerson() {
    }

    public ChinesePerson(String firstName, String sirName) {
        this.firstName = firstName;
        this.sirName = sirName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getSirName() {
        return this.sirName;
    }

    public ChinesePerson getChild() {
        return this.child;
    }

    public ChinesePerson getParent() {
        return this.parent;
    }

    /**
     * @param child
     * @return return this instance
     */
    public ChinesePerson setChild(ChinesePerson child) {
        this.child = child;
        child.parent = this;    //maintain bidirectional relationship
        return this;
    }

    public int getFamilyTreeDepth() {
        if (child == null) {
            return 1;
        }
        return child.getFamilyTreeDepth() + 1;
    }

    @Override
    public String toString() {
        return String.format("ChinesePerson[firstName=%s, sirName=%s]", this.firstName, this.sirName);
    }
}
