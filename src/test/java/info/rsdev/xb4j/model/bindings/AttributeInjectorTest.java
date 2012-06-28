package info.rsdev.xb4j.model.bindings;

import static org.junit.Assert.assertEquals;
import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.model.bindings.action.IMarshallingAction;
import info.rsdev.xb4j.model.converter.NullConverter;
import info.rsdev.xb4j.model.java.accessor.NoGetter;
import info.rsdev.xb4j.model.java.accessor.NoSetter;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;

import org.junit.Before;
import org.junit.Test;

public class AttributeInjectorTest {
	
	private IMarshallingAction action = null;
	
	private StringWriter writer = null;
	
	private SimplifiedXMLStreamWriter staxWriter = null;	
	
	@Before
	public void setUp() throws Exception {
		action = new IMarshallingAction() {
			@Override
			public String execute(Object javaContext) throws Xb4jMarshallException {
				return "Fixed value";
			}
		};
        writer = new StringWriter();
        staxWriter = new SimplifiedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
	}
	
	@Test
	public void testToXml() throws Exception {
		SimpleType simpleElement = new SimpleType(new QName("Simple"), NullConverter.INSTANCE);
		simpleElement.addAttribute(new AttributeInjector(new QName("attribute"), action), NoGetter.INSTANCE, NoSetter.INSTANCE);
		simpleElement.toXml(staxWriter, new ObjectA("true"));
		staxWriter.close();
		
		assertEquals("<Simple attribute=\"Fixed value\"/>", this.writer.toString());
	}
	
}
