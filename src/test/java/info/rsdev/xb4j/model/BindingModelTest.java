package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

public class BindingModelTest {

    private BindingModel model = null;
    
    @Before
    public void setup() {
        model = new BindingModel();
        RootBinding binding = new RootBinding(new QName("A"), ObjectA.class);
        binding.setChild(new SimpleTypeBinding(new QName("name")), "name");
        model.register(binding);
        
        binding = new RootBinding(new QName("a"), ObjectA.class);
        binding.setChild(new SimpleTypeBinding(new QName("eman")), "name");
        model.register(binding.copy(new QName("a")));
        
        model.register(new RootBinding(new QName("B"), ObjectB.class));
    }
    
    @Test
    public void testRegisterJavatypeTwice() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.toXml(out, new ObjectA("uppercase A"), new QName("A"));
        assertEquals("<A><name>uppercase A</name></A>", out.toString());
        
        out = new ByteArrayOutputStream();
        model.toXml(out, new ObjectA("lowercase a"), new QName("a"));
        assertEquals("<a><eman>lowercase a</eman></a>", out.toString());
    }
    
    @Test(expected=Xb4jException.class)
    public void testGetBindingMultipleNoSpecifier() {
        model.getBinding(ObjectA.class);
    }
    
    @Test
    public void testGetBindingUniqueWithSpecifier() {
        RootBinding expected = new RootBinding(new QName("B"), ObjectB.class);
        assertNotNull(model.getBinding(ObjectB.class, new QName("B")));
        assertEquals(expected, model.getBinding(ObjectB.class, new QName("B")));
    }
    
    @Test
    public void testGetBindingUniqueWrongSpecifier() {
        assertNull(model.getBinding(ObjectB.class, new QName("C")));    //ObjectB is registered under different QName
    }
    
    @Test
    public void testGetBindingUniqueNoSpecifier() {
        RootBinding expected = new RootBinding(new QName("B"), ObjectB.class);
        assertNotNull(model.getBinding(ObjectB.class));
        assertEquals(expected, model.getBinding(ObjectB.class));
    }
    
}
