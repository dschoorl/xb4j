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
package info.rsdev.xb4j.test;

import java.io.File;

/**
 * A DTO for a File object (for testing purposes)
 * 
 * @author Dave Schoorl
 */
public class ObjectF {
	
	private File file = null;
	
	private String xmlEncoding = null;
	
	protected ObjectF() {}
	
	public ObjectF(File file, String xmlEncoding) {
		this.file = file;
		this.xmlEncoding = xmlEncoding;
	}
	
	public File getFile() {
		return this.file;
	}
}
