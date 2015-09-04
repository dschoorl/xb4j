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
package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.model.converter.IValueConverter;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.constructor.IJavaArgument;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class SimpleArgument extends SimpleType implements IJavaArgument {

    private Ignore xmlConsumer = null;
    
    public SimpleArgument(QName element) {
        super(element);
    }

    public SimpleArgument(QName element, IValueConverter converter) {
        super(element, converter);
    }

    @Override
    public UnmarshallResult getParameterValue(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext)
            throws XMLStreamException {
        return super.unmarshall(staxReader, javaContext);
    }
    
    @Override
    public UnmarshallResult toJava(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        if (xmlConsumer == null) {
            xmlConsumer = new Ignore(getElement());
            xmlConsumer.setOptional(isOptional());
        }
        return xmlConsumer.toJava(staxReader, javaContext);
    }
    
    @Override
    public boolean setProperty(JavaContext javaContext, Object propertyValue) {

        /* do not set the unmarshalled value as a property on the JavaContext-object, because it must be used as an 
         * argument by an ICreator implementation
         */
        return true;
    }

}
