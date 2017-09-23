package info.rsdev.xb4j.model.java.accessor;

import static org.junit.Assert.*;
import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.JavaContext;

import org.junit.Before;
import org.junit.Test;

public class MethodGetterTest {

    private class SuperComputer {

        @SuppressWarnings("unused")
        public int getAnswerToTheUltimateQuestionOfLifeTheUniverseAndEverything() {
            return 42;
        }
    }

    private MethodGetter getter = null;

    @Before
    public void setup() {
        this.getter = new MethodGetter("getAnswerToTheUltimateQuestionOfLifeTheUniverseAndEverything");
    }

    @Test
    public void methodGetterObtainsValueThroughFullMethodName() {
        JavaContext myContext = new JavaContext(new SuperComputer());
        JavaContext returnContext = getter.get(myContext);
        assertNotNull(returnContext.getContextObject());
        assertSame(Integer.class, returnContext.getContextObject().getClass());
        assertEquals(42, returnContext.getContextObject());
    }

    @Test(expected = Xb4jException.class)
    public void methodGetterThrowsExceptionOnAccessWhenMethodNotExists() {
        JavaContext myContext = new JavaContext(new Object());
        getter.get(myContext);
    }

}
