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

import info.rsdev.xb4j.exceptions.ValidationException;
import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.JavaContext;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 * Abstract class for converting the xml schema types dateTime, date and time to Java objects and back again. Subclasses must
 * implement the abstract methods for the specific Java type, E.g. {@link Date} or {@link Calendar} to convert to.
 *
 * @author Dave Schoorl
 */
public abstract class AbstractDateConverter implements IValueConverter {

    public static final String DATE_ONLY = "xs:date";
    public static final String TIME_ONLY = "xs:time";
    public static final String DATE_TIME = "xs:dateTime";
    private static final List<String> SUPPORTED_XML_DATE_TYPES = Arrays.asList(DATE_ONLY, TIME_ONLY, DATE_TIME);

    /**
     * For datetime conversions, we do not rely on the <code>DatatypeConverter</code> implementation of JAXB, because then the output
     * will be formatted in the default timezone, while we want to control the timezone when running automated tests. Instead, we
     * create our own copy of {@link DatatypeFactory}. The conversion code however, is copied shamelessly from Sun's implementation
     * of <code>javax.xml.bind.DatatypeConverter</code>.
     */
    private static final DatatypeFactory DATA_TYPE_FACTORY;

    static {
        try {
            DATA_TYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new Error(e);
        }
    }

    private final IValidator validator;

    private final int xmlType;

    /**
     * The timezone that will be used for the textual representation when converting from Java to xml.
     */
    private TimeZone timeZone = TimeZone.getDefault();

    public AbstractDateConverter(String xmlType, IValidator validator) {
        if (validator == null) {
            validator = NoValidator.INSTANCE;
        }
        if (!SUPPORTED_XML_DATE_TYPES.contains(xmlType)) {
            throw new Xb4jException(String.format("Not a valid xmlType %s. Choose one of: %s", xmlType, SUPPORTED_XML_DATE_TYPES));
        }
        this.xmlType = SUPPORTED_XML_DATE_TYPES.indexOf(xmlType);
        this.validator = validator;
    }

    @Override
    public Object toObject(JavaContext javaContext, String value) {
        if ((value == null) || value.isEmpty()) {
            return null;
        }
        try {
            GregorianCalendar aMoment = DATA_TYPE_FACTORY.newXMLGregorianCalendar(value.trim()).toGregorianCalendar(timeZone, null, null);
            return fromCalendar(validator.isValid(aMoment));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid date format representation", e);
        }
    }

    @Override
    public String toText(JavaContext javaContext, Object value) {
        if (value == null) {
            return null;
        }
        if (!getJavaType().isAssignableFrom(value.getClass())) {
            throw new Xb4jException(String.format("Expected a %s, but was a %s", getJavaType().getName(),
                    value.getClass().getName()));
        }
        GregorianCalendar aMoment = validator.isValid(toCalander(value));
        switch (xmlType) {
            case 0:
                //  From DatatypeConverter.printDate: "%Y-%M-%D%z"
                return DateTimeFormatter.ISO_DATE.format(aMoment.toZonedDateTime().withFixedOffsetZone());
            case 1:
                // From DatatypeConverter.printTime: "%h:%m:%s%z"
                return DateTimeFormatter.ISO_TIME.format(aMoment.toZonedDateTime().withFixedOffsetZone());
            case 2:
                // From DatatypeConverter.printDateTime: "%Y-%M-%DT%h:%m:%s%z"
                return DateTimeFormatter.ISO_DATE_TIME.format(aMoment.toZonedDateTime().withFixedOffsetZone());
        }
        throw new Xb4jException("Xml date type index not recognized: " + xmlType);
    }

    /**
     * The timezone is protected, so that test classes can set it to a controlled value when running tests.
     *
     * @param timeZone
     */
    protected void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    protected TimeZone getTimeZone() {
        return this.timeZone;
    }

    protected abstract GregorianCalendar toCalander(Object value);

    protected abstract Object fromCalendar(GregorianCalendar calendar);

}
