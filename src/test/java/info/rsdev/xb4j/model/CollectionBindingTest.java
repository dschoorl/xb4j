package info.rsdev.xb4j.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.test.ObjectTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.junit.Test;

/**
 * 
 * @author Dave Schoorl
 */
public class CollectionBindingTest {
	
	@Test
	public void testMarshallValueCollectionNoContainerElement() {
		//fixture
        RootBinding root = new RootBinding(new QName("root"), ObjectTree.class);
        CollectionBinding collection = (CollectionBinding)root.setChild(new CollectionBinding(ArrayList.class), "messages");
        collection.setItem(new SimpleTypeBinding(new QName("detail")));
        BindingModel model = new BindingModel().register(root);
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("bericht1");
        instance.addMessage("bericht2");
        
        model.toXml(stream, instance);
        String result = stream.toString();
        assertEquals("<root><detail>bericht1</detail><detail>bericht2</detail></root>", result);
	}
	
    @Test
    public void testMarshallValueCollectionWithContainerElement() {
        //fixture
        RootBinding root = new RootBinding(new QName("root"), ObjectTree.class);
        CollectionBinding collection = (CollectionBinding)root.setChild(new CollectionBinding(new QName("collection"), ArrayList.class), "messages");
        collection.setItem(new SimpleTypeBinding(new QName("detail")));
        BindingModel model = new BindingModel().register(root);
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectTree instance = new ObjectTree();
        instance.addMessage("bericht1");
        instance.addMessage("bericht2");
        
        model.toXml(stream, instance);
        String result = stream.toString();
        assertEquals("<root><collection><detail>bericht1</detail><detail>bericht2</detail></collection></root>", result);
    }
    
	@Test
	public void testUnmarshallValueCollectionNoContainerElement() {
		//fixture
        RootBinding root = new RootBinding(new QName("root"), ObjectTree.class);
        CollectionBinding collection = (CollectionBinding)root.setChild(new CollectionBinding(ArrayList.class), "messages");
        collection.setItem(new SimpleTypeBinding(new QName("detail")));
        BindingModel model = new BindingModel().register(root);
        
        ByteArrayInputStream stream = new ByteArrayInputStream("<root><detail>bericht1</detail><detail>bericht2</detail></root>".getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree)instance;
        assertNotNull(tree.getMessages());
        assertEquals(2, tree.getMessages().size());
        assertArrayEquals(new String[] {"bericht1", "bericht2"}, tree.getMessages().toArray());
	}
	
    @Test
    public void testUnmarshallValueCollectionWithContainerElement() {
        //fixture
        RootBinding root = new RootBinding(new QName("root"), ObjectTree.class);
        CollectionBinding collection = (CollectionBinding)root.setChild(new CollectionBinding(new QName("collection"), ArrayList.class), "messages");
        collection.setItem(new SimpleTypeBinding(new QName("detail")));
        BindingModel model = new BindingModel().register(root);
        
        ByteArrayInputStream stream = new ByteArrayInputStream("<root><collection><detail>bericht1</detail><detail>bericht2</detail></collection></root>".getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        ObjectTree tree = (ObjectTree)instance;
        assertNotNull(tree.getMessages());
        assertEquals(2, tree.getMessages().size());
        assertArrayEquals(new String[] {"bericht1", "bericht2"}, tree.getMessages().toArray());
    }
    
}
