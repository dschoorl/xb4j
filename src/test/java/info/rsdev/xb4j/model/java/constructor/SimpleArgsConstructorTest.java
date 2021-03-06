package info.rsdev.xb4j.model.java.constructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.bindings.Ignore;
import info.rsdev.xb4j.model.bindings.Repeater;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.Sequence;
import info.rsdev.xb4j.model.bindings.SimpleArgument;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectTree;
import info.rsdev.xb4j.util.XmlStreamFactory;
import java.io.StringReader;
import java.util.LinkedList;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Before;
import org.junit.Test;

public class SimpleArgsConstructorTest {

    private XMLStreamReader staxReader = null;

    private BindingModel model = null;
    private Sequence objectSequence = null;

    @Before
    public void setup() {
        model = new BindingModel();
        Root root = new Root(new QName("root"), ObjectTree.class);
        Sequence content = root.setChild(new Sequence(false));
        objectSequence = content.add(new Sequence(NoElementFetchStrategy.INSTANCE, new ArgsConstructor(ObjectA.class, new QName("name")), false), "myObject");
        Repeater messages = (Repeater) content.add(new Repeater(new QName("messages"), LinkedList.class, true), "messages");
        messages.setItem(new SimpleType(new QName("message"), false));
        model.registerRoot(root);
    }

    @Test
    public void testCreateObjectWithNextXmlElementInStream() throws XMLStreamException {
        objectSequence.add(new SimpleArgument(new QName("name"), false), "AName");
        staxReader = XmlStreamFactory.makeReader(new StringReader("<root><name>Repelsteeltje</name></root>"));

        Object instance = model.toJava(staxReader);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        assertNotNull(((ObjectTree) instance).getMyObject());
        assertEquals("Repelsteeltje", ((ObjectA) ((ObjectTree) instance).getMyObject()).getAName());
    }

    @Test
    public void testCreateObjectFromXmlElementInStreamSkippingSome() throws XMLStreamException {
        objectSequence.add(new Ignore(new QName("bla"), false));
        objectSequence.add(new SimpleArgument(new QName("name"), false), "AName");
        objectSequence.add(new Ignore(new QName("bla"), false));
        staxReader = XmlStreamFactory.makeReader(new StringReader("<root><bla /><name>Repelsteeltje</name><bla /></root>"));

        Object instance = model.toJava(staxReader);
        assertNotNull(instance);
        assertSame(ObjectTree.class, instance.getClass());
        assertNotNull(((ObjectTree) instance).getMyObject());
        assertEquals("Repelsteeltje", ((ObjectA) ((ObjectTree) instance).getMyObject()).getAName());
    }

//	@Test
//	public void testCreateObjectFromTwoXmlElementsInStream() throws XMLStreamException {
//		ArgsConstructor constructor = new ArgsConstructor(ObjectD.class, new QName("first"), new QName("last"));
//		staxReader = XMLStreamReaderFactory.newReader("<objectd><first>Dave</first><last>Schoorl</last></objectd>");
//		staxReader.nextTag();	//kickoff reading the xml stream
//		Object instance = constructor.newInstance(null, staxReader);
//		assertNotNull(instance);
//		assertSame(ObjectD.class, instance.getClass());
//		assertEquals("Schoorl, Dave", ((ObjectD)instance).getFullName());
//	}
//
//	@Test
//	public void testCreateObjectFromTwoXmlElementsInStreamReversedOrder() throws XMLStreamException {
//		ArgsConstructor constructor = new ArgsConstructor(ObjectD.class, new QName("first"), new QName("last"));
//		staxReader = XMLStreamReaderFactory.newReader("<objectd><last>Schoorl</last><first>Dave</first></objectd>");
//		staxReader.nextTag();	//kickoff reading the xml stream
//		Object instance = constructor.newInstance(null, staxReader);
//		assertNotNull(instance);
//		assertSame(ObjectD.class, instance.getClass());
//		assertEquals("Schoorl, Dave", ((ObjectD)instance).getFullName());
//	}
}
