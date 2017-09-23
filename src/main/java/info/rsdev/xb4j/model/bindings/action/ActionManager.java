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

import info.rsdev.xb4j.model.bindings.action.IPhasedAction.ExecutionPhase;
import info.rsdev.xb4j.model.java.JavaContext;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages actions and the execution of actions. This class is created with 'Composition over Inheritence' in mind
 *
 * @author Dave Schoorl
 */
public class ActionManager {

    private List<IPhasedAction> actions = null;

    public void addAction(IPhasedAction action) {
        if (action != null) {
            if (actions == null) {
                actions = new LinkedList<>();
            }
            actions.add(action);
        }
    }

    public JavaContext executeActions(ExecutionPhase phase, JavaContext javaContext) {
        if (actions != null) {
            for (IPhasedAction action : actions) {
                if (action.executeAt(phase)) {
                    javaContext = action.execute(javaContext);
                }
            }
        }
        return javaContext;
    }

    public boolean hasActionsForPhase(ExecutionPhase phase) {
        if (actions == null) {
            return false;
        }

        return actions.stream().anyMatch((action) -> (action.executeAt(phase)));
    }

}
