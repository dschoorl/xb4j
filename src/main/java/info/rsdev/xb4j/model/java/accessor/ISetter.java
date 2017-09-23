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
 * An {@link ISetter} is used during the unmarshalling process (from xml to java). It will set the result that was returned from the
 * {@link IBinding#toXml(SimplifiedXMLStreamWriter, JavaContext)} method of a childbinding in the current javaContext.
 *
 * @author Dave Schoorl
 */
public interface ISetter {

    /**
     * Set the given value on the provided javaContext object. The implementation knows how to do this.
     *
     * @param javaContext the object to change by setting the the value on it
     * @param value the value to set on the
     * @return true if the javaContext was succesfully changed, false otherwise.
     */
    boolean set(JavaContext javaContext, Object value);

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object obj);

}
