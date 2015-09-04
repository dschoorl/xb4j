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
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.MimicSetter;
import info.rsdev.xb4j.model.java.constructor.DefaultConstructor;
import info.rsdev.xb4j.model.java.constructor.NullCreator;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class is called an {@link MapRepeater} and not a {@link Map}, in order to avoid type name collission with the Java 
 * Collections interface.
 * 
 * TODO: use keyValue container element on top of / instead of map container element? Currently only map container element is supported
 * 
 * @author Dave Schoorl
 */
public class MapRepeater extends AbstractBinding {
	
	public static final int UNBOUNDED = Integer.MAX_VALUE;
	
	private IBinding keyBinding = null;
	
	private IBinding valueBinding = null;
	
	private int maxOccurs = UNBOUNDED;
	
	/**
	 * Create a {@link MapRepeater} where the collection instance is passed on from the parent binding in the binding tree. The main
	 * use case is to support the {@link Root} xml element to contain a {@link Map} without an additional container element. 
	 */
	public MapRepeater() {
		super(NoElementFetchStrategy.INSTANCE, NullCreator.INSTANCE);
		setSetter(MimicSetter.INSTANCE);
	}
	
	/**
	 * Create a {@link MapRepeater} where the underlying collection is of the specified type. The type must be a concrete
	 * class, so it can be created during unmarshalling process (xml to java).
	 * @param mapType
	 */
	public MapRepeater(Class<?> mapType) {
		super(NoElementFetchStrategy.INSTANCE, new DefaultConstructor(mapType));
	}
	
	public MapRepeater(Class<?> mapType, boolean isOptional) {
		super(NoElementFetchStrategy.INSTANCE, new DefaultConstructor(mapType));
        setOptional(isOptional);
	}
	
    public MapRepeater(QName element, Class<?> mapType) {
    	this(element, mapType, true);
    }
    
    public MapRepeater(QName element, Class<?> mapType, boolean isOptional) {
    	super(new DefaultElementFetchStrategy(element), new DefaultConstructor(mapType));
        setOptional(isOptional);
    }
    
	public <T extends IBinding> MapRepeater setKeyValue(T keyBinding, T valueBinding) {
		if (keyBinding == null) {
			throw new NullPointerException("Binding for map keys cannot be null");
		}
		if (valueBinding == null) {
			throw new NullPointerException("Binding for map values cannot be null");
		}
		
		getSemaphore().lock();
		try {
			validateMutability();
			this.keyBinding = keyBinding;
			this.keyBinding.setParent(this);
			this.valueBinding = valueBinding;
			this.valueBinding.setParent(this);
			return this;
		} finally {
			getSemaphore().unlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        JavaContext javaMapContext = select(javaContext, newInstance(staxReader, javaContext));
        Object contextObject = javaMapContext.getContextObject();
        if (!(contextObject instanceof Map<?, ?>)) {
            throw new Xb4jUnmarshallException(String.format("Not a Map: %s", contextObject), this);
        }
        Map<Object, Object> map = (Map<Object, Object>)contextObject;
        
        //read enclosing collection element (if defined)
        QName containerElement = getElement();
    	boolean startTagFound = false;
    	if (containerElement != null) {
    		if (!staxReader.isNextAnElementStart(containerElement)) {
	    		if (isOptional()) {
                    return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
	    		} else {
                    return UnmarshallResult.newMissingElement(this);
	    		}
    		} else {
    			startTagFound = true;
    		}
    	}
        
        attributesToJava(staxReader, javaMapContext);

        int occurences = 0;
        UnmarshallResult keyResult = null;
        UnmarshallResult valueResult = null;
        boolean proceed = true;
        while (proceed) {
        	keyResult = keyBinding.toJava(staxReader, javaMapContext);
        	valueResult = valueBinding.toJava(staxReader, javaContext);
            proceed = keyResult.isUnmarshallSuccessful() && valueResult.isUnmarshallSuccessful();
            if (proceed) {
            	occurences++;
            	if ((maxOccurs != UNBOUNDED) && (occurences > maxOccurs)) {
            		throw new Xb4jUnmarshallException(String.format("Found %d occurences, but no more than %d are allowed", occurences, maxOccurs), this);
            	}
            	map.put(keyResult.getUnmarshalledObject(), valueResult.getUnmarshalledObject());
            }
        }
        
        //determine if the keyValue bindings have no more occurences or whether the xml fragment of those bindings are incomplete
        if (ErrorCodes.MISSING_MANDATORY_ERROR.equals(keyResult.getErrorCode()) && !keyResult.getFaultyBinding().equals(resolveBinding(keyBinding))) {
        	return keyResult;
        }
        if (ErrorCodes.MISSING_MANDATORY_ERROR.equals(valueResult.getErrorCode()) && !valueResult.getFaultyBinding().equals(resolveBinding(valueBinding))) {
        	return keyResult;
        }
        
        if ((occurences == 0) && !isOptional()) {
        	return new UnmarshallResult(UnmarshallResult.MISSING_MANDATORY_ERROR, String.format("Mandatory %s has no content: %s", 
        			this, staxReader.getLocation()), this);
        }
        
        //read end of enclosing collection element (if defined)
        if ((containerElement != null) && !staxReader.isNextAnElementEnd(containerElement) && startTagFound) {
    		String encountered =  (staxReader.isAtElement()?String.format("(%s)", staxReader.getName()):"");
    		throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", containerElement,
    				staxReader.getEventName(), encountered), this);
        }
        
