package info.rsdev.xb4j.model.java;

import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;

/**
 * 
 * @author dschoorl
 */
public class PropertyNotNullChooser implements IChooser {
	
	private FieldAccessProvider fieldAccessor = null;
	
	public PropertyNotNullChooser(String fieldName) {
		this.fieldAccessor = new FieldAccessProvider(fieldName);
	}
	
	@Override
	public boolean matches(Object javaContext) {
		if (javaContext == null) { return true; }
		Object fieldValue = fieldAccessor.get(javaContext);
		return fieldValue != null;
	}
	
}
