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

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class passes back the results from the unmarshalling process from a child binding to it's calling
 * (parent) binding. It contains the resulting object (if any) and meta information about the unmarshalling
 * process.
 * 
 * @author Dave Schoorl
 */
public class UnmarshallResult {
	
	private static final Logger logger = LoggerFactory.getLogger(UnmarshallResult.class);
	
	/**
	 * A singleton instance that indicates that the unmarshalling process encountered a missing
	 * optional xml representation
	 */
	public static final UnmarshallResult MISSING_OPTIONAL_ELEMENT = new UnmarshallResult();
	
	private String errorMessage = null;
	
	private Object unmarshalledObject = null;
	
	private boolean unmarshalledObjectIsHandled = false;
	
	private boolean isMissingOptional = false;
	
	/**
	 * Create a new {@link UnmarshallResult} that indicates that the unmarshalling process encountered a missing
	 * optional xml representation
	 * 
	 * @param isOptionalElementMissing 
	 */
	private UnmarshallResult() {
		this.isMissingOptional = true;
	}
	
	/**
	 * Create a new {@link UnmarshallResult} that represents a failing unmarshall process. A missing optional element
	 * is not considered to be a failure
	 * @param msg the message that will passed down to the caller of the unmarshall process
	 */
	public UnmarshallResult(String msg) {
		if (logger.isTraceEnabled()) {
			logger.trace("Error UnmarshalResult created; message=".concat(msg==null?"null":msg));
		}
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
		return errorMessage == null;
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
		return isMissingOptional;
	}
	
	public static final UnmarshallResult newMissingElement(QName element) {
		return new UnmarshallResult(String.format("Mandatory element not encountered in xml: %s", element));
	}

}
