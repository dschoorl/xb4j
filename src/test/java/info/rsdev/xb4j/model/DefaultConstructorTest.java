package info.rsdev.xb4j.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.test.MyObject;
import info.rsdev.xb4j.test.MyOtherObject;

import org.junit.Test;

public class DefaultConstructorTest {

    @Test
    public void testInstantiatePrivateDefaultConstructor() {
        DefaultConstructor constructor = new DefaultConstructor(MyObject.class);
        Object instance = constructor.newInstance();
        assertNotNull(instance);
        assertSame(MyObject.class, instance.getClass());
    }
    
    @Test(expected=Xb4jException.class)
    public void testNoDefaultConstructor() {
        new DefaultConstructor(MyOtherObject.class);    //has no default constructor
    }
    
    //TODO: test with anonymous inner classes?

}
