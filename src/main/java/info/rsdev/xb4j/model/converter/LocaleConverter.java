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

import info.rsdev.xb4j.exceptions.ValidationException;
import info.rsdev.xb4j.model.java.JavaContext;

import java.util.Locale;

/**
 * Convert a string to and from a {@link Locale}
 * 
 * @author Dave Schoorl
 */
public class LocaleConverter implements IValueConverter {
    
    public static final LocaleConverter INSTANCE = new LocaleConverter();
    
    private LocaleConverter() {}

    @Override
    public Locale toObject(JavaContext javaContext, String value) {
        if (value == null) { return null; }
        return parseLocale(value);
    }

    @Override
    public String toText(JavaContext javaContext, Object value) {
        if (value == null) { return null; }
        if (!(value instanceof Locale)) {
            throw new ValidationException(String.format("Expected a %s, but was a %s", Locale.class.getName(), 
                    value.getClass().getName()));
        }
        return ((Locale)value).toString();
    }

    @Override
    public Class<?> getJavaType() {
        return Locale.class;
    }
    
    /**
     * A locale can consist of a language, country and variant part (the latter two are optional)
     * @param value
     * @return
     */
    private static Locale parseLocale(String value) {
    	if (value == null) { return null; }
    	int underscoreIndex = value.indexOf("_");
    	if (underscoreIndex < 0) {
    		return new Locale(value);	//value = language code
    	} else {
        	String language = value.substring(0, underscoreIndex);
        	value = value.substring(underscoreIndex + 1);	//strip language code
    		underscoreIndex = value.indexOf("_");
    		if (underscoreIndex < 0) {
    			return new Locale(language, value);		//value = country code
    		} else {
    	    	String country = value.substring(0, underscoreIndex);
    	    	value = value.substring(underscoreIndex + 1);	//strip country code
    	    	if (value.length() > 0) {
    	    		return new Locale(language, country, value);	//value = variant
    	    	} else {
    	    		return new Locale(language, country);
    	    	}
    		}
    	}
    }
}
