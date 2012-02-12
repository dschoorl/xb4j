package info.rsdev.xb4j.model;

import static org.junit.Assert.assertEquals;
import java.io.StringWriter;

import org.junit.Test;

/**
 *
 * @author Dave Schoorl
 */
public class BindingModelTest {

    @Test
    public void testSimpleMarshalling() {
        StringWriter writer = new StringWriter();
        Object instance = new Object();
        BindingModel model = new BindingModel();
        model.toXml(writer, instance);
        assertEquals("<root/>", writer.toString());
    }

}
