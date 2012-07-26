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
package info.rsdev.xb4j.model.bindings;

import java.io.File;

import info.rsdev.xb4j.model.converter.IValueConverter;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.constructor.NullCreator;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.util.file.FixedDirectoryOutputStrategy;
import info.rsdev.xb4j.util.file.IFileOutputStrategy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * <p>This element is meant to support sending files inside the SOAP request and/or response and not via Webservice standards, 
 * such as MTOM or SOAP with Attachments. The element content will be streamed to/from a {@link File}.</p> 
 * <p>This first implementation does not allow you to dynamically configure the encoder/decoder used when writing to/reading from
 * the stax-stream. Base64 encoding is assumed. Also the output format can not be configured dynamically. </p>
 * <p>With dynamic configuration of this element, I mean the configuration through xml attributes and/or elements in the stax 
 * stream. It is configurable statically when you are writing your bindings.</p> 
 * <p>Remark: support for sending files is done through an {@link IBinding} type and not an {@link IValueConverter}, because we need
 * access to the StAX stream, and that is only available with a {@link IBinding} implementation (not taking {@link IAttribute} into
 * consideration, obviously).</p>
 * @author Dave Schoorl
 */
public class SimpleFileType extends AbstractBinding {
	
	private static final QName DEFAULT_CODINGTYPE_ATTRIBUTENAME = new QName("Encoding"); 
	
	private static final QName DEFAULT_FILENAME_ATTRIBUTENAME = new QName("Name");

	private static final QName DEFAULT_MIMETYPE_ATTRIBUTENAME = new QName("MimeType");

	private Object contentType = null;	//UTF-8, Base64, etc.
	
	/**
	 * The name of the attribute that contains the coding of the element. Base64 coding is assumed when it cannot
	 * be determined from the attributes.
	 */
	private QName codingTypeAttributeName = DEFAULT_CODINGTYPE_ATTRIBUTENAME;
	
	/**
	 * The name of the attribute that will hold the Filename
	 */
	private QName filenameAttributeName = DEFAULT_FILENAME_ATTRIBUTENAME;
	
	/**
	 * The name of the attribute that will hold the mimeType (mainly used for responses)
	 */
	private QName mimeTypeAttributeName = DEFAULT_FILENAME_ATTRIBUTENAME;
	
	private IFileOutputStrategy fileOutputStrategy = null; 
	
	private Object streamEncodingStrategy = null;
	
    /**
     * Create a new {@link SimpleFileType} that will use the user's temp directory for storage
     * @param element
     */
    public SimpleFileType(QName element) {
    	this(element, new FixedDirectoryOutputStrategy());
    }
    
    /**
     * Create a new {@link SimpleFileType} with a {@link DefaultElementFetchStrategy}
     * @param element the element that contains the file data
     */
    public SimpleFileType(QName element, IFileOutputStrategy fileOutputStrategy) {
    	super(new DefaultElementFetchStrategy(element), NullCreator.INSTANCE);
    	if (fileOutputStrategy == null) {
    		throw new NullPointerException("IFileOutputStrategy cannot be null");
    	}
    	this.fileOutputStrategy = fileOutputStrategy;
    }

	@Override
	public boolean generatesOutput(JavaContext javaContext) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext)
			throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Class<?> getJavaType() {
		return File.class;	//this element can only be bound to an java.io.File type
	}
	
}
