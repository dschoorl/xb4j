package info.rsdev.xb4j.model.java.accessor;

public class NoGetter implements IGetter {
	
	public static final NoGetter INSTANCE = new NoGetter();
	
	private NoGetter() {}
	
	@Override
	public Object get(Object javaContext) {
		return javaContext;
	}
	
}
