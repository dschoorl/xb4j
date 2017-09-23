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
 * Converts the object add hand to a String, by calling the {@link #toString()} method on the value. The Singleton
 * {@link ToStringConverter#INSTANCE} does not perform any validation.
 *
 * @author Dave Schoorl
 */
public class ToStringConverter implements IValueConverter {

    public static final ToStringConverter INSTANCE = new ToStringConverter();

    private final IValidator validator;

    private ToStringConverter() {
        this.validator = NoValidator.INSTANCE;
    }

    public ToStringConverter(IValidator validator) {
        if (validator == null) {
            throw new NullPointerException("you must provide a IValidator implementation");
        }
        this.validator = validator;
    }

    @Override
    public Object toObject(JavaContext javaContext, String value) {
        return value;
    }

    @Override
    public String toText(JavaContext javaContext, Object value) {
        return validator.isValid(value == null ? null : value.toString());
    }

    @Override
    public Class<?> getJavaType() {
        return String.class;
    }

}
