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
import javax.xml.stream.XMLStreamReader;

public class CollectionBinding extends AbstractBindingBase {
	
	public static final int UNBOUNDED = Integer.MAX_VALUE;
	
	private IBindingBase itemBinding = null;
	
	private int maxOccurs = UNBOUNDED;
	
	public CollectionBinding(Class<?> javaType) {
	    setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
		setObjectCreator(new DefaultConstructor(javaType));
		setSetter(new MethodSetter("add"));   //default add method for Collection interface
	}
	
    public CollectionBinding(QName element, Class<?> javaType) {
        setElementFetchStrategy(new DefaultElementFetchStrategy(element));
        setObjectCreator(new DefaultConstructor(javaType));
        
    }
    
	public IBindingBase setItem(IBindingBase itemBinding) {
		if (itemBinding == null) {
			throw new NullPointerException("Binding for collection items cannot be null");
		}
		this.itemBinding = itemBinding.setSetter(new MethodSetter("add"));   //default add method for Collection interface;
		return this.itemBinding;
	}
	
	public ChoiceBinding setItem(ChoiceBinding itemBinding) {
		setItem((IBindingBase)itemBinding);
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
        if (collectionElement != null) {
            if (staxReader.nextTag() == XMLStreamReader.START_ELEMENT) {
                QName element = staxReader.getName();
                if (!isExpected(element)) { //take optional into account??
                    throw new Xb4jException(String.format("Expected collection tag %s, but encountered element %s",
                            collectionElement, element));
                }
            }
        }
        
        Object result = null;
        boolean proceed = true;
        while (proceed) {
            staxReader.startRecording(); //TODO: support multiple simultaneous recordings (markings)
            try {
                result = itemBinding.toJava(staxReader, collection);
                if (proceed = (result != null)) {
                    staxReader.stopAndWipeRecording();
                }
            } finally {
                staxReader.rewindAndPlayback();
            }
        }
        if (javaContext != null) {
        	setProperty(javaContext, collection);
        }
        
        //read end of enclosing collection element (if defined)
        if (collectionElement != null) {
            if (staxReader.nextTag() == XMLStreamReader.END_ELEMENT) {
                QName element = staxReader.getName();
                if (!isExpected(element)) { //take optional into account??
                    throw new Xb4jException(String.format("Encountered unexpected close tag %s (expected close tag %s",
                            element, collectionElement));
                }
            }
        }
        
		return newJavaContext;
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        Object collection = getProperty(javaContext);
        
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
	
}
