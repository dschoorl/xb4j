package info.rsdev.xb4j.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;
import info.rsdev.xb4j.util.XmlStreamFactory;

class BindingModelTest {

    private BindingModel model = null;

    private static final QName UP_QNAME = new QName("http://1", "a", "up");
    private static final QName LO_QNAME = new QName("http://2", "a", "lo");

    @BeforeEach
    public void setup() {
        model = new BindingModel();
        Root binding = new Root(UP_QNAME, ObjectA.class);
        binding.setChild(new SimpleType(new QName("name"), false), "name");
        model.registerRoot(binding);

        binding = new Root(LO_QNAME, ObjectA.class);
        binding.setChild(new SimpleType(new QName("eman"), false), "name");
        model.registerRoot(binding);

        model.registerRoot(new Root(new QName("B"), ObjectB.class));
    }

    @Test
    void testRegisterJavatypeTwice() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.getXmlStreamer(ObjectA.class, UP_QNAME).toXml(XmlStreamFactory.makeWriter(out), new ObjectA("uppercase A"));
        assertEquals("<up:a xmlns:up=\"http://1\"><name>uppercase A</name></up:a>", out.toString());

        out = new ByteArrayOutputStream();
        model.getXmlStreamer(ObjectA.class, LO_QNAME).toXml(XmlStreamFactory.makeWriter(out), new ObjectA("lowercase a"));
        assertEquals("<lo:a xmlns:lo=\"http://2\"><eman>lowercase a</eman></lo:a>", out.toString());
    }

    @Test
    void testGetBindingMultipleNoSpecifier() {
        assertThrows(Xb4jException.class, () -> model.getBinding(ObjectA.class));
    }

    @Test
    void testGetBindingUniqueWithSpecifier() {
        Root expected = new Root(new QName("B"), ObjectB.class);
        assertNotNull(model.getBinding(ObjectB.class, null));
        assertEquals(expected, model.getBinding(ObjectB.class, null));
    }

    @Test
    void testGetBindingUniqueWrongSpecifier() {
        assertNull(model.getBinding(ObjectB.class, UP_QNAME));    //ObjectB is registered under different namespace
    }

    @Test
    void testGetBindingUniqueNoSpecifier() {
        Root expected = new Root(new QName("B"), ObjectB.class);
        assertNotNull(model.getBinding(ObjectB.class));
        assertEquals(expected, model.getBinding(ObjectB.class));
    }

    @Test
    void testBindJavaclassTwiceToSameQName() throws Exception {
        BindingModel aModel = new BindingModel();
        aModel.registerRoot(new Root(new QName("http://bkwi.nl/1", "eenVersie"), ObjectA.class));
        assertThrows(Xb4jException.class, () -> aModel.registerRoot(new Root(new QName("http://bkwi.nl/1", "eenVersie"), ObjectA.class)));	//same class must use different QName, if not: Xb4jException is thrown
    }

    @Test
    void testBindJavaclassTwiceWithDifferentQName() throws Exception {
        BindingModel aModel = new BindingModel();
        Root root1 = aModel.registerRoot(new Root(new QName("http://bkwi.nl/1", "eenVersie"), ObjectA.class));
        Root root2 = aModel.registerRoot(new Root(new QName("http://bkwi.nl/2", "eenVersie"), ObjectA.class));
        assertNotSame(root1, root2);
    }

}
