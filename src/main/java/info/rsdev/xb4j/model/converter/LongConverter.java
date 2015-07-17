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

/**
 * Converts between xml text and Java {@link Long} type
 * 
 * @author Dave Schoorl
 */
public class LongConverter implements IValueConverter {
	
	/**
	 * This Singleton converter just converts Strings to and from Integers. It performs no validation on the Integer values
	 */
	public static final LongConverter INSTANCE = new LongConverter();
	
	public static final LongConverter POSITIVE = new LongConverter(new RangeValidator(1, Long.MAX_VALUE));
	public static final LongConverter ZERO_OR_POSITIVE = new LongConverter(new RangeValidator(0, Long.MAX_VALUE));
	public static final LongConverter NEGATIVE = new LongConverter(new RangeValidator(Long.MIN_VALUE, -1));
	public static final LongConverter ZERO_OR_NEGATIVE = new LongConverter(new RangeValidator(Long.MIN_VALUE, 0));
	
	private IValidator validator = NoValidator.INSTANCE;
	
	private LongConverter() {}
	
	/**
	 * Create a new {@link LongConverter} that performs validation on the Integer. If you don't need validation, please use
	 * the Singleton instance {@link #INSTANCE}
	 * @param validator the validation strategy to use
	 */
	public LongConverter(IValidator validator) {
		if (validator == null) {
			throw new NullPointerException("IValidator cannot be null");
		}
		this.validator = validator;
	}
	
	@Override
	public Long toObject(JavaContext javaContext, String value) {
		if ((value == null) || value.isEmpty()) { return null; }
		try {
			return validator.isValid(Long.valueOf(value));
		} catch (NumberFormatException e) {
			throw new ValidationException(String.format("Cannot convert to a Long: '%s'", value), e);
		}
	}
	
	@Override
	public String toText(JavaContext javaContext, Object value) {
		if (value == null) { return null; }
		if (!(value instanceof Long)) {
			throw new Xb4jException(String.format("Expected a %s, but was a %s", Long.class.getName(), 
					value.getClass().getName()));
		}
		return validator.isValid((Long)value).toString();
	}
	
	@Override
	public Class<?> getJavaType() {
		return Long.class;
	}
	
	
	public static final class RangeValidator implements IValidator {
		
		private final long lowerBound;
		private final long upperBound;
		
		public RangeValidator(long lowerBound, long upperBound) {
			if (lowerBound > upperBound) {
				this.upperBound = lowerBound;
				this.lowerBound = upperBound;
			} else {
				this.upperBound = upperBound;
				this.lowerBound = lowerBound;
			}
		}

		@Override
		public <T> T isValid(T instance) throws ValidationException {
			long value = (Long)instance;
			if ((value < lowerBound) || (value > upperBound)) {
				throw new ValidationException(String.format("Long must be between %d and %d: %d", lowerBound, upperBound, instance));
			}
			return instance;
		}
		
	}
}
