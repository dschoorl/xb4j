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
package info.rsdev.xb4j.model.converter;

import info.rsdev.xb4j.model.java.JavaContext;

/**
 * Convert a String to a specific Java object and vice versa
 *
 * @author Dave Schoorl
 */
public interface IValueConverter {

    /**
     * Convert a String to the required Java object representation.
     *
     * @param javaContext the {@link JavaContext} that may be used to aid in the conversion process
     * @param value the String value to convert into a Java object representation
     * @return the Java representation of the given value or null when there is no value
     */
    public Object toObject(JavaContext javaContext, String value);

    public String toText(JavaContext javaContext, Object value);

    public Class<?> getJavaType();

}
