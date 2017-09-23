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
package info.rsdev.xb4j.model.bindings.chooser;

import info.rsdev.xb4j.model.bindings.Ignore;
import info.rsdev.xb4j.model.java.JavaContext;

/**
 * Never select this option. This is the marshalling counterpart for {@link Ignore} binding. Use the stateless {@link #INSTANCE}
 * where appropriate.
 *
 * @author Dave Schoorl
 */
public class NeverChooser implements IChooser {

    /**
     * Stateless reusable instance of the {@link NeverChooser}
     */
    public static final NeverChooser INSTANCE = new NeverChooser();

    private NeverChooser() {
    }

    @Override
    public boolean matches(JavaContext javaContext) {
        return false;
    }

}
