package info.rsdev.xb4j.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NamespaceContextTest {

    private NamespaceContext context = null;

    @BeforeEach
    public void setup() throws Exception {
        this.context = new NamespaceContext();
    }

    @Test
    void testRegisterNamespace() {
        assertFalse(context.isRegistered("http://first/level/namespace"));
        String prefix = context.registerNamespace(new QName("http://root", "root", "rt"), "http://first/level/namespace", null);
        assertEquals("ns0", prefix);
        assertTrue(context.isRegistered("http://first/level/namespace"));
    }

    @Test
    void testUnregisterNamespacesFor() {
        this.context.registerNamespace(new QName("http://root", "root", "rt"));
        assertTrue(context.isRegistered("http://root"));
        context.unregisterNamespacesFor(new QName("http://root", "root", "rt"));
        assertFalse(context.isRegistered("http://root"));
    }

    @Test
    void reusePrefixFromPreviousRegistration() {
        context.registerNamespace(new QName("http://namespace/one", "root", "one"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals(1, context.size());
        context.registerNamespace(new QName("http://namespace/one", "child"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals(1, context.size());
    }

    @Test
    void reusePrefixFromOlderRegistration() {
        context.registerNamespace(new QName("http://namespace/one", "localOne", "one"));
        context.registerNamespace(new QName("http://namespace/two", "localTwo", "two"));
        context.registerNamespace(new QName("http://namespace/one", "localEins"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals(2, context.size());
    }

    @Test
    void doNotChangePrefixesOfEarlierRegistrations() {
        context.registerNamespace(new QName("http://namespace/two", "localTwo", "two"));
        context.registerNamespace(new QName("http://namespace/one", "localOne", "one"));
        context.registerNamespace(new QName("http://namespace/one", "localEins", "uno"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals("two", context.getPrefix("http://namespace/two"));
        assertEquals(2, context.size());
    }

    @Test
    void registerMultipleNamespacesOnElement() {
        QName contextElement = new QName("http://namespace/one", "localOne", "one");
        context.registerNamespace(contextElement);
        context.registerNamespace(contextElement, "http://namespace/two", "two");
        context.registerNamespace(contextElement, "http://namespace/one", "uno");
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertEquals("two", context.getPrefix("http://namespace/two"));
        assertEquals(1, context.size());
    }

    @Test
    void unregisterLastElementWithMultipleContextElements() {
        context.registerNamespace(new QName("http://namespace/one", "localOne", "one"));
        context.registerNamespace(new QName("http://namespace/two", "localTwo", "two"));
        context.unregisterNamespacesFor(new QName("http://namespace/two", "localTwo", "two"));
        assertEquals("one", context.getPrefix("http://namespace/one"));
        assertFalse(context.isRegistered("http://namespace/two"));
        assertEquals(1, context.size());
    }
    
    @Test
    void doNotMixupRegisteredNamespaceOnSiblingElement() {
        String prefix1 = context.registerNamespace(new QName("http://namespace/one", "localOne"));
        context.unregisterNamespacesFor(new QName("http://namespace/one", "localOne"));
        String prefix2 = context.registerNamespace(new QName("http://namespace/one", "localTwo"));
        assertThat(prefix1, not(equalTo(prefix2)));
    }
}
