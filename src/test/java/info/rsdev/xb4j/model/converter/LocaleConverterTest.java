package info.rsdev.xb4j.model.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.model.java.JavaContext;

import java.util.Locale;

import org.junit.Test;

public class LocaleConverterTest {
	
	@Test
	public void testToObjectLanguageOnly() {
    	JavaContext javaContext = null;	//not needed in LocaleConverter implementation
		assertNull(LocaleConverter.INSTANCE.toObject(javaContext, null));
		assertEquals(new Locale("nl"), LocaleConverter.INSTANCE.toObject(javaContext, "nl"));
		assertEquals(Locale.ENGLISH, LocaleConverter.INSTANCE.toObject(javaContext, "en"));
	}
	
	@Test
	public void testToObjectLanguagePlusCountry() {
    	JavaContext javaContext = null;	//not needed in LocaleConverter implementation
		assertEquals(Locale.UK, LocaleConverter.INSTANCE.toObject(javaContext, "en_GB"));
		assertEquals(new Locale("nl", "NL"), LocaleConverter.INSTANCE.toObject(javaContext, "nl_NL"));
	}
	
	@Test
	public void testToObjectLanguagePlusCountryPlusVariant() {
    	JavaContext javaContext = null;	//not needed in LocaleConverter implementation
		assertEquals(new Locale("es", "ES", "Traditional_WIN"), LocaleConverter.INSTANCE.toObject(javaContext, "es_ES_Traditional_WIN"));
	}
	
	@Test
	public void testToTextLanguageOnly() {
    	JavaContext javaContext = null;	//not needed in LocaleConverter implementation
		assertNull(LocaleConverter.INSTANCE.toText(javaContext, null));
		assertEquals("nl", LocaleConverter.INSTANCE.toText(javaContext, new Locale("nl")));
		assertEquals("en", LocaleConverter.INSTANCE.toText(javaContext, Locale.ENGLISH));
		assertEquals("en", LocaleConverter.INSTANCE.toText(javaContext, new Locale("en", "", "")));	//omit unneccesary underscores
	}
	
	@Test
	public void testToTextLanguagePlusCountry() {
    	JavaContext javaContext = null;	//not needed in LocaleConverter implementation
		assertEquals("nl_NL", LocaleConverter.INSTANCE.toText(javaContext, new Locale("nl", "NL")));
		assertEquals("en_GB", LocaleConverter.INSTANCE.toText(javaContext, Locale.UK));
	}
	
	@Test
	public void testToTextLanguagePlusCountryPlusVariant() {
    	JavaContext javaContext = null;	//not needed in LocaleConverter implementation
		assertEquals("es_ES_Traditional_WIN", LocaleConverter.INSTANCE.toText(javaContext, new Locale("es", "ES", "Traditional_WIN")));
	}
	
	@Test
	public void testGetJavaType() {
		assertSame(Locale.class, LocaleConverter.INSTANCE.getJavaType());
	}
	
}
