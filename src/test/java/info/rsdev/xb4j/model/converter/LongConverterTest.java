package info.rsdev.xb4j.model.converter;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import info.rsdev.xb4j.model.java.JavaContext;

import org.junit.Test;

public class LongConverterTest {
	
    @Test
    public void nullValuesAreNotValidated() {
        JavaContext javaContext = mock(JavaContext.class);  //not needed in IntegerConverter implementation
        assertNull(LongConverter.POSITIVE.toObject(javaContext, null));
    }
    
	@Test
	public void emptyStringsAreTreatedAsNullValues() {
        JavaContext javaContext = mock(JavaContext.class);  //not needed in IntegerConverter implementation
        assertNull(LongConverter.POSITIVE.toObject(javaContext, ""));
	}
	
}
