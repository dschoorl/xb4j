package info.rsdev.xb4j.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NamespaceContextTest {
	
	private NamespaceContext context = null;
	
	@Before
	public void setUp() throws Exception {
		this.context = new NamespaceContext();
		this.context.registerNamespace(new QName("http://root", "root", "rt"));
	}
	
	@After
	public void tearDown() throws Exception {
		this.context.clear();
		this.context = null;
	}
	
	@Test
	public void testRegisterNamespace() {
		NamespaceContext context = new NamespaceContext();
		assertFalse(context.isRegistered("http://first/level/namespace"));
		String prefix = context.registerNamespace(new QName("http://root", "root", "rt"), "http://first/level/namespace", null);
		assertEquals("ns0", prefix);
		assertTrue(context.isRegistered("http://first/level/namespace"));
	}
	
	@Test
	public void testUnregisterNamespacesFor() {
		assertTrue(context.isRegistered("http://root"));
		context.unregisterNamespacesFor(new QName("http://root", "root", "rt"));
		assertFalse(context.isRegistered("http://root"));
	}
	
}
