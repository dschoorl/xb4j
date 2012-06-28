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

import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;

/**
 * Extension point that allows users of this framework to manipulate the java context, without the input from the xml
 * stream. This allows you E.g. to set values in the Java domain that have no counterpart in the xml domain, or who's value 
 * is a derived value etc.  
 * 
 * @author Dave Schoorl
 */
public interface IUnmarshallingAction {
	
	public void execute(Object javaContext) throws Xb4jUnmarshallException;
	
}