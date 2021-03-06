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
 * Convert anything and everything to null, allways. The sole purpose of this converter at the moment of inception is to aid
 * automated testing.
 *
 * @author Dave Schoorl
 */
public class NullConverter implements IValueConverter {

    public static final NullConverter INSTANCE = new NullConverter();

    private NullConverter() {
    }

    @Override
    public Object toObject(JavaContext javaContext, String value) {
        return null;
    }

    @Override
    public String toText(JavaContext javaContext, Object value) {
        return null;
    }

    @Override
    public Class<?> getJavaType() {
        return Object.class;
    }

}
