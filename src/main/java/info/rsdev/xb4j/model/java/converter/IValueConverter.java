package info.rsdev.xb4j.model.java.converter;

/**
 * Convert a String to a specific Java object and vice versa
 * 
 * @author Dave Schoorl
 */
public interface IValueConverter {
	
	public Object toObject(String value);
	
	public String toText(Object value);
	
	public Class<?> getJavaType();
	
}
