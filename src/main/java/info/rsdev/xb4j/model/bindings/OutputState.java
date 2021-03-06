/*
 * Copyright 2017 Red Star Development.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.xb4j.model.bindings;

/**
 * This class describes the output states of a {@link IBinding} dduring the marshalling of a given Java object.
 * 
 * @author Dave Schoorl
 */
public enum OutputState {
    
    /**
     * The binding does not generate xml output for the given context object
     */
    NO_OUTPUT,
    
    /**
     * The binding will generate output when it's parent is marshalled, but it will not force it's
     * parent to be generated.
     */
    COLLABORATE,
    
    /**
     * The binding generates xml output for the given context object. It will force it's parent to be generated.
     */
    HAS_OUTPUT;
}
