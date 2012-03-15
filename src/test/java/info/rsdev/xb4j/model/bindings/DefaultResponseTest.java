package info.rsdev.xb4j.model.bindings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import info.rsdev.xb4j.test.ObjectTree;

import javax.xml.namespace.QName;

import org.junit.Test;

public class DefaultResponseTest {

	@Test
	public void testMissingOptionalElement() {
		assertTrue(DefaultResponse.MISSING_OPTIONAL_ELEMENT.isMissingOptional());
		assertTrue(DefaultResponse.MISSING_OPTIONAL_ELEMENT.isUnmarshallSuccessful());
		assertFalse(DefaultResponse.MISSING_OPTIONAL_ELEMENT.mustHandleUnmarshalledObject());
		assertNull(DefaultResponse.MISSING_OPTIONAL_ELEMENT.getUnmarshalledObject());
		assertNull(DefaultResponse.MISSING_OPTIONAL_ELEMENT.getErrorMessage());
	}
	
	@Test
	public void testMissingMandatoryElement() {
		DefaultResponse missingMandatory = DefaultResponse.newMissingElement(new QName("where-am-i"));
		assertFalse(missingMandatory.isMissingOptional());
		assertFalse(missingMandatory.isUnmarshallSuccessful());
		assertFalse(missingMandatory.mustHandleUnmarshalledObject());
		assertNull(missingMandatory.getUnmarshalledObject());
		assertNotNull(missingMandatory.getErrorMessage());
	}

	@Test
	public void testReturnUnmarshalledAndUnhandledObject() {
		DefaultResponse unhandledResponse = new DefaultResponse(new ObjectTree());
		assertFalse(unhandledResponse.isMissingOptional());
		assertTrue(unhandledResponse.isUnmarshallSuccessful());
		assertFalse(unhandledResponse.mustHandleUnmarshalledObject());
		assertNotNull(unhandledResponse.getUnmarshalledObject());
		assertNull(unhandledResponse.getErrorMessage());
	}
}
