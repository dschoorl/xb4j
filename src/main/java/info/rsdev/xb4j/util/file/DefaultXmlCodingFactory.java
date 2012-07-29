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

import info.rsdev.xb4j.model.BindingModel;

import org.apache.commons.codec.binary.Base32InputStream;
import org.apache.commons.codec.binary.Base32OutputStream;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

/**
 * Implementation of {@link IXmlCodingFactory}, offering support for Base64 coding. If you use 
 * this class in your {@link BindingModel}, then you must make sure that Apache Commons-Codec 
 * (at least version 1.5) is on your runtime classpath.
 * 
 * @author Dave Schoorl
 */
public class DefaultXmlCodingFactory extends AbstractXmlCodingFactory {
	
	public static final DefaultXmlCodingFactory INSTANCE = new DefaultXmlCodingFactory();
	
	private DefaultXmlCodingFactory() {
		super();
	}
	
	protected void registerCodingTypes() {
		registerCoding("Base64", Base64InputStream.class, Base64OutputStream.class);
		registerCoding("Base32", Base32InputStream.class, Base32OutputStream.class);
	}
	
}
