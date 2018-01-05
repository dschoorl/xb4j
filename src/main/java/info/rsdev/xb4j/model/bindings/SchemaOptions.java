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
 * Enumeration of options to change the behavior of a {@link IBinding}
 * 
 * @author Dave Schoorl
 */
public enum SchemaOptions implements SchemaOption {
   
    /**
     * This option indicates that the xml element can have xsi:nil attribute set to true. See:
     * https://www.w3.org/TR/xmlschema-1/#xsi_nil
     */
    NILLABLE;
}

/**
 * A marker interface to indicate that the BindOption represents an option related to the schema specification
 * 
 * @author Dave Schoorl
 */
interface SchemaOption extends BindOption {
    
}
