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
package info.rsdev.xb4j.model.java;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.bindings.Recursor;
import info.rsdev.xb4j.model.bindings.Repeater;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Dave Schoorl
 */
public class JavaContext {
	
	private static final Map<String, Object> EMPTY_IMMUTABLE_MAP = Collections.emptyMap();
	
	private Object contextObject = null;
	
	private int indexInCollection = -1;
	
	private Map<String, Object> externalContext = null;
	
	public JavaContext(Object contextObject) {
		this(contextObject, EMPTY_IMMUTABLE_MAP);
	}
	
	public JavaContext(Object contextObject, Map<String, Object> externalContext) {
		if (externalContext == null) {
			throw new NullPointerException("External context Map cannot be null");
		}
		this.contextObject = contextObject;
		this.externalContext = new HashMap<String, Object>(externalContext);
	}
	
	/**
	 * Copy constructor for a context object that is not an item in a collection
	 */
	private JavaContext(Object newContextObject, JavaContext original) {
		this.contextObject = newContextObject;
		this.externalContext = original.externalContext;
	}
	
	/**
	 * Copy constructor for a context object that is an item in a collection
	 */
	private JavaContext(Object newContextObject, int index, JavaContext original) {
		if (index < 0) {
			throw new Xb4jException("Index of context object into a collection cannot be negative:"+index);
		}
		this.contextObject = newContextObject;
		this.indexInCollection = index;
		this.externalContext = original.externalContext;
	}
	
	public Object getContextObject() {
		return this.contextObject;
	}
	
	public Object get(String externalContextKey) {
		return externalContext.get(externalContextKey);
	}
	
	public JavaContext newContext(Object newContextObject) {
		return new JavaContext(newContextObject, this);
	}
	
	public JavaContext newContext(Object newContextObject, int index) {
		return new JavaContext(newContextObject, index, this);
	}
	
	/**
	 * Get the index of the context object in this JavaContext into the sequential collection of the parent binding (if any). 
	 * When the  parent binding does not reflect a collection, this method returns -1. Currently, only a {@link Repeater} and 
	 * a {@link Recursor} bindingd reflect the notion of a sequential collection.
	 * @return the index into a collection or -1 when the context object is not the item of a sequential collection
	 */
	public int getIndexInCollection() {
		return this.indexInCollection;
	}
	
	@Override
	public String toString() {
		return String.format("%s[context=%s, external context objectcount=%d]", getClass().getSimpleName(), contextObject, externalContext.size());
	}
	
}
