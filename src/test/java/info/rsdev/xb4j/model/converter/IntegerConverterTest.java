package info.rsdev.xb4j.model.converter;

import static org.junit.Assert.assertEquals;
import info.rsdev.xb4j.model.java.JavaContext;

import org.junit.Test;

public class IntegerConverterTest {
	
	@Test
	public void testToObjectWithPadding() {
    	JavaContext javaContext = null;	//not needed in IntegerConverter implementation
		assertEquals("01", new IntegerConverter(NoValidator.INSTANCE, 2).toText(javaContext, Integer.valueOf(1)));
	}
	
}
