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

import java.util.List;

/**
 *
 * @author Dave Schoorl
 */
public class ObjectC extends ObjectA {

    private int max = 0;

    private String description = null;

    private List<String> details = null;

    protected boolean isInitialized = false;

    public ObjectC() {
        super();
    }

    public int getMax() {
        return this.max;
    }

    public ObjectC setMax(int max) {
        this.max = max;
        return this;
    }

    @Override
    public ObjectC setAName(String name) {
        super.setAName(name);
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public ObjectC setDescription(String description) {
        this.description = description;
        return this;
    }

    protected List<String> getDetails() {
        return this.details;
    }

    public ObjectC setDetails(List<String> details) {
        this.details = details;
        return this;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    public ObjectC setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
        return this;
    }

}
