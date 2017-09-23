package info.rsdev.xb4j.model.java.accessor;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.JavaContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodGetter extends AbstractMethodAccessor implements IGetter {

    public MethodGetter(String methodname) {
        super(methodname);
    }

    @Override
    public JavaContext get(JavaContext javaContext) {
        Method method = getMethod(javaContext.getContextObject().getClass(), this.methodname);
        try {
            Object result = method.invoke(javaContext.getContextObject());
            return javaContext.newContext(result);
        } catch (RuntimeException e) {
            throw e;	//to signal FindBugs that I consciously do not handle RuntimeExceptions
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Xb4jException(String.format("Could not get value from object '%s' through method '%s'",
                    javaContext.getContextObject(), this.methodname));
        }
    }

    @Override
    public String toString() {
        return "MethodGetter[methodname=".concat(methodname).concat("]");
    }

}
