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
package info.rsdev.xb4j.model.bindings.action;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.bindings.IBinding;
import info.rsdev.xb4j.model.java.JavaContext;

/**
 * Extension point that allows users of this framework to manipulate the java context, at specified points in the marshalling or
 * unmarshalling phase. This allows the user E.g. to set values in the Java domain that have no counterpart in the xml domain, or
 * who's value is a derived value etc.
 *
 * @author Dave Schoorl
 */
public interface IPhasedAction {

    public static enum ExecutionPhase {
        //when marshalling Java to Xml, the following phases are identified:

        /**
         * Execute at the start of the marshall process of the {@link IBinding} that this action is set on
         */
        BEFORE_MARSHALLING,
        /**
         * Execute at the end of the marshall process of the {@link IBinding} that this action is set on
         */
        AFTER_MARSHALLING,
        //when unmarshalling from Xml to Java, the following phases are identified:

        /**
         * Execute at the start of the unmarshall process of the {@link IBinding} that this action is set on
         */
        BEFORE_UNMARSHALLING,
        /**
         * Execute directly after creation of the object for the {@link IBinding} that this action is set on. It depends on the
         * binding configuration if an object must be created that will be passed on in the unmarshall process or not. The
         * JavaContext contains the nely created context object or null if no context object was created.
         */
        AFTER_OBJECT_CREATION,
        /**
         * Execute at the end of the unmarshall process of the {@link IBinding} that this action is set on
         */
        AFTER_UNMARSHALLING;
    }

    /**
     *
     * @param javaContext the JavaContext that the IPhasedAction is executed against
     * @return the JavaContext to use in further marshalling or unmarshalling
     * @throws Xb4jException
     */
    public JavaContext execute(JavaContext javaContext) throws Xb4jException;

    /**
     * Determine if this action must execute at the currentPhase or not
     *
     * @param currentPhase the phase about to be executed
     * @return true if this action must execute at the curent phase or false otherwise
     */
    public boolean executeAt(ExecutionPhase currentPhase);

}
