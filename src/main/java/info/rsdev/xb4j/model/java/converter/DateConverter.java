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
	public static final DateConverter DATE_TIME = new DateConverter(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
	
	private final SimpleDateFormat formatter;
	
	public DateConverter(SimpleDateFormat formatter) {
		if (formatter == null) {
			throw new NullPointerException("SimpleDateFormat cannot be null");
		}
		this.formatter = formatter;
	}
	
	@Override
	public Object toObject(String value) {
		if (value == null) { return null; }
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
			return formatter.format((Date)value);
		}
	}
	
	@Override
	public Class<?> getJavaType() {
		return Date.class;
	}
	
}
