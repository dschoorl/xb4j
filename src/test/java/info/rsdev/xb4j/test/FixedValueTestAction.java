package info.rsdev.xb4j.test;

import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.model.bindings.action.IMarshallingAction;
import info.rsdev.xb4j.model.java.JavaContext;

public class FixedValueTestAction implements IMarshallingAction {
	
	public static final FixedValueTestAction INSTANCE = new FixedValueTestAction(); 
	@Override
	public String execute(JavaContext javaContext) throws Xb4jMarshallException {
		return "Fixed value";
	}
}