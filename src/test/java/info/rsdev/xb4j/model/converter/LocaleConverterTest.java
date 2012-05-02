package info.rsdev.xb4j.model.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Locale;

import org.junit.Test;

public class LocaleConverterTest {
	
	@Test
	public void testToObjectLanguageOnly() {
		assertNull(LocaleConverter.INSTANCE.toObject(null));
		assertEquals(new Locale("nl"), LocaleConverter.INSTANCE.toObject("nl"));
		assertEquals(Locale.ENGLISH, LocaleConverter.INSTANCE.toObject("en"));
	}
	
	@Test
	public void testToObjectLanguagePlusCountry() {
		assertEquals(Locale.UK, LocaleConverter.INSTANCE.toObject("en_GB"));
		assertEquals(new Locale("nl", "NL"), LocaleConverter.INSTANCE.toObject("nl_NL"));
	}
	
	@Test
	public void testToObjectLanguagePlusCountryPlusVariant() {
		assertEquals(new Locale("es", "ES", "Traditional_WIN"), LocaleConverter.INSTANCE.toObject("es_ES_Traditional_WIN"));
	}
	
	@Test
	public void testToTextLanguageOnly() {
		assertNull(LocaleConverter.INSTANCE.toText(null));
		assertEquals("nl", LocaleConverter.INSTANCE.toText(new Locale("nl")));
		assertEquals("en", LocaleConverter.INSTANCE.toText(Locale.ENGLISH));
		assertEquals("en", LocaleConverter.INSTANCE.toText(new Locale("en", "", "")));	//omit unneccesary underscores
	}
	
	@Test
	public void testToTextLanguagePlusCountry() {
		assertEquals("nl_NL", LocaleConverter.INSTANCE.toText(new Locale("nl", "NL")));
		assertEquals("en_GB", LocaleConverter.INSTANCE.toText(Locale.UK));
	}
	
	@Test
	public void testToTextLanguagePlusCountryPlusVariant() {
		assertEquals("es_ES_Traditional_WIN", LocaleConverter.INSTANCE.toText(new Locale("es", "ES", "Traditional_WIN")));
	}
	
	@Test
	public void testGetJavaType() {
		assertSame(Locale.class, LocaleConverter.INSTANCE.getJavaType());
	}
	
}
