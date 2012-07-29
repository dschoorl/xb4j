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

import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provide factory methods to create decoding/encoding streams to and from files...?!? Please clarify this a bit Dave.
 *  
 * @author Dave Schoorl
 */
public interface IXmlCodingFactory {
	
	/**
	 * Create an InputStream that reads bytes from the fromFile, convert the bytes to text, using the
	 * xmlDecodingType. The {@link InputStream} can then be used to insert the text as element content
	 * using {@link SimplifiedXMLStreamWriter#elementContentFromInputStream(InputStream)}.
	 * 
	 * @param fromFile
	 * @param xmlDecodingType
	 * @return
	 * @throws Xb4jException
	 */
	public InputStream getDecodingStream(File fromFile, String xmlDecodingType, Object... parameters);
	
	/**
	 * Decorate an existing InputStream, that reads bytes from some source, with decoding capabilities
	 * according to the xmlDecodingType
	 * @param in
	 * @param xmlDecodingType
	 * @return
	 */
	public InputStream getDecodingStream(InputStream in, String xmlDecodingType, Object... parameters);
	
	public OutputStream getEncodingStream(File toFile, String xmlEncodingType, Object... parameters);
	
	public OutputStream getEncodingStream(OutputStream out, String xmlEncodingType, Object... parameters);
	
	public boolean supports(String xmlCodingType);
}
