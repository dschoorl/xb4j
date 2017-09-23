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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;
import info.rsdev.xb4j.model.java.JavaContext;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LocaleConverterTest {

    private JavaContext mockContext = null;

    @Before
    public void setup() {
        this.mockContext = mock(JavaContext.class);
    }

    @After
    public void teardown() {
        verifyZeroInteractions(mockContext);    //JavaContext is not used by this converter
    }

    @Test
    public void testToObjectLanguageOnly() {
        assertNull(LocaleConverter.INSTANCE.toObject(mockContext, null));
        assertEquals(new Locale("nl"), LocaleConverter.INSTANCE.toObject(mockContext, "nl"));
        assertEquals(Locale.ENGLISH, LocaleConverter.INSTANCE.toObject(mockContext, "en"));
    }

    @Test
    public void testToObjectLanguagePlusCountry() {
        assertEquals(Locale.UK, LocaleConverter.INSTANCE.toObject(mockContext, "en_GB"));
        assertEquals(new Locale("nl", "NL"), LocaleConverter.INSTANCE.toObject(mockContext, "nl_NL"));
    }

    @Test
    public void testToObjectLanguagePlusCountryPlusVariant() {
        assertEquals(new Locale("es", "ES", "Traditional_WIN"), LocaleConverter.INSTANCE.toObject(mockContext, "es_ES_Traditional_WIN"));
    }

    @Test
    public void testToTextLanguageOnly() {
        assertNull(LocaleConverter.INSTANCE.toText(mockContext, null));
        assertEquals("nl", LocaleConverter.INSTANCE.toText(mockContext, new Locale("nl")));
        assertEquals("en", LocaleConverter.INSTANCE.toText(mockContext, Locale.ENGLISH));
        assertEquals("en", LocaleConverter.INSTANCE.toText(mockContext, new Locale("en", "", "")));	//omit unneccesary underscores
    }

    @Test
    public void testToTextLanguagePlusCountry() {
        assertEquals("nl_NL", LocaleConverter.INSTANCE.toText(mockContext, new Locale("nl", "NL")));
        assertEquals("en_GB", LocaleConverter.INSTANCE.toText(mockContext, Locale.UK));
    }

    @Test
    public void testToTextLanguagePlusCountryPlusVariant() {
        assertEquals("es_ES_Traditional_WIN", LocaleConverter.INSTANCE.toText(mockContext, new Locale("es", "ES", "Traditional_WIN")));
    }

    @Test
    public void testGetJavaType() {
        assertSame(Locale.class, LocaleConverter.INSTANCE.getJavaType());
    }

}
