package info.rsdev.xb4j.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import info.rsdev.xb4j.model.bindings.Choice;
import info.rsdev.xb4j.model.bindings.Element;
import info.rsdev.xb4j.model.bindings.Ignore;
import info.rsdev.xb4j.model.bindings.Repeater;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.UnmarshallResult;
import info.rsdev.xb4j.model.bindings.chooser.NeverChooser;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.ObjectB;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.XmlStreamFactory;

public class UnboundedChoicesTest {
    
    private Repeater repeater = null;
    private Choice choice = null;
    
    @Before
    public void setup() {
        Root root = new Root(new QName("sigh"), Object.class);
        repeater = root.setChild(new Repeater(new QName("list"), ArrayList.class));
        choice = repeater.setItem(new Choice());
    }
    
    @Test
    public void combinationsOfUnboundedChoicesAreSupported() throws Exception {
        //setup
        choice.addOption(new Element(new QName("elem1"), ObjectA.class));
        choice.addOption(new Element(new QName("elem2"), ObjectB.class));
        
        //run test
        ArrayList<?> collection = unmarshall("<list><elem1 /><elem2 /><elem1 /></list>");
        
        //assert
        assertEquals(3, collection.size());
        assertSame(ObjectA.class, collection.get(0).getClass());
        assertSame(ObjectB.class, collection.get(1).getClass());
        assertSame(ObjectA.class, collection.get(2).getClass());
    }
    
    @Test
    public void ignoreChoicesInUnboundedMandatoryChoiCe() throws Exception {
        choice.addOption(new Ignore(new QName("elem1")), NeverChooser.INSTANCE);
        choice.addOption(new Ignore(new QName("elem2")), NeverChooser.INSTANCE);
        
        //run test
        ArrayList<?> collection = unmarshall("<list><elem1 /><elem2 /><elem1 /></list>");
        
        //assert
        assertEquals(0, collection.size());
    }
    
    @Test
    public void ignoreChoicesInUnboundedOptionalChoice() throws Exception {
        repeater.setOptional(true);
        choice.setOptional(true);
        choice.addOption(new Ignore(new QName("elem1"), true), NeverChooser.INSTANCE);
        choice.addOption(new Ignore(new QName("elem2"), true), NeverChooser.INSTANCE);
        
        //run test
        ArrayList<?> collection = unmarshall("<list><elem1 /><elem2 /><elem1 /></list>");
        
        //assert
        assertEquals(0, collection.size());
    }
    
    private ArrayList<?> unmarshall(String xmlSnippet) throws XMLStreamException {
        StringReader reader = new StringReader(xmlSnippet);
        RecordAndPlaybackXMLStreamReader staxWriter = new RecordAndPlaybackXMLStreamReader(XmlStreamFactory.makeReader(reader));

        UnmarshallResult result = repeater.toJava(staxWriter, new JavaContext(null));
        assertTrue(result.isUnmarshallSuccessful());
        assertSame(ArrayList.class, result.getUnmarshalledObject().getClass());
        return (ArrayList<?>)result.getUnmarshalledObject();
    }
    
}
