/* Copyright 2012 Red Star Development / Dave Schoorl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.exceptions.Xb4jMarshallException;
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.java.accessor.MethodSetter;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class is called an {@link Repeater} and not a {@link Collection}, in order to avoid name collission with the Java 
 * Collections interface.
 * 
 * @author dschoorl
 */
public class Repeater extends AbstractBinding {
	
	public static final int UNBOUNDED = Integer.MAX_VALUE;
	
	private IBinding itemBinding = null;
	
	private int maxOccurs = UNBOUNDED;
	
	/**
	 * Create a {@link Repeater} where the underlying collection is of the specified type. The type must be a concrete
	 * class, so it can be created during unmarshalling process (xml to java).
	 * @param collectionType
	 */
	public Repeater(Class<?> collectionType) {
		super(NoElementFetchStrategy.INSTANCE, new DefaultConstructor(collectionType));
	}
	
	public Repeater(Class<?> collectionType, boolean isOptional) {
		super(NoElementFetchStrategy.INSTANCE, new DefaultConstructor(collectionType));
        setOptional(isOptional);
	}
	
    public Repeater(QName element, Class<?> collectionType) {
    	this(element, collectionType, true);
    }
    
    public Repeater(QName element, Class<?> collectionType, boolean isOptional) {
    	super(new DefaultElementFetchStrategy(element), new DefaultConstructor(collectionType));
        setOptional(isOptional);
    }
    
