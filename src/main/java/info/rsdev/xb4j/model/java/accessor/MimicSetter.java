/* Copyright 2014 Red Star Development / Dave Schoorl
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
 * A stateless {@link ISetter} implementation that does not set the value on the javaContext, but pretends that if done so,
 * by always returning true on the {@link #set(JavaContext, Object)} method.
 * 
 * @author Dave Schoorl
 */
public final class MimicSetter implements ISetter {
	
	public static final MimicSetter INSTANCE = new MimicSetter();
	
	private MimicSetter() {}
	
	@Override
	public boolean set(JavaContext javaContext, Object propertyValue) {
		return true;
	}
	
}
