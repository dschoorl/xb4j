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

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.io.File;
import java.io.IOException;

/**
 * This class is responsible for providing temporary {@link File} instances pointing to empty but existing files to which 
 * the application can write. The files created are marked {@link File#deleteOnExit()}. 
 *  
 * @author Dave Schoorl
 */
public class FixedDirectoryOutputStrategy implements IFileOutputStrategy {
	
	/**
	 * A {@link FixedDirectoryOutputStrategy} instance that will use java's temp directory as target.
	 */
	public static final FixedDirectoryOutputStrategy INSTANCE = new FixedDirectoryOutputStrategy();
	
	private final File parentDirectory;
	
	/**
	 * Create a new {@link FixedDirectoryOutputStrategy} that will use java's temp directory as target.
	 */
	public FixedDirectoryOutputStrategy() {
		this.parentDirectory = validateParentDirectory(new File(System.getProperty("java.io.tmpdir")));
	}
	
	/**
	 * Create a new {@link FixedDirectoryOutputStrategy} that will use the given parentDirectory as target 
	 */
	public FixedDirectoryOutputStrategy(File parentDirectory) {
		this.parentDirectory = validateParentDirectory(parentDirectory);
	}
	
	@Override
	public File getAndCreateFile(String hint) {
		//dissect the hint in a name part and an extension part
		String extension = null;
		String name = null;
		if ((hint == null) || (hint.length() == 0)) {
			name = "temp";
		} else {
			int lastDotIndex = hint.lastIndexOf('.');
			if (lastDotIndex >= 0) {
				name = hint.substring(0, lastDotIndex);
				if (lastDotIndex < hint.length()) {
					extension = hint.substring(lastDotIndex);	//including the dot
				}
			} else {
				name = hint;
			}
			
			while (name.length() < 3) {
				name = name.concat("_");	//the name must be at least 3 characters according to the File.createTempFile() JavaDoc
			}
		}

		try {
			File tempFile = File.createTempFile(name, extension, parentDirectory);
			tempFile.deleteOnExit();
			return tempFile;
		} catch (IOException e) {
			throw new Xb4jException("Exception occured when creating an output File on the filesystem", e);
		}
	}
	
	/**
	 * Make sure that the provided  parentDirectory exists, is a directory and the application can create files in it
	 * 
	 * @param parentDirectory 
	 * @return the parentDirectory
	 */
	private File validateParentDirectory(File parentDirectory) {
		if (parentDirectory == null) {
//			throw new NullPointerException("Parent directory cannot be null");	//or use the temp directory for the current user??
			parentDirectory = new File(".").getAbsoluteFile();	//or throw an exception??
		}
		
		if (!parentDirectory.exists()) {
			//create directory (maybe even multiple path elements)
			if (!parentDirectory.mkdirs()) {
				throw new Xb4jException("Cannot create output directory %s.");
			}
		} if (!parentDirectory.isDirectory()) {
			throw new Xb4jException(String.format("Provided path %s is not a directory", parentDirectory.getAbsolutePath()));
		}
		
		//test if we can create a file here and write to it
		if (!parentDirectory.canWrite()) {	//is this a sufficient test?
			throw new Xb4jException(String.format("(The user running) this application is not allowed to write in the provided " +
					"output directory: %s", parentDirectory.getAbsolutePath()));
		}
		
		return parentDirectory;
	}
	
}
