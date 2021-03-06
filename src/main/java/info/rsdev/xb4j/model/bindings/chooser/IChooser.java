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

import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.java.JavaContext;

/**
 * An {@link IChooser} is used during the marshalling process (from java to xml). It select's the appropriate option from a
 * {@link Choice} binding that is applicable for the object currently being marshalled.
 *
 * @author Dave Schoorl
 */
public interface IChooser {

    /**
     * When the java context matches the option of a {@link Choice} binding coupled to this {@link IChooser}, that option (selection
     * path) will be used for further marshalling of the java context
     *
     * @param javaContext the java object tree being marshalled
     * @return true when the option coupled to this {@link IChooser} matches the java context, false otherwise
     */
    public boolean matches(JavaContext javaContext);
}
