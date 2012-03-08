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
package info.rsdev.xb4j.model.java.converter;

import info.rsdev.xb4j.exceptions.ValidationException;
import info.rsdev.xb4j.exceptions.Xb4jException;

/**
 * 
 * @author Dave Schoorl
 */
public class IntegerConverter implements IValueConverter {
	
	/**
	 * This Singleton converter just converts Strings to and from Integers. It performs no validation on the Integer values
	 */
	public static final IntegerConverter INSTANCE = new IntegerConverter();
	
	public static final IntegerConverter POSITIVE = new IntegerConverter(new RangeValidator(1, Integer.MAX_VALUE));
	public static final IntegerConverter ZERO_OR_POSITIVE = new IntegerConverter(new RangeValidator(0, Integer.MAX_VALUE));
	public static final IntegerConverter NEGATIVE = new IntegerConverter(new RangeValidator(Integer.MIN_VALUE, -1));
	public static final IntegerConverter ZERO_OR_NEGATIVE = new IntegerConverter(new RangeValidator(Integer.MIN_VALUE, 0));
	
	private IValidator validator = NoValidator.INSTANCE;
	
	private IntegerConverter() {}
	
	/**
	 * Create a new {@link IntegerConverter} that performs validation on the Integer. If you don't need validation, please use
	 * the Singleton instance {@link #INSTANCE}
	 * @param validator the validation strategy to use
	 */
	public IntegerConverter(IValidator validator) {
		if (validator == null) {
			throw new NullPointerException("IValidator cannot be null");
		}
		this.validator = validator;
	}
	
	@Override
	public Object toObject(String value) {
		if (value == null) { return null; }
		return validator.isValid(Integer.valueOf(value));
	}
	
	@Override
	public String toText(Object value) {
		if (value == null) { return null; }
		if (!(value instanceof Integer)) {
			throw new Xb4jException(String.format("Expected a %s, but was a %s", Integer.class.getName(), 
					value.getClass().getName()));
		}
		return validator.isValid((Integer)value).toString();
	}
	
	@Override
	public Class<?> getJavaType() {
		return Integer.class;
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