package info.rsdev.xb4j.model.java.constructor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.test.ObjectA;
import info.rsdev.xb4j.test.SubclassedObjectA;

import org.junit.Test;

public class DefaultConstructorTest {

    @Test
    public void testInstantiatePrivateDefaultConstructor() {
        DefaultConstructor constructor = new DefaultConstructor(ObjectA.class);
        Object instance = constructor.newInstance();
        assertNotNull(instance);
        assertSame(ObjectA.class, instance.getClass());
    }
    
    @Test(expected=Xb4jException.class)
    public void testNoDefaultConstructor() {
        new DefaultConstructor(SubclassedObjectA.class);    //has no default constructor
    }
    
    //TODO: test with anonymous inner classes?

}
