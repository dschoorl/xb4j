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
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.bindings.action.IPhasedAction;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.model.java.constructor.ICreator;
import info.rsdev.xb4j.model.java.constructor.IJavaArgument;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This interface defines how to transform from Java instance to xml and visa versa, regardless whether the binding represents a
 * single element or a group of elements.
 *
 * @author Dave Schoorl
 */
public interface IBinding {

    /**
     * Read the xml from the staxReader and produce the Java instance that it binds to.
     *
     * @param staxReader
     * @param javaContext the {@link JavaContext} to operate on
     * @return the {@link UnmarshallResult} from this binding.
     * @throws XMLStreamException
     */
    UnmarshallResult toJava(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException;

    /**
     *
     * @param staxWriter
     * @param javaContext
     * @throws XMLStreamException
     */
    void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException;

    /**
     * Determine if the binding will output anything to xmlStream, so that we can know if we have to output an empty mandatory
     * container tag, or suppress an empty optional container tag. Or, of course, output a non-empty element to the xml stream.
     *
     * @param javaContext
     * @return
     */
    boolean generatesOutput(JavaContext javaContext);

    /**
     * Bindings are organized in a hierarchy. Call setParent to build the hierarchy of bindings.
     *
     * @param parent the parent {@link IBinding} that this binding is a child of.
     */
    void setParent(IBinding parent);

    /**
     *
     * @return
     */
    IBinding getParent();

    QName getElement();

    IBinding addAttribute(IAttribute attribute, String fieldName);

    IBinding addAttribute(IAttribute attribute, IGetter getter, ISetter setter);

    Class getJavaType();

    /**
     * Get the {@link JavaContext} that will be passed on to nested bindings. The new JavaContext is based on the currentContext,
     * meaning that any external context objects are passed on, and the context object is set with the value created by this
     * binding. If this binding does not create a new context object, then the value will be set to null.
     *
     * @param staxReader the xml stream that is currently read. It may be necessary to determine which java type must be created,
     * E.g. in case of a choice
     * @param currentContext the current JavaContext that was passed on to this binding
     * @return a new {@link JavaContext} with the context object created by this binding or null when no contex object is created
     */
    JavaContext newInstance(RecordAndPlaybackXMLStreamReader staxReader, JavaContext currentContext);

    JavaContext getProperty(JavaContext javaContext);

    boolean setProperty(JavaContext javaContext, Object propertyValue);

    /**
     * The implementation of an {@link IGetter} to be used when the binding wants to obtain the Java instance that needs to be
     * marshalled from the current Java context in {@link #getProperty(JavaContext)}
     *
     * @param getter the {@link IGetter} implementation to use. Use {@link NoGetter#INSTANCE} when the Java context need not be
     * changed
     * @return this {@link IBinding}
     */
    IBinding setGetter(IGetter getter);

    /**
     * The implementation of an {@link ISetter} to be used when the binding wants to process it's unmarshalled result through the
     * {@link #setProperty(JavaContext, Object)} method.
     *
     * @param setter the {@link ISetter} implementation to use. Use {@link NoSetter#INSTANCE} when the unmarshalled result must not
     * be handled by this {@link IBinding}, but by one of it's ancestors in the binding tree.
     * @return this {@link IBinding}
     */
    IBinding setSetter(ISetter setter);

    IBinding addAction(IPhasedAction action);

    /**
     * Whether a binding is optional, is only relevant when it has an xml representation. Checking for presence of an element in
     * this binding definition must be done by the developer where applicable, prior to calling this method. This method simply
     * returns the value of the isOptional indicator.
     *
     * @return true if the element (when applicable) can appear in the xml, false if it must appear in the xml
     */
    boolean isOptional();

    <T extends IBinding> T setOptional(boolean isOptional);

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    String getPath();

    /**
     * Get the {@link ISemaphore} instance that is at the root of the binding tree that this binding belongs to. If the binding does
     * not belong to a binding tree yet, a {@link NullSafeSemaphore} instance should be returned, to support threadsafe operations
     * on this binding tree.
     *
     * @return An {@link ISemaphore} instance that represent the root of the binding tree, or {@link NullSafeSemaphore} when the
     * root is not an instance of {@link ISemaphore}
     */
    ISemaphore getSemaphore();

    IModelAware getModelAware();

    void validateMutability();

    void resolveReferences();

    /**
     * Search the binding tree for the {@link IBinding} or {@link IAttribute} that can create the java object that is required as an
     * argument by an {@link ICreator} implementation.
     *
     * @param argumentQName the element or attribute name that identifies the xml snippet to unmarshall to the IJavaArgument
     * @return
     */
    IJavaArgument findArgumentBindingOrAttribute(QName argumentQName);

    /**
     * Check if the java type being marshalled matches what is expected by the binding.
     *
     * @param javaContext the java context containing the context object to validate
     * @throws Xb4jException when there is a mismatch between the expected and the actual Java context object
     */
    default void validateContextObject(JavaContext javaContext) throws Xb4jException {
        javaContext = getProperty(javaContext);
        if ((this.getJavaType() != null) && (javaContext.getContextObject() != null)) {
            if (!this.getJavaType().isAssignableFrom(javaContext.getContextObject().getClass())) {
                throw new Xb4jUnmarshallException(String.format("expected %s, but encountered %s",
                        this.getJavaType(), javaContext.getContextObject().getClass()), this);
            }
        }
    }

}
