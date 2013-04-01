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

import info.rsdev.xb4j.model.BindingModel;

/**
 * implementations of this interface are aware that they are part of a larger modelling world and that they can 
 * be finalized (that is: been made immutable)
 * 
 * @author Dave Schoorl
 */
public interface IModelAware {

    public void setModel(BindingModel model);
    
    public BindingModel getModel();
    
    /**
     * Check if we can modify the structure definition of this instance
     * 
     * @return true if the instance is mutable, false otherwise
     */
    public boolean isImmutable();
    
    /**
     * Mark the instance immutable, so that it no longer can be changed. An implementation is made immutable, the
     * first time it's {@link Root} binding is used to marshall/unmarshall, so that it can be used in a threadsafe 
     * manner. When a copy is made, the copy is mutable again.
     */
    public void makeImmutable();
    
}
