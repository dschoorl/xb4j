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
import java.util.Collection;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Group a number of elements where ordering is fixed. Elements can be optional. When an element can occur more than once, you must
 * wrap them inside a {@link Repeater}.
 *
 * @author Dave Schoorl
 */
public class Sequence extends AbstractContainerBinding {

    /**
     * Create a new {@link Sequence} which inherits it's element and javatype from it's parent
     * @param isOptional
     * @param options
     */
    @SafeVarargs
    public Sequence(boolean isOptional, Enum<? extends BindOption>... options) {
        super(NoElementFetchStrategy.INSTANCE, NullCreator.INSTANCE, isOptional, options);
    }

    /**
     * Create a new {@link Sequence} with full control over the implementations of {@link IElementFetchStrategy} and
     * {@link ICreator} for this binding.
     *
     * @param elementFetcher The {@link IElementFetchStrategy} to use. If not provided, {@link NoElementFetchStrategy} is used.
     * @param objectCreator The {@link ICreator} to use. If not provided, {@link NullCreator} is used.
     * @param isOptional
     * @param options
     */
    @SafeVarargs
    public Sequence(IElementFetchStrategy elementFetcher, ICreator objectCreator, boolean isOptional, 
            Enum<? extends BindOption>... options) {
        super((elementFetcher == null ? NoElementFetchStrategy.INSTANCE : elementFetcher),
                (objectCreator == null ? NullCreator.INSTANCE : objectCreator), isOptional, options);
    }

    @SafeVarargs
    public Sequence(QName element, boolean isOptional, Enum<? extends BindOption>... options) {
        super(new DefaultElementFetchStrategy(element), NullCreator.INSTANCE, isOptional, options);
    }

    @SafeVarargs
    public Sequence(Class<?> javaType, boolean isOptional, Enum<? extends BindOption>... options) {
        super(NoElementFetchStrategy.INSTANCE, new DefaultConstructor(javaType), isOptional, options);
    }

    @SafeVarargs
    public Sequence(QName element, Class<?> javaType, boolean isOptional, Enum<? extends BindOption>... options) {
        super(new DefaultElementFetchStrategy(element), new DefaultConstructor(javaType), isOptional, options);
    }

    @SafeVarargs
    public Sequence(QName element, ICreator creator, boolean isOptional, Enum<? extends BindOption>... options) {
        super(new DefaultElementFetchStrategy(element), creator, isOptional, options);
    }

    @Override
    public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        QName expectedElement = getElement();
        if (expectedElement != null) {
            if (!staxReader.isNextAnElementStart(expectedElement)) {
                if (isOptional()) {
                    return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
                }
                return UnmarshallResult.newMissingElement(this);
            }
        }

        JavaContext newJavaContext = newInstance(staxReader, javaContext);
        attributesToJava(staxReader, select(javaContext, newJavaContext));

        if (isNil(staxReader)) {
            return handleNil(staxReader);
        } else {
            for (IBinding child : getChildren()) {
                UnmarshallResult unmarshallChildResult = child.toJava(staxReader, select(javaContext, newJavaContext));
                if (!unmarshallChildResult.isUnmarshallSuccessful()) {
                    return unmarshallChildResult;	//this sequence is incomplete (mandatory elements are missing)
                }
                if (unmarshallChildResult.mustHandleUnmarshalledObject()) {
                    if (!setProperty(select(javaContext, newJavaContext), unmarshallChildResult.getUnmarshalledObject())) {
                        //the unmarshalled object could not be set on the (new) java context
                        String message = String.format("Unmarshalled object '%s' not set in Java context '%s'. ",
                                unmarshallChildResult.getUnmarshalledObject(), select(javaContext, newJavaContext).getContextObject());
                        if (!hasSetter()) {
                            message = message.concat("No ISetter defined.");
                        }
                        throw new Xb4jUnmarshallException(message, this);
                    }
                }
            }

            //before processing the result of the unmarshalling, first check if the xml is wellformed
            if ((expectedElement != null) && !staxReader.isNextAnElementEnd(expectedElement)) {
                String encountered = (staxReader.isAtElement() ? String.format("(%s)", staxReader.getName()) : "");
                throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", expectedElement,
                        staxReader.getEventName(), encountered), this);
            }

            //or set the newly created Java object in the current Java context
            if (newJavaContext.getContextObject() != null) {
                if (setProperty(javaContext, newJavaContext.getContextObject())) {
                    return new UnmarshallResult(newJavaContext.getContextObject(), true);
                }
                return new UnmarshallResult(newJavaContext.getContextObject());
            }

            return UnmarshallResult.NO_RESULT;
        }
    }

    @Override
    public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
        if (generatesOutput(javaContext) == OutputState.NO_OUTPUT) {
            return;
        }

        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();

        //mixed content is not yet supported -- there are either child elements or there is content
        Collection<IBinding> children = getChildren();
        boolean isEmptyElement = children.isEmpty();	//TODO: take isOptional properties into account
        //TODO: reliably determine if child bindings have output -- new API method hasContent?
        javaContext = getProperty(javaContext);
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
            attributesToXml(staxWriter, javaContext);
        }

        for (IBinding child : children) {
            child.toXml(staxWriter, javaContext);
        }

        if (element != null) {
            staxWriter.closeElement(element, isEmptyElement);
        }
    }

    /**
     * Determine if the container has any childbindings that will output anything to xmlStream, so that we can no if we have to
     * output an empty mandatory container tag, or suppress an empty optional container tag. This method must be added to the
     * IBinding interface.
     *
     * @param javaContext
     * @return
     */
    @Override
    public OutputState generatesOutput(JavaContext javaContext) {
        javaContext = getProperty(javaContext);
        if (javaContext.getContextObject() != null) {
            //when a binding has data for output, it should always be generated
            for (IBinding child : getChildren()) {
                OutputState outputState = child.generatesOutput(javaContext);
                if ((outputState == OutputState.HAS_OUTPUT) ||
                        ((outputState == OutputState.COLLABORATE) && !isOptional())) {
                    return OutputState.HAS_OUTPUT;
                }
            }
        }

        if (attributesGenerateOutput(javaContext) == OutputState.HAS_OUTPUT) {
            return OutputState.HAS_OUTPUT;
        }

        //when there is nothing to output
        return isOptional() ? OutputState.NO_OUTPUT : OutputState.COLLABORATE;
    }

}
