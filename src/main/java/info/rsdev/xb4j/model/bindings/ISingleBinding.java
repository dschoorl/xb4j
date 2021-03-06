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
package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;

/**
 * The binding can have only one child
 *
 * @author Dave Schoorl
 */
public interface ISingleBinding extends IBinding {

    public <T extends IBinding> T setChild(T childContainer);

    public <T extends IBinding> T setChild(T childBinding, IGetter getter, ISetter setter);

    public <T extends IBinding> T setChild(T childBinding, String fieldname);

}
