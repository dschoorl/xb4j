package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import info.rsdev.xb4j.test.ObjectC;

import javax.xml.namespace.QName;

import org.junit.Test;

public class SequenceBindingTest {
	
	@Test
	public void testMarshallMultipleElementsNoNamespace() {
		RootBinding root = new RootBinding(new QName("root"), ObjectC.class);
		SequenceBinding sequence = root.add(new SequenceBinding());
		sequence.add(new SimpleTypeBinding(new QName("naam")), "name");
		sequence.add(new SimpleTypeBinding(new QName("omschrijving")), "description");
		BindingModel model = new BindingModel().register(root);
		
		ObjectC instance = new ObjectC().setName("tester").setDescription("Ik test dingen");
		
        String expected = "<root><naam>tester</naam><omschrijving>Ik test dingen</omschrijving></root>";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, instance);
        assertEquals(expected, stream.toString());
	}
	
}
