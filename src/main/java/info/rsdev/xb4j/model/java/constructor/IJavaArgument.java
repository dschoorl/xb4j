/* Copyright 2015 Red Star Development / Dave Schoorl
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
package info.rsdev.xb4j.model.java.constructor;

import javax.xml.stream.XMLStreamException;

import info.rsdev.xb4j.model.bindings.IAttribute;
import info.rsdev.xb4j.model.bindings.IBinding;
import info.rsdev.xb4j.model.bindings.UnmarshallResult;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;

/**
 * This class marks an {@link IBinding} or {@link IAttribute} as a parameter value. This means that the unmarshalled value will not
 * be set on a {@link JavaContext}, but instead, it will be used in a {@link ICreator} implementation
 *
 * @author Dave Schoorl
 * @see ArgsConstructor an {@link ICreator} implementation that calls a Java constructor with parameters
 */
public interface IJavaArgument {

    public UnmarshallResult getParameterValue(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException;

}
