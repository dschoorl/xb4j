package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import info.rsdev.xb4j.test.ObjectA;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Test;

public class ComplexTypeBindingTest {
	
    @Test 
    public void testMarshallComplexType() {
        ComplexTypeBinding complexType = new ComplexTypeBinding("typeO", null);
        complexType.add(new ValueBinding(new QName("name")), "name");
        
        RootBinding root = new RootBinding(new QName("root"), ObjectA.class);	//has element, but class comes from child
        root.add(new ComplexTypeReference("typeO", null), "");
        
        //bind complextype to other xml element (same javaclass) -- this is currently not supported by BindingModel
//        RootBinding hoofdmap = new RootBinding(new QName("hoofdmap"), ObjectA.class);   //has element, but class comes from child
//        hoofdmap.add(new ComplexTypeReference("typeO", null), "");
        
        BindingModel model = new BindingModel();
        model.register(complexType);
        model.register(root);
//        model.register(hoofdmap);
        
        //marshall root
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new ObjectA("test");
        model.toXml(stream, instance);
        String result = stream.toString();
        assertEquals("<root><name>test</name></root>", result);
        
        //marshall hoofdmap
        assertEquals("<hoofdmap><name>test</name></hoofdmap>", result);
    }
    
}
