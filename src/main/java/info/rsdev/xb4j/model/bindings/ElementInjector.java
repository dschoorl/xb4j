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

import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.model.bindings.action.IMarshallingAction;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.constructor.NullCreator;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class allows you to insert xml element into the StAX stream, who's value is not present from the java context being 
 * unmarshalled. Instead, the value is provided by an {@link IMarshallingAction}.
 * 
 * @author Dave Schoorl
 */
public class ElementInjector extends AbstractBinding {
	
	private IMarshallingAction valueProvider = null;
	
	public ElementInjector(QName element, IMarshallingAction valueProvider) {
		super(new DefaultElementFetchStrategy(element), NullCreator.INSTANCE);
		setMarshallingAction(valueProvider);
	}
	
	/**
     * Copy constructor
     * 
     * @param original
     */
	protected ElementInjector(ElementInjector original) {
		super(original);
		this.valueProvider = original.valueProvider;
	}

	@Override
	public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        QName element = getElement();	//never null for an ElementInjector
        javaContext = getProperty(javaContext);
        String value = this.valueProvider.execute(javaContext);
        boolean isEmpty = (value == null) || value.isEmpty();
        if (isEmpty && !hasAttributes() && !isOptional()) {
        	throw new Xb4jMarshallException(String.format("No content for mandatory element %s", element), this);	//this does not support an empty element
        }
        
        boolean outputElement = !isEmpty || hasAttributes();
        if (outputElement) {
        	staxWriter.writeElement(element, isEmpty);
            attributesToXml(staxWriter, javaContext);
        }
        
        if (!isEmpty) {
            staxWriter.writeContent(value);
        }
        
        if (outputElement && !isEmpty) {
            staxWriter.closeElement(element);
        }

	}
	
	@Override
	public boolean generatesOutput(JavaContext javaContext) {
		return true;
	}
	
	@Override
	public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
		//swallow element if encountered, always treat this element as optional
        QName expectedElement = getElement();
		if (!staxReader.isAtElementStart(expectedElement)) {
            return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
		}
		
		//start tag is found: consume and ignore xml stream until end tag
		staxReader.skipToElementEnd();
        
		return UnmarshallResult.NO_RESULT;
	}
	
    private void setMarshallingAction(IMarshallingAction valueProvider) {
		if (valueProvider == null) {
			throw new NullPointerException("IMarshallingAction cannot be null");
		}
		this.valueProvider  = valueProvider;
	}

}
