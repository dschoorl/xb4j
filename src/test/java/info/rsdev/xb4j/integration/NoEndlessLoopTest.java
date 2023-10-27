package info.rsdev.xb4j.integration;

import static info.rsdev.xb4j.test.UnmarshallUtils.unmarshall;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.bindings.Repeater;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.Sequence;
import info.rsdev.xb4j.model.bindings.SimpleType;
import info.rsdev.xb4j.model.converter.IntegerConverter;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;

class NoEndlessLoopTest {

    private static class DocRoot {

        List<Object> collection = null;
    }

    private Root root = null;

    @BeforeEach
    public void setupBinding() {
        root = new Root(new QName("root"), DocRoot.class);
        Element with = root.setChild(new Element(new QName("with"), false));
        Repeater collection = with.setChild(new Repeater(new QName("collection"), ArrayList.class, true), "collection");  //we enter a endless loop when collection is empty or all items are ignored
        Sequence collectionItems = collection.setItem(new Sequence(false));

        //define an unbounded choice between ObjectA en ObjectB types
        Repeater aAndBs = collectionItems.add(new Repeater(ArrayList.class, true));
        Choice aAndBTypes = aAndBs.setItem(new Choice(true));
        Element aType = aAndBTypes.addOption(new Element(new QName("a"), ObjectA.class, true));
        aType.setChild(new SimpleType(new QName("name"), false), "name");
        Element bType = aAndBTypes.addOption(new Element(new QName("b"), ObjectB.class, true));
        bType.setChild(new SimpleType(new QName("number"), IntegerConverter.INSTANCE, false), "value");

    }

    @Test
    @Timeout(value=500, unit = TimeUnit.MILLISECONDS)
    void stopRepeaterWhenChildChoiceHasNoMoreFittingOptions() throws XMLStreamException {
        DocRoot docRoot = unmarshall(root, DocRoot.class, EXAMPLE);
        assertNotNull(docRoot.collection);
        assertEquals(1, docRoot.collection.size());
        assertSame(ArrayList.class, docRoot.collection.get(0).getClass());
        assertEquals(3, ((ArrayList<?>) docRoot.collection.get(0)).size());
    }

    @Test
    @Timeout(value=500, unit = TimeUnit.MILLISECONDS)
    void stopRepeaterWhenThereAreNoFittingOptions() throws XMLStreamException {
        DocRoot docRoot = unmarshall(root, DocRoot.class, EMPTY_EXAMPLE);
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

}
