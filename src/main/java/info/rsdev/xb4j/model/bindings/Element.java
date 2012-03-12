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
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * <p>An {@link Element} can represent an element in the xml world. It can also represent a counterpart object in
 * the Java world. At least one of the two is required. An ElementBinding can hold other bindings, E.g. 
 * a {@link Sequence}, a {@link Reference} or a {@link Choice}.</p>
 * <p>An ElementBinding cannot contain text. When you need an element that must contain text, use {@link SimpleType} 
 * instead.</p>
 * 
 * TODO: add support for fixed / default values in the xml world?
 * TODO: simple type cannot be an empty element??
 * 
 * @author Dave Schoorl
 */
public class Element extends AbstractSingleBinding {
	
    /**
     * Create a new {@link Element} with a {@link DefaultElementFetchStrategy}
     * @param element the element 
     */
    public Element(QName element) {
    	super(new DefaultElementFetchStrategy(element), null);
    }
    
    public Element(Class<?> javaType) {
    	super(NoElementFetchStrategy.INSTANCE, new DefaultConstructor(javaType));
    }

    public Element(QName element, Class<?> javaType) {
    	super(new DefaultElementFetchStrategy(element), new DefaultConstructor(javaType));
    }

    public Element(QName element, ICreator creator) {
    	super(new DefaultElementFetchStrategy(element), creator);
    }
    
    public Element(IElementFetchStrategy elementFetcher, ICreator creator) {
    	super(elementFetcher, creator);
    }
    
    /**
     * Copy constructor
     * 
     * @param original
     * @param newElement
     */
    protected Element(Element original, IElementFetchStrategy elementFetcher) {
    	super(original, elementFetcher);
    }
    
    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();
        if ((expectedElement != null) && !staxReader.isAtElementStart(expectedElement)) {
        	return null;
        }
        
        Object newJavaContext = newInstance();
    	IBinding childBinding = getChildBinding();
    	if (childBinding != null) {
    		childBinding.toJava(staxReader, select(javaContext, newJavaContext));
    	}
        setProperty(javaContext, newJavaContext);
        
        if ((expectedElement != null) && !staxReader.isAtElementEnd(expectedElement)) {
            throw new Xb4jException("No End tag encountered: ".concat(expectedElement.toString()));
        }
        
        return newJavaContext;
    }
    
    @Override
    public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        
        //mixed content is not yet supported -- there are either child elements or there is content
    	IBinding childBinding = getChildBinding();
        boolean isEmptyElement = childBinding == null;
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
        }
        
        if (childBinding != null) {
        	childBinding.toXml(staxWriter, getProperty(javaContext));
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
    }
    
}
