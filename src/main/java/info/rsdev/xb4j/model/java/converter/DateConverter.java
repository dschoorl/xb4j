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
import info.rsdev.xb4j.exceptions.Xb4jException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Dave Schoorl
 */
public class DateConverter implements IValueConverter {
	
	/**
	 * This Singleton instance of {@link DateConverter} allows only a date as YYYY-MM-DD, without timezone information
	 */
	public static final DateConverter DATE_ONLY = new DateConverter(new SimpleDateFormat("yyyy-MM-dd"));
	
	/**
	 * This Singleton instance of {@link DateConverter} allows only a date as YYYY-MM-DDThh:mm:ss, without timezone information.
	 * The hours are noted in 24hrs format, no AM/PM indicator
	 */
	public static final DateConverter DATE_TIME = new DateConverter(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
	
	private final SimpleDateFormat formatter;
	
	public DateConverter(SimpleDateFormat formatter) {
		if (formatter == null) {
			throw new NullPointerException("SimpleDateFormat cannot be null");
		}
		this.formatter = formatter;
	}
	
	@Override
	public Date toObject(String value) {
		if (value == null) { return null; }
		
		value = removeTimezoneSeparator(value);
		try {
			synchronized(formatter) {
				return formatter.parse(value);
			}
		} catch (ParseException e) {
			throw new ValidationException(String.format("Date does not obey format %s: %s", formatter.toPattern(), value), e);
		}
	}
	
	@Override
	public String toText(Object value) {
		if (value == null) { return null; }
		if (!(value instanceof Date)) {
			throw new Xb4jException(String.format("Expected a %s, but was a %s", Date.class.getName(), 
					value.getClass().getName()));
		}
		synchronized(formatter) {
			String result = formatter.format((Date)value);
			return addTimeZoneSeparator(result);
		}
	}
	
	/**
	 * In Java the SimpleDateFormat expects no column between the hours and minutes of the timezone, whereas xml 
	 * does (+0100 vs. +01:00). This method bridges the gap
	 * @param value
	 * @return
	 */
	private String removeTimezoneSeparator(String value) {
		return value.substring(0, 22).concat(value.substring(23));
	}
	
	private String addTimeZoneSeparator(String value) {
		return value.substring(0, 22).concat(":").concat(value.substring(22));
	}
	
	@Override
	public Class<?> getJavaType() {
		return Date.class;
	}
	
}
