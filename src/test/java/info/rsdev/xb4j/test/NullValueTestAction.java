package info.rsdev.xb4j.test;

import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.model.bindings.action.IMarshallingAction;
import info.rsdev.xb4j.model.java.JavaContext;

public class NullValueTestAction implements IMarshallingAction {

    @Override
    public String execute(JavaContext javaContext) throws Xb4jMarshallException {
        return null;
    }
}
