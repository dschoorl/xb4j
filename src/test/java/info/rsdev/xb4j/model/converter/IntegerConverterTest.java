package info.rsdev.xb4j.model.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IntegerConverterTest {
	
	@Test
	public void testToObjectWithPadding() {
		assertEquals("01", new IntegerConverter(NoValidator.INSTANCE, 2).toText(new Integer(1)));
	}
	
}
