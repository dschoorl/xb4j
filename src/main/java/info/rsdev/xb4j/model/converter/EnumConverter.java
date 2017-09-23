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
package info.rsdev.xb4j.model.converter;

import info.rsdev.xb4j.exceptions.ValidationException;
import info.rsdev.xb4j.model.java.JavaContext;

/**
 * Convert Strings to and from enum constants. The enum to use must be known upfront and the current support is limited to their
 * constant name; conversion on the basis of the values of custom fields are not supported.
 *
 * @author Dave Schoorl
 */
public class EnumConverter implements IValueConverter {

    private final Class<? extends Enum<?>> enumType;

    public EnumConverter(Class<? extends Enum<?>> enumType) {
        this.enumType = enumType;
    }

    @Override
    public Object toObject(JavaContext javaContext, String value) {
        if ((value == null) || value.isEmpty()) {
            return null;
        }
        for (Enum<?> constant : enumType.getEnumConstants()) {
            if (constant.name().equals(value)) {
                return constant;
            }
        }
        throw new ValidationException(String.format("%s is not a constant value in enum '%s'", value, enumType));
    }

    @Override
    public String toText(JavaContext javaContext, Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Enum)) {
            throw new ValidationException(String.format("Expected an enum, but was a %s", value.getClass().getName()));
        }

        return value.toString();
    }

    @Override
    public Class<?> getJavaType() {
        return enumType.getClass();
    }

}
