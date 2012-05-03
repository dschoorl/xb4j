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
package info.rsdev.xb4j.exceptions;

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.bindings.IBinding;

/**
 * 
 * @author Dave Schoorl
 */
public class Xb4jMarshallException extends Xb4jException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Don't know what to do with this binding yet, but we could use it to make a better diagnostic message; when it will be used,
	 * we need to reconsider the transient keyword and realise that the binding basically holds references to the entire 
	 * {@link BindingModel}
	 */
	@SuppressWarnings("unused")
	private transient IBinding bindingContext = null; 

	public Xb4jMarshallException(String message, IBinding binding) {
		super(message);
		this.bindingContext = binding;
	}

	public Xb4jMarshallException(String message, IBinding binding, Throwable cause) {
		super(message, cause);
		this.bindingContext = binding;
	}
	
}
