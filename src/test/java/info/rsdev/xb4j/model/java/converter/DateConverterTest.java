package info.rsdev.xb4j.model.java.converter;

import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;

/**
 * These test are worthless. I only created them to see how timezone information should be passed
 * 
 * @author dschoorl
 */
public class DateConverterTest {
	
	@Test
	public void testToText() {
		String result = DateConverter.DATE_TIME.toText(new Date());
		assertNotNull(result);
	}
	
	@Test
	public void testToObject() {
		Date result = DateConverter.DATE_TIME.toObject("2012-03-14T08:23:00+01:00");
		assertNotNull(result);
	}
	
}
