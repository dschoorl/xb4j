package info.rsdev.xb4j.model.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.rsdev.xb4j.model.java.JavaContext;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Test the conversion support for data, time and dateTime schema data types to Java Date instances. The same xml
 * values must be easily transfered to other date types, such as Calendar.
 * 
 * Note: these tests should pass, no matter what timezone is set on the machine running the tests
 * 
 * 
 * @author dschoorl
 */
public class DateConverterTest {
	
	/**
	 * Test the happy flows in converting DateTime schema data types into Java Date objects
	 */
	@Test
	public void testDateTimeToText() throws Exception {
    	JavaContext javaContext = null;	//not needed in DateConverter implementation
		TimeZone timeZone = TimeZone.getTimeZone("GMT+05:00");
		Calendar march17 = new GregorianCalendar(2012, 2, 17, 12, 34, 56);
		march17.setTimeZone(timeZone);
		march17.add(Calendar.MILLISECOND, 789);
		DateConverter.XML_DATETIME.setTimeZone(timeZone);
		String result = DateConverter.XML_DATETIME.toText(javaContext, march17.getTime());
		assertEquals("2012-03-17T12:34:56.789+05:00", result);
	}
	
	@Test
	public void testToObject() {
    	JavaContext javaContext = null;	//not needed in DateConverter implementation
		Date result = (Date)DateConverter.XML_DATETIME.toObject(javaContext, "2012-03-14T08:23:00+01:00");
		assertNotNull(result);
	}
	
}
