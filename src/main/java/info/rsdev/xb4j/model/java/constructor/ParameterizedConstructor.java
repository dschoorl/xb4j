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
package info.rsdev.xb4j.model.java.constructor;

import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;

/**
 * This implementation of {@link ICreator} is capable of calling a constructor that takes arguments. The arguments are
 * read from the xml stream and can come from xml attributes as well as xml elements.
 * 
 * @author Dave Schoorl
 */
public class ParameterizedConstructor implements ICreator {
	
	private final Class<?> javaType;
	
	public ParameterizedConstructor(Class<?> javaType) {
		this.javaType = javaType;
	}
	
	@Override
	public Object newInstance(RecordAndPlaybackXMLStreamReader staxReader) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Class<?> getJavaType() {
		return this.javaType;
	}
	
}
