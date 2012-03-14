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
package info.rsdev.xb4j.model.java.converter;

import info.rsdev.xb4j.exceptions.ValidationException;

/**
 * Convert a string to and from a boolean value
 * 
 * @author Dave Schoorl
 */
public class BooleanConverter implements IValueConverter {
    
    public static final BooleanConverter INSTANCE = new BooleanConverter();
    
    private BooleanConverter() {}

    @Override
    public Boolean toObject(String value) {
        if (value == null) { return null; }
        if (value.equalsIgnoreCase("true")) { return Boolean.TRUE; }
        if (value.equalsIgnoreCase("false")) {return Boolean.FALSE; }
        throw new ValidationException("Not a boolean value (Expected one of: {true, false}): ".concat(value));
    }

    @Override
    public String toText(Object value) {
        if (value == null) { return null; }
        if (!(value instanceof Boolean)) {
            throw new ValidationException(String.format("Expected a %s, but was a %s", Boolean.class.getName(), 
                    value.getClass().getName()));
        }
        return value.toString();
    }

    @Override
    public Class<?> getJavaType() {
        return Boolean.class;
    }

}
