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
 * 
 * @author Dave Schoorl
 */
public class IntegerConverter implements IValueConverter {
	
	private static final int MAX_LENGTH = 11;	//Integer.MAX_VALUE / Integer.MIN_VALUE can have 10 digits plus a sign + / -
	
	/**
	 * This Singleton converter just converts Strings to and from Integers. It performs no validation on the Integer values
	 */
	public static final IntegerConverter INSTANCE = new IntegerConverter();
	
	public static final IntegerConverter POSITIVE = new IntegerConverter(new RangeValidator(1, Integer.MAX_VALUE));
	public static final IntegerConverter ZERO_OR_POSITIVE = new IntegerConverter(new RangeValidator(0, Integer.MAX_VALUE));
	public static final IntegerConverter NEGATIVE = new IntegerConverter(new RangeValidator(Integer.MIN_VALUE, -1));
	public static final IntegerConverter ZERO_OR_NEGATIVE = new IntegerConverter(new RangeValidator(Integer.MIN_VALUE, 0));
	
	private IValidator validator = NoValidator.INSTANCE;
	
	private int minLength = -1;	//pad with zeros when value is shorter than minLength; maxLength can be set through RangeValidator mechanism
	
	private IntegerConverter() {}
	
	/**
	 * Create a new {@link IntegerConverter} that performs validation on the Integer. If you don't need validation, please use
	 * the Singleton instance {@link #INSTANCE}
	 * @param validator the validation strategy to use
	 */
	public IntegerConverter(IValidator validator) {
		this(validator, 0);
	}
	
	/**
	 * Create a new {@link IntegerConverter} with a specific {@link IValidator} and a minimum required length. When converting 
	 * xml to Object, a {@link ValidationException} is thrown when the length is shorter than minLength. When converting an
	 * {@link Integer} to xml, the number will be left padded with zeros to reach the minLength size.
	 * @param validator
	 * @param minLength
	 */
	public IntegerConverter(IValidator validator, int minLength) {
		setValidator(validator);
		setMinLength(minLength);
	}
	
	@Override
	public Integer toObject(JavaContext javaContext, String value) {
		if (value == null) { return null; }
		if ((minLength > 1) && (value.length() < minLength)) {
			throw new ValidationException(String.format("Value %s is too short: it should have at least %d characters, and not %d",
					value, minLength, value.length()));
		}
		return validator.isValid(Integer.valueOf(value));
	}
	
	@Override
	public String toText(JavaContext javaContext, Object value) {
		if (value == null) { return null; }
		if (!(value instanceof Integer)) {
			throw new Xb4jException(String.format("Expected a %s, but was a %s", Integer.class.getName(), 
					value.getClass().getName()));
		}
		Integer intValue = validator.isValid((Integer)value);
		return String.format((minLength<1?"%d":"%0"+minLength+"d"), intValue);
	}
	
	@Override
	public Class<?> getJavaType() {
		return Integer.class;
	}
	
	private void setMinLength(int minLength) {
		if (minLength < 0) {
			throw new Xb4jException("Minimum length for an Integer cannot be negative: "+minLength);
		} else if (minLength > MAX_LENGTH) {
			throw new Xb4jException("Minimum length for an Integer cannot be larger than "+MAX_LENGTH+": "+minLength);
		}
		this.minLength = minLength;
	}
	
	private void setValidator(IValidator validator) {
		if (validator == null) {
			throw new NullPointerException("IValidator cannot be null");
		}
		this.validator = validator;
	}
	
	
	public static final class RangeValidator implements IValidator {
		
		private final int lowerBound;
		private final int upperBound;
		
		public RangeValidator(int lowerBound, int upperBound) {
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
			int value = (Integer)instance;
			if ((value < lowerBound) || (value > upperBound)) {
				throw new ValidationException(String.format("Integer must be between %d and %d: %d", lowerBound, upperBound, instance));
			}
			return instance;
		}
		
	}
}
