package info.rsdev.xb4j.model;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Test;

public class ComplexTypeBindingTest {
	
//    @Test
    public void testMarshallComplexType() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        BindingModel model = new BindingModel();
        SequenceBinding binding = new SequenceBinding(new QName("root"), Object.class);	//has element, but class comes from child
//        binding.add(new ComplexTypeBinding(), "");
        model.bind(binding);
        
        model.toXml(stream, instance);
        assertEquals("<root/>", stream.toString());
    }
    
}
