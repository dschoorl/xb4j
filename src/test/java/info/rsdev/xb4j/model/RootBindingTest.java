package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;

import javax.xml.namespace.QName;

import org.junit.Test;

public class RootBindingTest {
	
	/**
	 * A Rootbinding cannot be optional
	 */
	@Test(expected=Xb4jException.class)
	public void testSetOptionalNotAllowed() {
		RootBinding root = new RootBinding(new QName("root"), Object.class);
		root.setOptional(true);
	}
	
}