	public <T extends IBinding> T setItem(T itemBinding) {
		if (itemBinding == null) {
			throw new NullPointerException("Binding for collection items cannot be null");
		}
		
		this.itemBinding = itemBinding;
		this.itemBinding.setParent(this);
		itemBinding.setSetter(new MethodSetter("add"));   //default add method for Collection interface;
		return itemBinding;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
	    //TODO: also support addmethod on container class, which will add to underlying collection for us
        Object newJavaContext = newInstance();
        Object collection = select(javaContext, newJavaContext);
        
        if (!(collection instanceof Collection<?>)) {
            throw new Xb4jUnmarshallException(String.format("Not a Collection: %s", collection), this);
        }
        
        //read enclosing collection element (if defined)
        QName collectionElement = getElement();
    	boolean startTagFound = false;
    	if (collectionElement != null) {
    		if (!staxReader.isAtElementStart(collectionElement)) {
	    		if (isOptional()) {
                    return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
	    		} else {
                    return UnmarshallResult.newMissingElement(this);
	    		}
    		} else {
    			startTagFound = true;
    		}
    	}
        
        attributesToJava(staxReader, select(javaContext, newJavaContext));

        int occurences = 0;
        UnmarshallResult result = null;
        boolean proceed = true;
        while (proceed) {
        	result = itemBinding.toJava(staxReader, collection);
            proceed = result.isUnmarshallSuccessful();
        	if (result.mustHandleUnmarshalledObject()) {
        		((Collection<Object>)collection).add(result.getUnmarshalledObject());
        	}
            if (proceed) {
            	occurences++;
            	if ((maxOccurs != UNBOUNDED) && (occurences > maxOccurs)) {
            		throw new Xb4jUnmarshallException(String.format("Found %d occurences, but no mare than %d are allowed", occurences, maxOccurs), this);
            	}
            }
        }
        
        //determine if the childBinding has no more occurences or whether the xml fragment of the childBinding is incomplete
        if (ErrorCodes.MISSING_MANDATORY_ERROR.equals(result.getErrorCode()) && !result.getBindingWithError().equals(resolveItemBinding(itemBinding))) {
        	return result;
        }
        
        if ((occurences == 0) && !isOptional()) {
        	return new UnmarshallResult(UnmarshallResult.MISSING_MANDATORY_ERROR, String.format("Mandatory %s has no content: %s", 
        			this, staxReader.getLocation()), this);
        }
        
        //read end of enclosing collection element (if defined)
        if ((collectionElement != null) && !staxReader.isAtElementEnd(collectionElement) && startTagFound) {
    		String encountered =  (staxReader.isAtElement()?String.format("(%s)", staxReader.getName()):"");
    		throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", collectionElement,
    				staxReader.getEventName(), encountered), this);
        }
        
        boolean isHandled = false;
        if (javaContext != null) {
        	isHandled = setProperty(javaContext, collection);
        }
		return new UnmarshallResult(newJavaContext, isHandled);
	}
	
	/**
	 * Get the binding for the items in this collection. Normally, this is {@link #itemBinding} defined on this {@link Repeater},
	 * but that could be a {@link Reference}, in which case we need to look a little further to find the real {@link IBinding} for 
	 * the collection items.
	 * @return the {@link IBinding} for the collection items
	 */
	private IBinding resolveItemBinding(IBinding itemBinding) {
		if (!(itemBinding instanceof Reference)) {
			return itemBinding;
		}
		
		if (itemBinding.getElement() != null) {
			return itemBinding;	//the element name is defined on the Reference
		}
		
		Reference reference = (Reference)itemBinding;
		itemBinding = reference.getChildBinding();	//itemBinding now is a ComplexType
		if (itemBinding.getElement() != null) {
			return itemBinding;	//the element name is defined in the ComplexType
		}
		
		itemBinding = ((ComplexType)itemBinding).getChildBinding();
		if (itemBinding instanceof Reference) {
			if (reference.equals(itemBinding)) {
				throw new Xb4jException("Encountered cirsular reference; Reference pointing to itself: ".concat(reference.toString()));
			}
			return resolveItemBinding(itemBinding);	//silly situation: a Reference pointing to a Reference, but support it anyway
		}
		return itemBinding;
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
		if (!generatesOutput(javaContext)) { return; }
		
        Object collection = getProperty(javaContext);
        if ((collection != null) && (!(collection instanceof Collection<?>))) {
        	throw new Xb4jMarshallException(String.format("Not a Collection: %s", collection), this);
        }
        
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        if ((collection == null) || ((Collection<?>)collection).isEmpty()) {
            if (isOptional()) {
                return;
            } else {
                throw new Xb4jMarshallException(String.format("This collection is not optional: %s", this), this);
            }
        }
        
        boolean isEmptyElement = (itemBinding == null) || (javaContext == null);
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
            attributesToXml(staxWriter, javaContext);
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
	
	@Override
	public boolean generatesOutput(Object javaContext) {
        Object collection = getProperty(javaContext);
        if ((collection != null) && (collection instanceof Collection<?>) && !((Collection<?>)collection).isEmpty()) {
        	for (Object item: (Collection<?>)collection) {
            	if (itemBinding.generatesOutput(item)) {
            		return true;
            	}
        	}
        }
        
		//At this point, the we established that the itemBinding will not output content
		return (getElement() != null) && (hasAttributes() || !isOptional());	//suppress optional empty elements (empty means: no content and no attributes)
	}
	
	public Repeater setMaxOccurs(int newMaxOccurs) {
		if (newMaxOccurs <= 1) {
			throw new Xb4jException("maxOccurs must be 1 or higher: "+newMaxOccurs);
		}
		this.maxOccurs = newMaxOccurs;
		return this;
	}
	
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append(getClass().getSimpleName()).append("[");
    	String separator = "";
    	QName element = getElement();
    	if (element != null) {
    		sb.append(separator).append("element=");
    		sb.append(element.toString());
    		separator = ",";
    	}
    	Class<?> collectionType = getJavaType();
    	if (collectionType != null) {
    		sb.append(separator).append("collectionType=").append(collectionType.getName());
    		separator = ",";
    	}
    	IBinding item = resolveItemBinding(itemBinding);
    	if (item != null) {
    		sb.append(separator).append("item=").append(item.toString());
    		separator = ",";
    	}
    	sb.append("]");
        return sb.toString();
    }
	
}
