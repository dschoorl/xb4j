package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.accessor.MethodSetter;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class CollectionBinding extends AbstractBindingBase {
	
	public static final int UNBOUNDED = Integer.MAX_VALUE;
	
	private IBindingBase itemBinding = null;
	
	private int maxOccurs = UNBOUNDED;
	
	public CollectionBinding(Class<?> javaType) {
	    setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
		setObjectCreator(new DefaultConstructor(javaType));
	}
	
    public CollectionBinding(QName element, Class<?> javaType) {
        setElementFetchStrategy(new DefaultElementFetchStrategy(element));
        setObjectCreator(new DefaultConstructor(javaType));
    }
    
	public <T extends IBindingBase> T setItem(T itemBinding) {
		if (itemBinding == null) {
			throw new NullPointerException("Binding for collection items cannot be null");
		}
		
		this.itemBinding = itemBinding;
		this.itemBinding.setParent(this);
		itemBinding.setSetter(new MethodSetter("add"));   //default add method for Collection interface;
		return itemBinding;
	}
	
	@Override
	public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
	    //TODO: also support addmethod on container class, which will add to underlying collection for us
        Object newJavaContext = newInstance();
        Object collection = select(javaContext, newJavaContext);
        
        if (!(collection instanceof Collection<?>)) {
            throw new Xb4jException(String.format("Not a Collection: %s", collection));
        }
        
        //read enclosing collection element (if defined)
        QName collectionElement = getElement();
        if ((collectionElement != null) && !staxReader.isAtElementStart(collectionElement)) {
            throw new Xb4jException(String.format("Expected collection tag %s, but encountered element %s",
                    collectionElement, staxReader.getName()));
        }
        
        int occurences = 0;
        boolean proceed = true;
        while (proceed) {
            proceed = (itemBinding.toJava(staxReader, collection) != null);
            if (proceed) {
            	occurences++;
            	if ((maxOccurs != UNBOUNDED) && (occurences > maxOccurs)) {
            		throw new Xb4jException(String.format("Found %d occurences, but no mare than %d are allowed", occurences, maxOccurs));
            	}
            }
        }
        
        if ((occurences == 0) && !isOptional()) {
        	throw new Xb4jException("Mandatory collection has no content");
        }
        
        if (javaContext != null) {
        	setProperty(javaContext, collection);
        }
        
        //read end of enclosing collection element (if defined)
        if ((collectionElement != null) && !staxReader.isAtElementEnd(collectionElement)) {
            throw new Xb4jException(String.format("Expected element close tag </%s> (encountered a %s)",
                    collectionElement, staxReader.getEventName()));
        }
        
		return newJavaContext;
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        Object collection = getProperty(javaContext);
        if (collection == null) {
            if (isOptional()) {
                return;
            } else {
                throw new Xb4jException(String.format("This collection is not optional: %s", this));
            }
        }
        
        if (!(collection instanceof Collection<?>)) {
        	throw new Xb4jException(String.format("Not a Collection: %s", javaContext));
        }
        
        boolean isEmptyElement = (itemBinding == null) || (javaContext == null);
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
        }
        
        if (itemBinding != null) {
        	for (Object item: (Collection<?>)collection) {
            	itemBinding.toXml(staxWriter, item);
        	}
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
	}
	
	public CollectionBinding setMaxOccurs(int newMaxOccurs) {
		if (newMaxOccurs <= 1) {
			throw new Xb4jException("maxOccurs must be 1 or higher: "+newMaxOccurs);
		}
		this.maxOccurs = newMaxOccurs;
		return this;
	}
	
}
