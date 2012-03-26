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

/**
 * A match is made on the basis of the Java types
 * 
 * @author Dave Schoorl
 */
public class InstanceOfChooser implements IChooser {
	
	private Class<?> instanceOf = null;

	/**
	 * Create a new instance of {@link InstanceOfChooser}. This implementation of {@link IChooser} will match a choice when the 
	 * type of the current java context matches this javaType
	 * @param javaType the type that the java context object must have for the {@link IChooser} to match this choice
	 */
	public InstanceOfChooser(Class<?> javaType) {
		if (javaType == null) {
			throw new NullPointerException("Class cannot be null");
		}
		this.instanceOf = javaType;
	}
	
	@Override
	public boolean matches(Object javaContext) {
		if (javaContext == null) { return false; }
		return this.instanceOf.isAssignableFrom(javaContext.getClass());
	}
	
	@Override
	public String toString() {
        String fqClassName = getClass().getName();
        int dotIndex = Math.max(0, fqClassName.lastIndexOf('.') + 1);
        return String.format("%s[type=%s]", fqClassName.substring(dotIndex), this.instanceOf.getName());
	}
}
