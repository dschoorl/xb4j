package info.rsdev.xb4j.model.java.accessor;

public class NoSetter implements ISetter {
	
	public static final NoSetter INSTANCE = new NoSetter();
	
	private NoSetter() {}
	
	@Override
	public boolean set(Object javaContext, Object propertyValue) {
		return false;
	}
	
}
