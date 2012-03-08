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
package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.FetchFromParentStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * <p>An {@link ElementBinding} can represent an element in the xml world. It can also represent a counterpart object in
 * the Java world. At least one of the two is required. An ElementBinding can hold other bindings, E.g. 
 * a {@link SequenceBinding}, a {@link ComplexTypeReference} or a {@link ChoiceBinding}.</p>
 * <p>An ElementBinding cannot contain text. When you need an element that must contain text, use {@link SimpleTypeBinding} 
 * instead.</p>
 * 
 * TODO: add support for fixed / default values in the xml world?
 * TODO: simple type cannot be an empty element??
 * 
 * @author Dave Schoorl
 */
public class ElementBinding extends AbstractSingleBinding {
	
    public ElementBinding() {
    	setElementFetchStrategy(new FetchFromParentStrategy(this));
    }

    /**
     * Create a new {@link ElementBinding} with a {@link DefaultElementFetchStrategy}
     * @param element the element 
     */
    public ElementBinding(QName element) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    }
    
    public ElementBinding(Class<?> javaType) {
        setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
        setObjectCreator(new DefaultConstructor(javaType));
    }

    public ElementBinding(QName element, Class<?> javaType) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    	setObjectCreator(new DefaultConstructor(javaType));
    }

    public ElementBinding(QName element, ICreator creator) {
    	setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    	setObjectCreator(creator);
    }
    
    public ElementBinding(IElementFetchStrategy elementFetcher, ICreator creator) {
        setElementFetchStrategy(elementFetcher);
        setObjectCreator(creator);
    }
    
    @Override
    public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();
        if ((expectedElement != null) && !staxReader.isAtElementStart(expectedElement)) {
        	return null;
        }
        
        Object newJavaContext = newInstance();
    	IBindingBase childBinding = getChildBinding();
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
    	IBindingBase childBinding = getChildBinding();
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
