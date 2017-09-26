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

import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.java.constructor.NullCreator;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * <p>
 * An {@link Element} can represent an element in the xml world. It can also represent a counterpart object in the Java world. At
 * least one of the two is required. An ElementBinding can hold other bindings, E.g. a {@link Sequence}, a {@link Reference} or a
 * {@link Choice}.</p>
 * <p>
 * An ElementBinding cannot contain text. When you need an element that must contain text, use {@link SimpleType} instead.</p>
 *
 * TODO: add support for fixed / default values in the xml world? TODO: simple type cannot be an empty element??
 *
 * @author Dave Schoorl
 */
public class Element extends AbstractSingleBinding {

    /**
     * Create a new {@link Element} with a {@link DefaultElementFetchStrategy}
     *
     * @param element the element
     */
    public Element(QName element) {
        super(new DefaultElementFetchStrategy(element), NullCreator.INSTANCE);
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
     * @param elementFetcher
     */
    protected Element(Element original, IElementFetchStrategy elementFetcher) {
        super(original, elementFetcher);
    }

    @Override
    public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();
        boolean startTagFound = false;
        if (expectedElement != null) {
            if (!staxReader.isCurrentAnElementStart(expectedElement) && !staxReader.isNextAnElementStart(expectedElement)) {
                if (isOptional()) {
                    return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
                } else {
                    return UnmarshallResult.newMissingElement(this);
                }
            } else {
                startTagFound = true;
            }
        }

        JavaContext newJavaContext = newInstance(staxReader, javaContext);
        attributesToJava(staxReader, select(javaContext, newJavaContext));

        IBinding childBinding = getChildBinding();
        UnmarshallResult result = null;
        if (childBinding != null) {
            result = childBinding.toJava(staxReader, select(javaContext, newJavaContext));
        }

        if ((result != null) && !result.isUnmarshallSuccessful()) {
            return result;	//abort unmarshalling and let error bubble up
        }

        //before processing the result of the unmarshalling, first check if the xml is well-formed
        if ((expectedElement != null) && !staxReader.isNextAnElementEnd(expectedElement) && startTagFound) {
            String encountered = (staxReader.isAtElement() ? String.format("(%s)", staxReader.getName()) : "");
            throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered %s %s", expectedElement,
                    staxReader.getEventName(), encountered), this);
        }

        //process the UnmarshallResult
        /* When the UnmarshallResult yields an object, it must either be set on the current context or passed on to it's parent. 
         * 
         */
        if ((result != null) && result.mustHandleUnmarshalledObject()) {
            if (!setProperty(select(javaContext, newJavaContext), result.getUnmarshalledObject())) {
                //the unmarshalled object could net be set on the (new) java context
                if (newJavaContext == null) {
                    return result;
                } else {
                    throw new Xb4jUnmarshallException(String.format("Unmarshalled object '%s' not set in %s by binding %s",
                            result.getUnmarshalledObject(), select(javaContext, newJavaContext), this), this);
                }
            } else {
                result.setHandled();
            }
        } else {
            //or set the newly created Java object in the current Java context
            if (setProperty(javaContext, newJavaContext.getContextObject())) {
                return new UnmarshallResult(newJavaContext.getContextObject(), true);
            }
        }
        
        if (newJavaContext.getContextObject() != null) {
            return new UnmarshallResult(newJavaContext.getContextObject());
        }
        return result;
    }

    @Override
    public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        if (!generatesOutput(javaContext)) {
            return;
        }

        //mixed content is not yet supported -- there are either child elements or there is content
        QName element = getElement();
        //is element empty?
        IBinding child = getChildBinding();
        JavaContext nextJavaContext = getProperty(javaContext);
        boolean isEmpty = (child == null) || !child.generatesOutput(nextJavaContext);
        boolean outputElement = ((element != null) && (!isOptional() || !isEmpty));
        if (outputElement) {
            staxWriter.writeElement(element, isEmpty);
            attributesToXml(staxWriter, nextJavaContext);
        }

        if (!isEmpty) {
            child.toXml(staxWriter, nextJavaContext);
        }

        if (outputElement && !isEmpty) {
            staxWriter.closeElement(element);
        }
    }

    @Override
    public boolean generatesOutput(JavaContext javaContext) {
        javaContext = getProperty(javaContext);
        if (javaContext.getContextObject() != null) {
            IBinding child = getChildBinding();
            if ((child != null) && child.generatesOutput(javaContext)) {
                return true;
            }
        }

        //At this point, the childBinding will have no output
        return (getElement() != null) && (hasAttributes() || !isOptional());	//suppress optional empty elements (empty means: no content and no attributes)
    }

}
