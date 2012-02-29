package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

public class ComplexTypeBindingTest {
    
    private BindingModel model = null;
    
    @Before
    public void setup() {
        ComplexTypeBinding complexType = new ComplexTypeBinding("typeO", null);
        complexType.setChild(new SimpleTypeBinding(new QName("name")), "name");
        
        RootBinding root = new RootBinding(new QName("root"), ObjectA.class);   //has element, but class comes from child
        root.add(new ComplexTypeReference("typeO", null));
        
        //bind complextype to other xml element (same javaclass) -- this is currently not supported by BindingModel
        RootBinding hoofdmap = new RootBinding(new QName("directory"), ObjectTree.class);   //has element, but class comes from child
        hoofdmap.add(new ComplexTypeReference("typeO", null), "myObject");
        
        model = new BindingModel();
        model.register(complexType);
        model.register(root);
        model.register(hoofdmap);
    }
	
    @Test 
    public void testMarshallComplexType() {
        //marshall root
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Object instance = new ObjectA("test");
        model.toXml(stream, instance);
        String result = stream.toString();
        assertEquals("<root><name>test</name></root>", result);
        
        //marshall hoofdmap
        stream = new ByteArrayOutputStream();
        instance = new ObjectTree().setMyObject(new ObjectA("test"));
        model.toXml(stream, instance);
        result = stream.toString();
        assertEquals("<directory><name>test</name></directory>", result);
    }
    
    @Test
    public void testUnmarshallComplexType() {
        //Unmarshall ObjectA
        ByteArrayInputStream stream = new ByteArrayInputStream("<root><name>test</name></root>".getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
        assertEquals("test", ((ObjectA)instance).getName());
        
        //unmarshall ObjectTree
        stream = new ByteArrayInputStream("<directory><name>test</name></directory>".getBytes());
        instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree)instance;
        assertNotNull(tree.getMyObject());
        assertEquals("test", tree.getMyObject().getName());
    }
    
}