        boolean isHandled = false;
        if (javaContext.getContextObject() != null) {
        	isHandled = setProperty(javaContext, map);
        }
		return new UnmarshallResult(map, isHandled);
	}
	
	/**
	 * Get the binding for the entries in this map. Normally, this is {@link #keyBinding} defined on this {@link MapRepeater},
	 * but that could be a {@link Reference}, in which case we need to look a little further to find the real {@link IBinding} for 
	 * the collection items.
	 * @return the {@link IBinding} for the collection items
	 */
	private IBinding resolveBinding(IBinding keyOrValueBinding) {
		if (!(keyOrValueBinding instanceof Reference)) {
			return keyOrValueBinding;
		}
		
		if (keyOrValueBinding.getElement() != null) {
			return keyOrValueBinding;	//the element name is defined on the Reference
		}
		
		Reference reference = (Reference)keyOrValueBinding;
		keyOrValueBinding = reference.getChildBinding();	//itemBinding now is a ComplexType
		if (keyOrValueBinding.getElement() != null) {
			return keyOrValueBinding;	//the element name is defined in the ComplexType
		}
		
		keyOrValueBinding = ((ComplexType)keyOrValueBinding).getChildBinding();
		if (keyOrValueBinding instanceof Reference) {
			if (reference.equals(keyOrValueBinding)) {
				throw new Xb4jException("Encountered cirsular reference; Reference pointing to itself: ".concat(reference.toString()));
			}
			return resolveBinding(keyOrValueBinding);	//silly situation: a Reference pointing to a Reference, but support it anyway
		}
		return keyOrValueBinding;
	}
	
	@Override
	public void marshall(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
		if (!generatesOutput(javaContext)) { return; }
		
        Object map = getProperty(javaContext).getContextObject();
        if ((map != null) && (!(map instanceof Map<?, ?>))) {
        	throw new Xb4jMarshallException(String.format("Not a Map: %s", map), this);
        }
        
        //when this Binding must not output an element, the getElement() method should return null
        QName element = getElement();
        if ((map == null) || ((Map<?, ?>)map).isEmpty()) {
            if (isOptional()) {
                return;
            } else {
                throw new Xb4jMarshallException(String.format("This Map is not optional: %s", this), this);
            }
        }
        
        boolean isEmptyElement = (keyBinding == null) || (valueBinding == null) || (javaContext == null);
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
            attributesToXml(staxWriter, javaContext);
        }
        
        if ((keyBinding != null) || (valueBinding != null)) {
        	int index = 0;
        	for (Entry<?, ?> keyValue: ((Map<?, ?>)map).entrySet()) {
        		if (keyBinding != null) {
        			keyBinding.toXml(staxWriter, javaContext.newContext(keyValue.getKey(), index));
        		}
            	if (valueBinding != null) {
            		valueBinding.toXml(staxWriter, javaContext.newContext(keyValue.getValue(), index));
            	}
            	index++;
        	}
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
	}
	
	@Override
	public boolean generatesOutput(JavaContext javaContext) {
        Object map = getProperty(javaContext).getContextObject();
        if (map != null) {
	        if (!(map instanceof Map<?, ?>)) {
	        	throw new Xb4jMarshallException(String.format("Not a Map type: %s", map.getClass()), this);
	        }
	        if (!((Map<?, ?>)map).isEmpty()) {
	        	for (Entry<?, ?> keyValue: ((Map<?, ?>)map).entrySet()) {
	            	if (keyBinding.generatesOutput(javaContext.newContext(keyValue.getKey())) ||
	            		valueBinding.generatesOutput(javaContext.newContext(keyValue.getValue()))) {
	            		return true;
	            	}
	        	}
	        }
        }
        
		//At this point, the we established that the itemBinding will not output content
		return (getElement() != null) && (hasAttributes() || !isOptional());	//suppress optional empty elements (empty means: no content and no attributes)
	}
	
	public MapRepeater setMaxOccurs(int newMaxOccurs) {
		if (newMaxOccurs <= 1) {
			throw new Xb4jException("maxOccurs must be 1 or higher: "+newMaxOccurs);
		}
		getSemaphore().lock();
		try {
			validateMutability();
			this.maxOccurs = newMaxOccurs;
			return this;
		} finally {
			getSemaphore().unlock();
		}
	}
	
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer(128);
    	sb.append(getClass().getSimpleName()).append("[");
    	String separator = "";
    	QName element = getElement();
    	if (element != null) {
    		sb.append(separator).append("element=");
    		sb.append(element.toString());
    		separator = ",";
    	}
    	Class<?> mapType = getJavaType();
    	if (mapType != null) {
    		sb.append(separator).append("mapType=").append(mapType.getName());
    		separator = ",";
    	}
    	IBinding key = resolveBinding(keyBinding);
    	if (key != null) {
    		sb.append(separator).append("key=").append(key.toString());
    		separator = ",";
    	}
    	IBinding value = resolveBinding(valueBinding);
    	if (value != null) {
    		sb.append(separator).append("value=").append(value.toString());
    		separator = ",";
    	}
    	sb.append("]");
        return sb.toString();
    }

	@Override
	public void resolveReferences() {
		keyBinding.resolveReferences();
		valueBinding.resolveReferences();
	}
	
}
