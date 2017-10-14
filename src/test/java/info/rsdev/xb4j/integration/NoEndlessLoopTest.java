package info.rsdev.xb4j.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.bindings.Repeater;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.Sequence;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.model.bindings.UnmarshallResult;
import info.rsdev.xb4j.model.converter.IntegerConverter;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.XmlStreamFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;

//@org.junit.Ignore
public class NoEndlessLoopTest {

    private static class DocRoot {

        List<Object> collection = null;
    }

    private Root root = null;

    @Before
    public void setupBinding() {
        root = new Root(new QName("root"), DocRoot.class);
        Element with = root.setChild(new Element(new QName("with")));
        Repeater collection = with.setChild(new Repeater(new QName("collection"), ArrayList.class, true), "collection");  //we enter a endless loop when collection is empty or all items are ignored
        Sequence collectionItems = collection.setItem(new Sequence());

        //define an unbounded choice between ObjectA en ObjectB types
        Repeater aAndBs = collectionItems.add(new Repeater(ArrayList.class, true));
        Choice aAndBTypes = aAndBs.setItem(new Choice(true));
        Element aType = aAndBTypes.addOption(new Element(new QName("a"), ObjectA.class, true));
        aType.setChild(new SimpleType(new QName("name")), "name");
        Element bType = aAndBTypes.addOption(new Element(new QName("b"), ObjectB.class, true));
        bType.setChild(new SimpleType(new QName("number"), IntegerConverter.INSTANCE), "value");

    }

    @Test(timeout = 500)
    public void stopRepeaterWhenChildChoiceHasNoMoreFittingOptions() throws XMLStreamException {
        DocRoot docRoot = unmarshall(EXAMPLE);
        assertNotNull(docRoot.collection);
        assertEquals(1, docRoot.collection.size());
        assertSame(ArrayList.class, docRoot.collection.get(0).getClass());
        assertEquals(3, ((ArrayList<?>) docRoot.collection.get(0)).size());
    }

    @Test(timeout = 500)
    public void stopRepeaterWhenThereAreNoFittingOptions() throws XMLStreamException {
        DocRoot docRoot = unmarshall(EMPTY_EXAMPLE);
        assertNotNull(docRoot);
        assertNotNull(docRoot.collection);
        assertEquals(1, docRoot.collection.size());
        assertSame(ArrayList.class, docRoot.collection.get(0).getClass());
        assertEquals(0, ((ArrayList<?>) docRoot.collection.get(0)).size());
    }

    private static final String EXAMPLE
            = "<root>\n"
            + "<with>\n"
            + "<collection>\n"
            + "<a><name>Roy</name></a>\n"
            + "<b><number>42</number></b>\n"
            + "<a><name>Linda</name></a>\n"
            + "</collection>\n"
            + "</with>\n"
            + "</root>\n";

    private static final String EMPTY_EXAMPLE
            = "<root>\n"
            + "<with>\n"
            + "<collection />\n"
            + "</with>\n"
            + "</root>\n";

    private DocRoot unmarshall(String xmlSnippet) throws XMLStreamException {
        StringReader reader = new StringReader(xmlSnippet);
        RecordAndPlaybackXMLStreamReader staxWriter = new RecordAndPlaybackXMLStreamReader(XmlStreamFactory.makeReader(reader));

        UnmarshallResult result = root.toJava(staxWriter, new JavaContext(null));
        assertTrue(result.isUnmarshallSuccessful());
        assertSame(DocRoot.class, result.getUnmarshalledObject().getClass());
        return (DocRoot) result.getUnmarshalledObject();
    }

}
