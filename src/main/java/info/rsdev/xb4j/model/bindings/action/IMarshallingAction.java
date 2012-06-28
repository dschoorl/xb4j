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

import info.rsdev.xb4j.exceptions.Xb4jMarshallException;

/**
 * Extension point that allows users of this framework to generate a value for an xml element or an attribute, where the
 * implementor has the freedom to generate the value that must be inserted in the xml stream.
 * 
 * @author Dave Schoorl
 */
public interface IMarshallingAction {
	
	public String execute(Object javaContext) throws Xb4jMarshallException;
	
}
