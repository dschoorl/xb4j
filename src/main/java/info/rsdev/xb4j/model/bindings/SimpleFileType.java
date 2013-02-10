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

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.converter.IValueConverter;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.constructor.NullCreator;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.util.file.DefaultXmlCodingFactory;
import info.rsdev.xb4j.util.file.FixedDirectoryOutputStrategy;
import info.rsdev.xb4j.util.file.IFileOutputStrategy;
import info.rsdev.xb4j.util.file.IXmlCodingFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleFileType.class);
	
	private static final QName DEFAULT_CODINGTYPE_ATTRIBUTENAME = new QName("Encoding"); 
	
	private static final QName DEFAULT_FILENAME_ATTRIBUTENAME = new QName("Name");

	public static final String BASE64_CODING = DefaultXmlCodingFactory.BASE64_CODING;
	
	/**
	 * The name of the attribute that contains the coding of the element. Base64 coding is assumed when it cannot
	 * be determined from the attributes.
	 */
	private QName codingTypeAttributeName = null;
	
	/**
	 * The way the xml elementis coded, E.g. Base64. This value is used when it cannot dynamically be detected (from the element's attributes)
	 */
	private String codingType = null;
	
	/**
	 * The name of the attribute that will hint for the fileName
	 */
	private QName filenameAttributeName = null;
	
	/**
	 * A sugestion for the filename
	 */
	private String filenameHint = null;
	
	private IFileOutputStrategy fileOutputStrategy = null; 
	
	private IXmlCodingFactory xmlCodingFactory = null;
	
    /**
     * Create a new {@link SimpleFileType} that will use the user's temp directory for storage
     * @param element
     */
    public SimpleFileType(QName element) {
    	this(element, FixedDirectoryOutputStrategy.INSTANCE, DefaultXmlCodingFactory.INSTANCE);
    }
    
    public SimpleFileType(QName element, IFileOutputStrategy fileOutputStrategy) {
    	this(element, fileOutputStrategy, DefaultXmlCodingFactory.INSTANCE);
    }
    
    /**
     * Create a new {@link SimpleFileType} with a {@link DefaultElementFetchStrategy}
     * @param element the element that contains the file data
     */
    public SimpleFileType(QName element, IFileOutputStrategy fileOutputStrategy, IXmlCodingFactory xmlCodingFactory) {
    	super(new DefaultElementFetchStrategy(element), NullCreator.INSTANCE);
    	if (fileOutputStrategy == null) {
    		throw new NullPointerException("IFileOutputStrategy cannot be null");
    	}
    	if (xmlCodingFactory == null) {
    		throw new NullPointerException("IXmlCodingFactory cannot be null");
    	}
    	this.fileOutputStrategy = fileOutputStrategy;
    	this.xmlCodingFactory = xmlCodingFactory;
    	setCodingtypeFrom(DEFAULT_CODINGTYPE_ATTRIBUTENAME, BASE64_CODING);
    	setFilenameHintFrom(DEFAULT_FILENAME_ATTRIBUTENAME, "temp.bin");
    }

	@Override
	public boolean generatesOutput(JavaContext javaContext) {
		javaContext = getProperty(javaContext);
        Object inputObject = javaContext.getContextObject();
        if ((inputObject != null) && !(inputObject instanceof File)) {
        	if (logger.isDebugEnabled()) {
        		logger.debug(String.format("Expected an instance of File, but encountered %s", inputObject));
        	}
        	return false;	//no file -- no output
        }
        File inputFile = (File)inputObject;
        boolean isEmpty = (inputFile == null) || !inputFile.isFile() || (inputFile.length() == 0);
        if (isEmpty && logger.isDebugEnabled()) {
        	String path = (inputFile == null?null:inputFile.getAbsolutePath());
        	logger.debug(String.format("No data will be obtained for File %s", path));
        }
        return !isOptional() || !isEmpty;
	}
	
	@Override
	public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        QName expectedElement = getElement();	//should never be null for a SimpleType
    	boolean startTagFound = false;
    	if (expectedElement != null) {
    		if (!staxReader.isAtElementStart(expectedElement)) {
	    		if (isOptional()) {
                    return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
	    		} else {
                    return UnmarshallResult.newMissingElement(this);
	    		}
    		} else {
    			startTagFound = true;
    		}
    	}
        
		String codingType = getAttributeValue(staxReader, javaContext, this.codingTypeAttributeName, this.codingType, BASE64_CODING);
		String filenameHint = getAttributeValue(staxReader, javaContext, this.filenameAttributeName, this.filenameHint, "temp");
		
        attributesToJava(staxReader, javaContext);

        File outputFile = this.fileOutputStrategy.getAndCreateFile(filenameHint);
        OutputStream outputStream = null;
        try {
            outputStream = this.xmlCodingFactory.getDecodingStream(outputFile, codingType);
        	staxReader.elementContentToOutputStream(outputStream);
        } finally {
        	if (outputStream != null) {
        		try {
        			outputStream.close();
        		} catch (IOException e) {
        			throw new Xb4jException("Exception while closing stream from element content", e);
        		}
        	}
        }
        
    	if ((expectedElement != null) && !staxReader.isAtElementEnd(expectedElement) && startTagFound) {
    		String encountered =  (staxReader.isAtElement()?String.format("(%s)", staxReader.getName()):"");
    		throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", expectedElement,
    				staxReader.getEventName(), encountered), this);
        }
        
        boolean isValueHandled = setProperty(javaContext, outputFile);
        return new UnmarshallResult(outputFile, isValueHandled);
	}
	
	@Override
	public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
    	if (!generatesOutput(javaContext)) { return; }
		
        QName element = getElement();
        JavaContext newJavaContext = getProperty(javaContext);
        if ((newJavaContext == null) && !isOptional()) {	//TODO: check if element is nillable and output nill value for this element
        	throw new Xb4jMarshallException(String.format("No content for mandatory element %s", element), this);	//this does not support an empty element
        }
        
        Object inputObject = (newJavaContext == null? null: newJavaContext.getContextObject());
        if ((inputObject != null) && !(inputObject instanceof File)) {
        	throw new Xb4jMarshallException(String.format("Expected a File instance, but encountered '%s'", inputObject), this);
        }
        File inputFile = (File)inputObject; 
        boolean isEmpty = (inputFile == null) || !inputFile.isFile() || (inputFile.length() == 0);
        if (!isOptional() || !isEmpty) {
        	staxWriter.writeElement(element, isEmpty);
            attributesToXml(staxWriter, javaContext);
        }
        
        if (!isEmpty) {
            InputStream inputStream = null;
            try {
                inputStream = this.xmlCodingFactory.getEncodingStream(new BufferedInputStream(new FileInputStream(inputFile)), codingType);
            	int charsToXml = staxWriter.elementContentFromInputStream(inputStream);
            	if (logger.isDebugEnabled()) {
            		logger.debug(String.format("%d characters written to xml stream (element %s) for %s encoded file content", 
            				charsToXml, element, codingType));
            	}
            } catch (FileNotFoundException e) {
            	throw new Xb4jMarshallException(String.format("Could not open input stream to file %s", inputFile), this);
            } finally {
            	if (inputStream != null) {
            		try {
            			inputStream.close();
            		} catch (IOException e) {
            			throw new Xb4jException("Exception while closing stream to element content", e);
            		}
            	}
            }
            staxWriter.closeElement(element);
        }
	}
	
	@Override
	public Class<?> getJavaType() {
		return File.class;	//this element can only be bound to an java.io.File type
	}
	
	public SimpleFileType setCodingtypeFrom(QName attributeName, String fallbackValue) {
		this.codingTypeAttributeName = attributeName;
		this.codingType = fallbackValue;
		return this;
	}
	
	public SimpleFileType setFilenameHintFrom(QName attributeName, String fallbackValue) {
		this.filenameAttributeName = attributeName;
		this.filenameHint = fallbackValue;
		return this;
	}
	
	/**
	 * Obtain the value of a certain attribute. When the value for the attribute cannot be established, use the staticValue, or 
	 * of null, use the fallbackValue. The latter can never be null.
	 * 
	 * @param staxReader Get the attribute value as found in xml when unmarshalling, either one of staxReader or javaContext must be provided
	 * @param javaContext Get the attribute value from the javaContext when marshalling, either one of staxReader or javaContext must be provided
	 * @param attributeSource the QName to select the attribute defined with this element
	 * @param staticValue
	 * @param fallbackValue
	 * @return
	 */
	private String getAttributeValue(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext, QName attributeSource, String staticValue, String fallbackValue) {
		if ((staxReader == null) && (javaContext == null)) {
			throw new NullPointerException("Either one of RecordAndPlaybackXMLStreamReader or JavaContext must be supplied; both are null");
		}
		if (fallbackValue == null) {
			throw new NullPointerException(String.format("The fallbackValue for attribute %s cannot be null", attributeSource));
		}
		//read value for the attributeSource
		String value = null;
		if (hasAttributes() && (attributeSource != null)) {
			Collection<IAttribute> attributeDefinitions = getAttributes();
			for (IAttribute attributeDefinition: attributeDefinitions) {
				if (attributeDefinition.getAttributeName().equals(attributeSource)) {
					if (staxReader == null) {
						//we are marshalling from Java to Xml:
						value = attributeDefinition.getValue(javaContext);
					} else {
						//we are unmarshalling from Xml to Java
			    		Map<QName, String> attributes = staxReader.getAttributes();
			    		if ((attributes != null) && attributes.containsKey(attributeSource)) {
			    			value = attributes.get(attributeSource);
			    		} else {
			    			value = attributeDefinition.getValue(javaContext);
			    		}
					}
				}
			}
		}
		if (value == null) {
			if (staticValue == null) {
				return fallbackValue;
			}
			return staticValue;
		}
		return value;
	}
	
}
