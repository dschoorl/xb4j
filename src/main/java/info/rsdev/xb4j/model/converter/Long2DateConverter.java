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

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Convert the xml schema types dateTime, date and time to Java {@link Long} objects and back again.
 *
 * @author Dave Schoorl
 */
public class Long2DateConverter extends AbstractDateConverter {

    public static final Long2DateConverter XML_TIME = new Long2DateConverter(AbstractDateConverter.TIME_ONLY, NoValidator.INSTANCE);

    public static final Long2DateConverter XML_DATE = new Long2DateConverter(AbstractDateConverter.DATE_ONLY, NoValidator.INSTANCE);

    public static final Long2DateConverter XML_DATETIME = new Long2DateConverter(AbstractDateConverter.DATE_TIME, NoValidator.INSTANCE);

    public Long2DateConverter(String xmlType, IValidator validator) {
        super(xmlType, validator);
    }

    @Override
    protected GregorianCalendar toCalander(Object value) {
        GregorianCalendar calendar = new GregorianCalendar(getTimeZone());
        calendar.setTime(new Date((Long) value));
        return calendar;
    }

    @Override
    protected Object fromCalendar(GregorianCalendar calendar) {
        return calendar.getTime().getTime();
    }

    @Override
    public Class<?> getJavaType() {
        return Long.class;
    }

}
