package info.rsdev.xb4j.model.bindings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import info.rsdev.xb4j.test.ObjectTree;

import javax.xml.namespace.QName;

import org.junit.Test;

public class UnmarshallResultTest {

	@Test
	public void testMissingOptionalElement() {
		assertTrue(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.isMissingOptional());
		assertTrue(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.isUnmarshallSuccessful());
		assertFalse(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.mustHandleUnmarshalledObject());
		assertNull(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.getUnmarshalledObject());
		assertNull(UnmarshallResult.MISSING_OPTIONAL_ELEMENT.getErrorMessage());
	}
	
	@Test
	public void testMissingMandatoryElement() {
		UnmarshallResult missingMandatory = UnmarshallResult.newMissingElement(new QName("where-am-i"));
		assertFalse(missingMandatory.isMissingOptional());
		assertFalse(missingMandatory.isUnmarshallSuccessful());
		assertFalse(missingMandatory.mustHandleUnmarshalledObject());
		assertNull(missingMandatory.getUnmarshalledObject());
		assertNotNull(missingMandatory.getErrorMessage());
	}

	@Test
	public void testReturnUnmarshalledAndUnhandledObject() {
		UnmarshallResult unhandledResponse = new UnmarshallResult(new ObjectTree());
		assertFalse(unhandledResponse.isMissingOptional());
		assertTrue(unhandledResponse.isUnmarshallSuccessful());
		assertTrue(unhandledResponse.mustHandleUnmarshalledObject());
		assertNotNull(unhandledResponse.getUnmarshalledObject());
		assertNull(unhandledResponse.getErrorMessage());
	}
	
	@Test
	public void testReturnUnmarshalledAndHandledObject() {
		UnmarshallResult unhandledResponse = new UnmarshallResult(new ObjectTree(), true);
		assertFalse(unhandledResponse.isMissingOptional());
		assertTrue(unhandledResponse.isUnmarshallSuccessful());
		assertFalse(unhandledResponse.mustHandleUnmarshalledObject());
		assertNotNull(unhandledResponse.getUnmarshalledObject());
		assertNull(unhandledResponse.getErrorMessage());
	}
}
