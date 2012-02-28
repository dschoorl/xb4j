package info.rsdev.xb4j.model.java;

/**
 * A match is made on the basis of the Java types
 * 
 * @author Dave Schoorl
 */
public class InstanceOfChooser implements IChooser {
	
	private Class<?> instanceOf = null;

	/**
	 * Create a new instance of {@link InstanceOfChooser}. This implementation of {@link IChooser} will match a choice when the 
	 * type of the current java context matches this javaType
	 * @param javaType the type that the java context object must have for the {@link IChooser} to match this choice
	 */
	public InstanceOfChooser(Class<?> javaType) {
		if (javaType == null) {
			throw new NullPointerException("Class cannot be null");
		}
		this.instanceOf = javaType;
	}
	
	@Override
	public boolean matches(Object javaContext) {
		return this.instanceOf.isAssignableFrom(javaContext.getClass());
	}
	
	
}
