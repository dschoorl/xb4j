package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class CollectionBinding extends AbstractBinding {
	
	private IBinding itemBinding = null;
	
	public CollectionBinding(Class<?> javaType) {
		setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
		setObjectCreator(new DefaultConstructor(javaType));
	}
	
	public IBinding setItem(IBinding itemBinding) {
		if (itemBinding == null) {
			throw new NullPointerException("Binding for collection items cannot be null");
		}
		this.itemBinding = itemBinding;
		return this.itemBinding;
	}
	
	public ChoiceBinding setItem(ChoiceBinding itemBinding) {
		setItem((IBinding)itemBinding);
		return itemBinding;
	}
	
	@Override
	public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        
        if (!(javaContext instanceof Collection<?>)) {
        	throw new Xb4jException(String.format("Not a Collection: %s", javaContext));
        }
        
        boolean isEmptyElement = (itemBinding == null) || (javaContext == null);
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
        }
        
        if (itemBinding != null) {
        	for (Object newJavaContext: (Collection<?>)javaContext) {
            	itemBinding.toXml(staxWriter, newJavaContext);
        	}
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
	}
	
}
