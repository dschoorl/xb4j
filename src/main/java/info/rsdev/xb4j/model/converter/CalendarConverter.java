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

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Convert the xml schema types dateTime, date and time to Java {@link GregorianCalendar} objects and back again.
 *
 * @author Dave Schoorl
 */
public class CalendarConverter extends AbstractDateConverter {

    public static final CalendarConverter XML_TIME = new CalendarConverter(AbstractDateConverter.TIME_ONLY, NoValidator.INSTANCE);

    public static final CalendarConverter XML_DATE = new CalendarConverter(AbstractDateConverter.DATE_ONLY, NoValidator.INSTANCE);

    public static final CalendarConverter XML_DATETIME = new CalendarConverter(AbstractDateConverter.DATE_TIME, NoValidator.INSTANCE);

    public CalendarConverter(String xmlType, IValidator validator) {
        super(xmlType, validator);
    }

    @Override
    protected GregorianCalendar toCalander(Object value) {
        return (GregorianCalendar) value;
    }

    @Override
    protected Object fromCalendar(GregorianCalendar calendar) {
        return calendar;
    }

    @Override
    public Class<?> getJavaType() {
        return Calendar.class;
    }

}
