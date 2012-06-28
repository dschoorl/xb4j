package info.rsdev.xb4j.model.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.test.ChinesePerson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RecursorTest {
	
	private static final class FamilyTree {
		
	    private ChinesePerson child = null; //one child politics
	    
	    public FamilyTree() {}
	    
	    public ChinesePerson getChild() {
	        return this.child;
	    }
	    
	    public ChinesePerson setChild(ChinesePerson child) {
	    	this.child = child;
	    	return child;
	    }
	    
	    public int getFamilyTreeDepth() {
	    	if (child == null) {
	    		return 0;
	    	}
	    	return child.getFamilyTreeDepth();
	    }
	}
	
	private BindingModel model = null;
	
	@BeforeClass
	public static void setupOnce() {
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreWhitespace(true);
	}
	
	@Before
	public void setup() {
		model = new BindingModel();
		Root root = new Root(new QName("Stamboom"), FamilyTree.class);
		Recursor childRecursor = root.setChild(new Recursor(new QName("Kind"), ChinesePerson.class, "child"), "child");
		childRecursor.addAttribute(new Attribute(new QName("Voornaam")), "firstName");
		childRecursor.addAttribute(new Attribute(new QName("Achternaam")), "sirName");
		model.register(root);
	}
	
	@Test
	public void testMarshallNoRecurringElement() throws Exception {
		FamilyTree tree = new FamilyTree();
		assertEquals(0, tree.getFamilyTreeDepth());
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, tree);
        String expected = "<Stamboom />";
        
        XMLAssert.assertXMLEqual(expected, stream.toString());
	}
	
	@Test
	public void testMarshallSingleRecurringElement() throws Exception {
		FamilyTree tree = new FamilyTree();
		tree.setChild(new ChinesePerson("Mao", "Zedong"));
		assertEquals(1, tree.getFamilyTreeDepth());
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, tree);
        String expected = "<Stamboom>" +
        				  "  <Kind Voornaam=\"Mao\" Achternaam=\"Zedong\" />" +
        				  "</Stamboom>";
        
        XMLAssert.assertXMLEqual(expected, stream.toString());
	}
	
	@Test
	public void testMarshallMultipleRecurringElements() throws Exception {
		FamilyTree tree = new FamilyTree();
		tree.setChild(new ChinesePerson("Mao", "Zedong").setChild(new ChinesePerson("Mao Anqing", "Zedong").setChild(new ChinesePerson("Mao Xinyu", "Zedong"))));
		assertEquals(3, tree.getFamilyTreeDepth());
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, tree);
        String expected = "<Stamboom>" +
        				  "  <Kind Voornaam=\"Mao\" Achternaam=\"Zedong\">" +
        				  "    <Kind Voornaam=\"Mao Anqing\" Achternaam=\"Zedong\">" +
        				  "      <Kind Voornaam=\"Mao Xinyu\" Achternaam=\"Zedong\" />" +
        				  "    </Kind>" +
        				  "  </Kind>" +
        				  "</Stamboom>";
        
        XMLAssert.assertXMLEqual(expected, stream.toString());
	}
	
	@Test
	public void testUnmarshallNoRecurringElement() {
        String snippet = "<Stamboom />";
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(FamilyTree.class, instance.getClass());
        FamilyTree tree = (FamilyTree)instance;
		assertEquals(0, tree.getFamilyTreeDepth());
	}
	
	@Test
	public void testUnmarshallSingleRecurringElement() {
        String snippet = "<Stamboom>" +
				  		 "  <Kind Voornaam=\"Mao\" Achternaam=\"Zedong\" />" +
				  		 "</Stamboom>";
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(FamilyTree.class, instance.getClass());
        FamilyTree tree = (FamilyTree)instance;
		assertEquals(1, tree.getFamilyTreeDepth());
		assertEquals("Mao", tree.getChild().getFirstName());
		assertEquals("Zedong", tree.getChild().getSirName());
	}
	
	@Test
	public void testUnmarshallMultipleRecurringElements() {
        String snippet = "<Stamboom>" +
				  		 "  <Kind Voornaam=\"Mao\" Achternaam=\"Zedong\">" +
				  		 "    <Kind Voornaam=\"Mao Anqing\" Achternaam=\"Zedong\">" +
				  		 "      <Kind Voornaam=\"Mao Xinyu\" Achternaam=\"Zedong\" />" +
				  		 "    </Kind>" +
				  		 "  </Kind>" +
				  		 "</Stamboom>";
        ByteArrayInputStream stream = new ByteArrayInputStream(snippet.getBytes());
        Object instance = model.toJava(stream);
        assertNotNull(instance);
        assertSame(FamilyTree.class, instance.getClass());
        FamilyTree tree = (FamilyTree)instance;
		assertEquals(3, tree.getFamilyTreeDepth());
		assertEquals("Mao", tree.getChild().getFirstName());
		assertEquals("Zedong", tree.getChild().getSirName());
		assertNull(tree.getChild().getParent());
		assertEquals("Mao Anqing", tree.getChild().getChild().getFirstName());
		assertEquals("Mao", tree.getChild().getChild().getParent().getFirstName());
		assertEquals("Mao Xinyu", tree.getChild().getChild().getChild().getFirstName());
		assertEquals("Mao Anqing", tree.getChild().getChild().getChild().getParent().getFirstName());
	}
	
}