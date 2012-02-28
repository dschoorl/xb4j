package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import info.rsdev.xb4j.test.ObjectA;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Ignore;
import org.junit.Test;

public class ComplexTypeBindingTest {
	
    @Test @Ignore
    public void testMarshallComplexType() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new Object();
        ComplexTypeBinding complexType = new ComplexTypeBinding("typeO", null);
        complexType.add(new ValueBinding(new QName("name")), "name");
        
        RootBinding root = new RootBinding(new QName("root"), ObjectA.class);	//has element, but class comes from child
        root.add(new ComplexTypeReference("typeO", null), "");
        
        BindingModel model = new BindingModel();
        model.register(complexType);
        model.register(root);
        
        model.toXml(stream, instance);
        String result = stream.toString();
        assertEquals("<root><name>test</name></root>", result);
    }
    
}
