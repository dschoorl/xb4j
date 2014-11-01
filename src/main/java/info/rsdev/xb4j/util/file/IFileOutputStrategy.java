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
package info.rsdev.xb4j.util.file;

import java.io.File;

public interface IFileOutputStrategy {
	
	/**
	 * Create a {@link File} on the filesystem who's name is based on the provided hint. The exact path of the {@link File} 
	 * is implementation dependend.
	 * 
	 * @param hint
	 * @return The {@link File} handle to the file created on the filesystem
	 */
	public File getAndCreateFile(String hint);
	
}
