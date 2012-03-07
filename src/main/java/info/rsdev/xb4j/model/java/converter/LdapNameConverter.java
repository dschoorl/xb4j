package info.rsdev.xb4j.model.java.converter;

import info.rsdev.xb4j.exceptions.Xb4jException;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

/**
 * 
 * @author Dave Schoorl
 */
public class LdapNameConverter implements IValueConverter {
	
	public static final LdapNameConverter INSTANCE = new LdapNameConverter();

	@Override
	public Object toObject(String value) throws Xb4jException {
		if (value == null) { return null; }
		
		try {
			return new LdapName(value);
		} catch (InvalidNameException e) {
			throw new Xb4jException(String.format("Could not convert text '%s' to LdapName: ", value));
		}
	}

	@Override
	public String toText(Object value) throws Xb4jException {
		if (value == null) { return null; }
		if (!(value instanceof LdapName)) {
			throw new Xb4jException(String.format("Expected a %s, but was a %s", LdapName.class.getName(), 
					value.getClass().getName()));
		}
		return ((LdapName)value).toString();
	}

    @Override
    public Class<?> getJavaType() {
        return LdapName.class;
    }
	
}
