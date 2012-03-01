package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.rsdev.xb4j.model.java.InstanceOfChooser;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

public class ChoiceBindingTest {
	
	@Test
	public void testMarshallChoiceNoNamespaces() throws Exception {
		ChoiceBinding choice = new ChoiceBinding();
		choice.addChoice(new SimpleTypeBinding(new QName("elem1")), "name", new InstanceOfChooser(ObjectA.class));
		choice.addChoice(new SimpleTypeBinding(new QName("elem2")), "value", new InstanceOfChooser(ObjectB.class));
		
		ObjectA instanceA = new ObjectA("test");
		String expected = "<elem1>test</elem1>";
		
        StringWriter writer = new StringWriter();
        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        choice.toXml(new SimplifiedXMLStreamWriter(staxWriter), instanceA);
        assertEquals(expected, writer.toString());
        
		ObjectB instanceB = new ObjectB("test");
		expected = "<elem2>test</elem2>";
		
        writer = new StringWriter();
        staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        choice.toXml(new SimplifiedXMLStreamWriter(staxWriter), instanceB);
        assertEquals(expected, writer.toString());
	}
	
	@Test
	public void testUnmarshallChoiceNoNamespaces() throws Exception {
	    ICreator objectAProvider = mock(ICreator.class);
	    when(objectAProvider.newInstance()).thenReturn(new ObjectA(""));
	    
		ChoiceBinding choice = new ChoiceBinding(mock(IElementFetchStrategy.class), objectAProvider);
		choice.addChoice(new SimpleTypeBinding(new QName("elem1")), "name", new InstanceOfChooser(ObjectA.class));
		choice.addChoice(new SimpleTypeBinding(new QName("elem2")), "value", new InstanceOfChooser(ObjectB.class));
		
		//unmarshall first option
		ByteArrayInputStream stream = new ByteArrayInputStream("<elem1>test</elem1>".getBytes());
		Object instance = choice.toJava(new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream)), null);
		assertNotNull(instance);
		assertSame(ObjectA.class, instance.getClass());
		assertEquals("test", ((ObjectA)instance).getName());
		
		//unmarshall second option
		ICreator objectBProvider = mock(ICreator.class);
        when(objectBProvider.newInstance()).thenReturn(new ObjectB(""));
        choice.setObjectCreator(objectBProvider);
        
		stream = new ByteArrayInputStream("<elem2>test</elem2>".getBytes());
		instance = choice.toJava(new RecordAndPlaybackXMLStreamReader(XMLInputFactory.newInstance().createXMLStreamReader(stream)), null);
		assertNotNull(instance);
		assertSame(ObjectB.class, instance.getClass());
		assertEquals("test", ((ObjectB)instance).getValue());
	}
	
}
