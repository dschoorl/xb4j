package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;
import info.rsdev.xb4j.util.XmlStreamFactory;

import java.io.ByteArrayOutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

public class BindingModelTest {

    private BindingModel model = null;
    
    @Before
    public void setup() {
        model = new BindingModel();
        Root binding = new Root(new QName("http://1", "a", "up"), ObjectA.class);
        binding.setChild(new SimpleType(new QName("name")), "name");
        model.register(binding);
        
        binding = new Root(new QName("http://2", "a", "lo"), ObjectA.class);
        binding.setChild(new SimpleType(new QName("eman")), "name");
        model.register(binding);
        
        model.register(new Root(new QName("B"), ObjectB.class));
    }
    
    @Test
    public void testRegisterJavatypeTwice() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.toXml(XmlStreamFactory.makeWriter(out), new ObjectA("uppercase A"), "http://1");
        assertEquals("<up:a xmlns:up=\"http://1\"><name>uppercase A</name></up:a>", out.toString());
        
        out = new ByteArrayOutputStream();
        model.toXml(XmlStreamFactory.makeWriter(out), new ObjectA("lowercase a"), "http://2");
        assertEquals("<lo:a xmlns:lo=\"http://2\"><eman>lowercase a</eman></lo:a>", out.toString());
    }
    
    @Test(expected=Xb4jException.class)
    public void testGetBindingMultipleNoSpecifier() {
        model.getBinding(ObjectA.class);
    }
    
    @Test
    public void testGetBindingUniqueWithSpecifier() {
        Root expected = new Root(new QName("B"), ObjectB.class);
        assertNotNull(model.getBinding(ObjectB.class, XMLConstants.NULL_NS_URI));
        assertEquals(expected, model.getBinding(ObjectB.class, XMLConstants.NULL_NS_URI));
    }
    
    @Test
    public void testGetBindingUniqueWrongSpecifier() {
        assertNull(model.getBinding(ObjectB.class, "http://1"));    //ObjectB is registered under different namespace
    }
    
    @Test
    public void testGetBindingUniqueNoSpecifier() {
        Root expected = new Root(new QName("B"), ObjectB.class);
        assertNotNull(model.getBinding(ObjectB.class));
        assertEquals(expected, model.getBinding(ObjectB.class));
    }
    
    @Test(expected=Xb4jException.class)
    public void testBindJavaclassTwiceToDifferentLocalParts() throws Exception {
    	BindingModel aModel = new BindingModel();
    	aModel.register(new Root(new QName("http://bkwi.nl/1", "versie1"), ObjectA.class));
    	aModel.register(new Root(new QName("http://bkwi.nl/2", "versie2"), ObjectA.class));	//same class must use same localPart, if not: Xb4jException is thrown
    }
    
}
