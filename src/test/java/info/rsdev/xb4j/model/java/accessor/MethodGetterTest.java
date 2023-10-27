package info.rsdev.xb4j.model.java.accessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.JavaContext;

class MethodGetterTest {

    private class SuperComputer {

        @SuppressWarnings("unused")
        public int getAnswerToTheUltimateQuestionOfLifeTheUniverseAndEverything() {
            return 42;
        }
    }

    private MethodGetter getter = null;

    @BeforeEach
    public void setup() {
        this.getter = new MethodGetter("getAnswerToTheUltimateQuestionOfLifeTheUniverseAndEverything");
    }

    @Test
    void methodGetterObtainsValueThroughFullMethodName() {
        JavaContext myContext = new JavaContext(new SuperComputer());
        JavaContext returnContext = getter.get(myContext);
        assertNotNull(returnContext.getContextObject());
        assertSame(Integer.class, returnContext.getContextObject().getClass());
        assertEquals(42, returnContext.getContextObject());
    }

    @Test
    void methodGetterThrowsExceptionOnAccessWhenMethodNotExists() {
        JavaContext myContext = new JavaContext(new Object());
        assertThrows(Xb4jException.class, () -> getter.get(myContext));
    }

}
