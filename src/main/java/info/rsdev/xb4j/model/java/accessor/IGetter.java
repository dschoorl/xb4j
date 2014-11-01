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
package info.rsdev.xb4j.model.java.accessor;

import info.rsdev.xb4j.model.bindings.IBinding;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

/**
 * An {@link IGetter} is used during the marshalling process (from java to xml). It extracts from the current 
 * javaContext the new javaContext that will be pushed downwards in the binding hierarchy through the 
 * {@link IBinding#toXml(SimplifiedXMLStreamWriter, JavaContext)} method.
 * 
 * @author Dave Schoorl
 */
public interface IGetter {
	
	public JavaContext get(JavaContext javaContext);
	
    public int hashCode();
    
    public boolean equals(Object obj);
}
