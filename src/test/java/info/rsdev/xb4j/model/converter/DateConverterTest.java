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

import info.rsdev.xb4j.model.java.JavaContext;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 * Test the conversion support for data, time and dateTime schema data types to Java Date instances. The same xml values must be
 * easily transfered to other date types, such as Calendar.
 *
 * Note: these tests should pass, no matter what timezone is set on the machine running the tests
 *
 *
 * @author dschoorl
 */
public class DateConverterTest {

    private JavaContext mockContext = null;

    @Before
    public void setup() {
        this.mockContext = mock(JavaContext.class);
    }

    @After
    public void teardown() {
        verifyZeroInteractions(mockContext);    //JavaContext is not used by this converter
    }

    /**
     * Test the happy flows in converting DateTime schema data types into Java Date objects
     * @throws java.lang.Exception
     */
    @Test
    public void testDateTimeToText() throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone("GMT+05:00");
        Calendar march17 = new GregorianCalendar(2012, 2, 17, 12, 34, 56);
        march17.setTimeZone(timeZone);
        march17.add(Calendar.MILLISECOND, 789);
        DateConverter.XML_DATETIME.setTimeZone(timeZone);
        String result = DateConverter.XML_DATETIME.toText(mockContext, march17.getTime());
        assertEquals("2012-03-17T12:34:56.789+05:00", result);
    }

    @Test
    public void testToObject() {
        Date result = (Date) DateConverter.XML_DATETIME.toObject(mockContext, "2012-03-14T08:23:00+01:00");
        assertNotNull(result);
    }

}
