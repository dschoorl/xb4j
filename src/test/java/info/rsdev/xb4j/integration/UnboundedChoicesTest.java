package info.rsdev.xb4j.integration;

import static info.rsdev.xb4j.test.UnmarshallUtils.unmarshall;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.bindings.Ignore;
import info.rsdev.xb4j.model.bindings.Repeater;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.chooser.NeverChooser;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;

public class UnboundedChoicesTest {

    private Repeater repeater = null;
    private Choice choice = null;

    @BeforeEach
    public void setup() {
        Root root = new Root(new QName("sigh"), Object.class);
        repeater = root.setChild(new Repeater(new QName("list"), ArrayList.class, true));
        choice = repeater.setItem(new Choice(false));
    }

    @Test
    public void combinationsOfUnboundedChoicesAreSupported() throws Exception {
        //setup
        choice.addOption(new Element(new QName("elem1"), ObjectA.class, false));
        choice.addOption(new Element(new QName("elem2"), ObjectB.class, false));

        //run test
        ArrayList<?> collection = unmarshall(repeater, ArrayList.class, "<list>\n<elem1 />\n<elem2 />\n<elem1 />\n</list>\n");
        
        //assert
        assertEquals(3, collection.size());
        assertSame(ObjectA.class, collection.get(0).getClass());
        assertSame(ObjectB.class, collection.get(1).getClass());
        assertSame(ObjectA.class, collection.get(2).getClass());
    }

    @Test
    public void ignoreChoicesInUnboundedMandatoryChoiceAndOptions() throws Exception {
        choice.addOption(new Ignore(new QName("elem1"), false), NeverChooser.INSTANCE);
        choice.addOption(new Ignore(new QName("elem2"), false), NeverChooser.INSTANCE);

        //run test
        ArrayList<?> collection = unmarshall(repeater, ArrayList.class, "<list>\n<elem1 />\n<elem2 />\n<elem1 />\n</list>\n");

        //assert
        assertEquals(0, collection.size());
    }

    @Test
    public void ignoreChoicesInUnboundedOptionalChoiceAndMandatoryOptions() throws Exception {
        choice = repeater.setItem(new Choice(true));
        choice.addOption(new Ignore(new QName("elem1"), false), NeverChooser.INSTANCE);
        choice.addOption(new Ignore(new QName("elem2"), false), NeverChooser.INSTANCE);

        //run test
        ArrayList<?> collection =  unmarshall(repeater, ArrayList.class, "<list>\n<elem1 />\n<elem2 />\n<elem1 />\n</list>\n");

        //assert
        assertEquals(0, collection.size());
    }

}
