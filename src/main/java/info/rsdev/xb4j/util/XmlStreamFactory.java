/* Copyright 2013 Red Star Development / Dave Schoorl
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
package info.rsdev.xb4j.util;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public abstract class XmlStreamFactory {

	private XmlStreamFactory() {}
	
	public static XMLStreamWriter makeWriter(OutputStream stream) {
		XMLStreamWriter staxWriter = null;
        try {
            staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
        } catch (XMLStreamException e) {
            throw new Xb4jException("Cannot create XMLStreamWriter", e);
        }
        return staxWriter;
	}
	
	public static XMLStreamReader makeReader(InputStream stream) {
    	XMLStreamReader staxReader = null;
        try {
        	staxReader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        } catch (XMLStreamException e) {
        	throw new Xb4jException("Cannot create XMLStreamReader", e);
        }
        return staxReader;
	}
	
}
