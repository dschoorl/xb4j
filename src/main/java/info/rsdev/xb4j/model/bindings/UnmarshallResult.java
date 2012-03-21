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
package info.rsdev.xb4j.model.bindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class passes back the results from the unmarshalling process from a child binding to it's calling
 * (parent) binding. It contains the resulting object (if any) and meta information about the unmarshalling
 * process.
 * 
 * @author Dave Schoorl
 */
public class UnmarshallResult implements ErrorCodes {
	
	private static final Logger logger = LoggerFactory.getLogger(UnmarshallResult.class);
	
	/**
	 * A singleton instance that indicates that the unmarshalling process encountered a missing
	 * optional xml representation
	 */
	public static final UnmarshallResult MISSING_OPTIONAL_ELEMENT = new UnmarshallResult();
	
	/**
	 * errorCode is a code that indicates the type of error. There are two codes known by this class:
	 * (1) a missing optional element and (2) a missing mandatory element.
	 */
	private Integer errorCode = null;	//null means: no error
	
	private String errorMessage = null;
	
	private IBinding bindingWithError = null;
	
	private Object unmarshalledObject = null;
	
	private boolean unmarshalledObjectIsHandled = false;
	
	/**
	 * Create a new {@link UnmarshallResult} that indicates that the unmarshalling process encountered a missing
	 * optional xml representation
	 * 
	 * @param isOptionalElementMissing 
	 */
	private UnmarshallResult() {
		this.errorCode = MISSING_OPTIONAL_ERROR;
	}
	
	/**
	 * Create a new {@link UnmarshallResult} that represents a failing unmarshall process. A missing optional element
	 * is not considered to be a failure
	 * @param msg the message that will passed down to the caller of the unmarshall process
	 */
	public UnmarshallResult(Integer errorCode, String msg, IBinding bindingWithError) {
		if (logger.isTraceEnabled()) {
			logger.trace("Error UnmarshalResult created; message=".concat(msg==null?"null":msg));
		}
		if (errorCode == null) { 
			throw new NullPointerException("errorCode cannot be null when creating an error UnmarshallResult"); 
		}
		this.errorCode = errorCode;
		this.bindingWithError = bindingWithError;
		this.errorMessage = msg;
	}
	
	/**
	 * Create a new {@link UnmarshallResult} with the result of the unmarshall process; the resulting
	 * object is not yet set in the Java context
	 * @param unmarshalledObject the result of the unmarshalling process. Could be null.
	 */
	public UnmarshallResult(Object unmarshalledObject) {
		this(unmarshalledObject, false);
	}

	/**
	 * Create a new {@link UnmarshallResult} with the result of the unmarshall process; whether the resulting
	 * object is already set in the Java context or not is determined by the indicator.
	 * @param unmarshalledObject the result of the unmarshalling process. Could be null.
	 * @param unmarshalledObjectIsHandled true if the unmarshalledObject is already set in the Java context, false otherwise
	 */
	public UnmarshallResult(Object unmarshalledObject, boolean unmarshalledObjectIsHandled) {
		this.unmarshalledObject = unmarshalledObject;
		this.unmarshalledObjectIsHandled = unmarshalledObjectIsHandled;
	}

	/**
	 * Indicator whether or not the unmarshall process was aborted due to an error
	 * @return false if the unmarshalling process was aborted with an error, true otherwise
	 */
	public boolean isUnmarshallSuccessful() {
		return (errorCode == null) || (errorCode.equals(MISSING_OPTIONAL_ERROR));
	}

	/**
	 * Can only return true when unmarshalling yielded a non-null response object and the object is not already succesfully
	 * set as a property of the Java context
	 * @return true when the caller must handle the response Object (E.g. set it in the javaContext), false otherwise 
	 */
	public boolean mustHandleUnmarshalledObject() {
		return (unmarshalledObject != null) && !unmarshalledObjectIsHandled;
	}

	/**
	 * Get the Java object that resulted from the unmarshalling process. Could be null if the binding has no representation
	 * in the Java world.
	 * @return the unmarshalled object or null
	 */
	public Object getUnmarshalledObject() {
		return unmarshalledObject;
	}

	/**
	 * If something went wrong in the unmarshalling process, this message should explain what 
	 * @return an error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public Integer getErrorCode() {
		return errorCode;
	}
	
	/**
	 * The binding context where the error occurred
	 * @return
	 */
	public IBinding getBindingWithError() {
		return bindingWithError;
	}
	
	/**
	 * Indicate that the unmarshalled object has been handled
	 * 
	 * @return this {@link UnmarshallResult}
	 */
	public UnmarshallResult setHandled() {
		this.unmarshalledObjectIsHandled = true;
		return this;
	}

	/**
	 * Indicates that the unmarshalling process did not find the expected xml representation, but that the xml representation
	 * is optional anyway.
	 * @return true if the optional xml representation was missing, false otherwise 
	 */
	public boolean isMissingOptional() {
		return MISSING_OPTIONAL_ERROR.equals(errorCode);
	}
	
	public static final UnmarshallResult newMissingElement(IBinding bindingWithError) {
		return new UnmarshallResult(MISSING_MANDATORY_ERROR, String.format("Mandatory element not encountered in xml: %s", bindingWithError.getElement()), bindingWithError);
	}

}
