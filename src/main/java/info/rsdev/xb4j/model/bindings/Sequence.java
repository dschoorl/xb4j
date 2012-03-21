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

import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

/**
 * Group a number of elements where ordering is fixed. Elements can be optional. When an element can occur more than once, you 
 * must wrap them inside a {@link Repeater}.
 * 
 * @author Dave Schoorl
 */
public class Sequence extends AbstractBindingContainer {
	
	/**
	 * Create a new {@link Sequence} which inherits it's element and javatype from it's parent
	 */
	public Sequence() {
		super(NoElementFetchStrategy.INSTANCE, null);
	}
	
    public Sequence(QName element) {
    	super(new DefaultElementFetchStrategy(element), null);
    }

	public Sequence(Class<?> javaType) {
		super(NoElementFetchStrategy.INSTANCE, new DefaultConstructor(javaType));
	}
    
    public Sequence(QName element, Class<?> javaType) {
    	super(new DefaultElementFetchStrategy(element), new DefaultConstructor(javaType));
    }
    
    @Override
    public Sequence setOptional(boolean isOptional) {
    	super.setOptional(isOptional);
    	return this;
    }

    @Override
    public UnmarshallResult toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
    	QName expectedElement = getElement();
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
    	
    	UnmarshallResult result = null;
    	Object newJavaContext = newInstance();
        for (IBinding child: getChildren()) {
        	result = child.toJava(staxReader, select(javaContext, newJavaContext));
        	if (!result.isUnmarshallSuccessful()) {
        		return result;	//this sequence is incomplete (mandatory elements are missing)
        	}
        	if (result.mustHandleUnmarshalledObject()) {
        		if (!setProperty(select(javaContext, newJavaContext), result.getUnmarshalledObject())) {
        			//the unmarshalled object could not be set on the (new) java context
        			String message = String.format("Unmarshalled object '%s' not set in Java context '%s'. ", 
    						result.getUnmarshalledObject(), select(javaContext, newJavaContext));
        			if (!hasSetter()) {
        				message = message.concat("No ISetter defined.");
        			}
    				throw new Xb4jUnmarshallException(message, this);
        		}
        	}
        }
        
    	//before processing the result of the unmarshalling, first check if the xml is wellformed
    	if ((expectedElement != null) && !staxReader.isAtElementEnd(expectedElement) && startTagFound) {
    		String encountered =  (staxReader.isAtElement()?String.format("(%s)", staxReader.getName()):"");
    		throw new Xb4jException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", expectedElement,
    				staxReader.getEventName(), encountered));
    	}
    	
		//or set the newly created Java object int he current Java context
		if (setProperty(javaContext, newJavaContext)) {
	        return new UnmarshallResult(newJavaContext, true);
		}
        return new UnmarshallResult(newJavaContext);
    }
    
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        
        //mixed content is not yet supported -- there are either child elements or there is content
        Collection<IBinding> children = getChildren();
        boolean isEmptyElement = children.isEmpty();	//TODO: take isOptional properties into account
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
        }
        
        for (IBinding child: children) {
            child.toXml(staxWriter, getProperty(javaContext));
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
    }
    
}
