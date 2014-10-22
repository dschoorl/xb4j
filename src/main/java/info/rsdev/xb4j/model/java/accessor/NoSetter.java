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
package info.rsdev.xb4j.model.java.accessor;

import info.rsdev.xb4j.model.java.JavaContext;

/**
 * A stateless {@link ISetter} implementation to support null-safe operations; this implementation does not set the value on the
 * javaContext and communicates this by always returning false on the {@link #set(JavaContext, Object)} method.
 * 
 * @author Dave Schoorl
 */
public final class NoSetter implements ISetter {
	
	public static final NoSetter INSTANCE = new NoSetter();
	
	private NoSetter() {}
	
	@Override
	public boolean set(JavaContext javaContext, Object value) {
		return false;
	}
	
}
