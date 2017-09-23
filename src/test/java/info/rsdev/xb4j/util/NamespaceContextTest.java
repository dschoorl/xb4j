package info.rsdev.xb4j.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

public class NamespaceContextTest {

    private NamespaceContext context = null;

    @Before
    public void setup() throws Exception {
        this.context = new NamespaceContext();
    }

    @Test
    public void testRegisterNamespace() {
        assertFalse(context.isRegistered("http://first/level/namespace"));
        String prefix = context.registerNamespace(new QName("http://root", "root", "rt"), "http://first/level/namespace", null);
        assertEquals("ns0", prefix);
        assertTrue(context.isRegistered("http://first/level/namespace"));
    }

    @Test
    public void testUnregisterNamespacesFor() {
        this.context.registerNamespace(new QName("http://root", "root", "rt"));
        assertTrue(context.isRegistered("http://root"));
        context.unregisterNamespacesFor(new QName("http://root", "root", "rt"));
        assertFalse(context.isRegistered("http://root"));
    }

    @Test
    public void reusePrefixFromPreviousRegistration() {
        context.registerNamespace(new QName("http://namespace/one", "root", "one"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals(1, context.size());
        context.registerNamespace(new QName("http://namespace/one", "child"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals(1, context.size());
    }

    @Test
    public void reusePrefixFromOlderRegistration() {
        context.registerNamespace(new QName("http://namespace/one", "localOne", "one"));
        context.registerNamespace(new QName("http://namespace/two", "localTwo", "two"));
        context.registerNamespace(new QName("http://namespace/one", "localEins"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals(2, context.size());
    }

    @Test
    public void doNotChangePrefixesOfEarlierRegistrations() {
        context.registerNamespace(new QName("http://namespace/two", "localTwo", "two"));
        context.registerNamespace(new QName("http://namespace/one", "localOne", "one"));
        context.registerNamespace(new QName("http://namespace/one", "localEins", "uno"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals("two", context.getPrefix("http://namespace/two"));
        assertEquals(2, context.size());
    }

    @Test
    public void registerMultipleNamespacesOnElement() {
        QName contextElement = new QName("http://namespace/one", "localOne", "one");
        context.registerNamespace(contextElement);
        context.registerNamespace(contextElement, "http://namespace/two", "two");
        context.registerNamespace(contextElement, "http://namespace/one", "uno");
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals("two", context.getPrefix("http://namespace/two"));
        assertEquals(1, context.size());
    }

    @Test
    public void unregisterLastElementWithMultipleContextElements() {
        context.registerNamespace(new QName("http://namespace/one", "localOne", "one"));
        context.registerNamespace(new QName("http://namespace/two", "localTwo", "two"));
        context.unregisterNamespacesFor(new QName("http://namespace/two", "localTwo", "two"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertFalse(context.isRegistered("http://namespace/two"));
        assertEquals(1, context.size());
    }
}
