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
 * {@link ICreator} implementation to support null-safe operations when no ICreator is needed, but we do not want to check for
 * null when calling methods on the creator.
 *  
 * @author Dave Schoorl
 */
public final class NullCreator implements ICreator {
	
	public static final NullCreator INSTANCE = new NullCreator();
	
	private NullCreator() {}
	
	@Override
	public Object newInstance(RecordAndPlaybackXMLStreamReader staxReader) {
		return null;
	}
	
	@Override
	public Class<?> getJavaType() {
		return Object.class;
	}
	
}
