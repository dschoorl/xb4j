package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.action.IUnmarshallingAction;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This binding type allows you to swallow an element (tree) in the xml stream. The containing element tags are recognized and 
 * the entire tree (if any) is ignored. 
 * 
 * @author dschoorl
 */
public class Ignore implements IBinding {
	
	private QName element = null;
	
    private IBinding parent = null;
    
    private boolean isOptional = false; //by default, everything is mandatory, unless explicitly made optional
    
    private IUnmarshallingAction actionAfterUnmarshalling = null;
    
    public Ignore(QName element) {
    	this(element, false);
    }
    
    public Ignore(QName element, boolean isOptional) {
    	if (element == null) {
    		throw new NullPointerException("Element cannot be null");
    	}
    	this.element = element;
    	this.isOptional = isOptional;
    }
    
	@Override
	public UnmarshallResult toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        QName expectedElement = getElement();
		if (!staxReader.isAtElementStart(expectedElement)) {
    		if (isOptional()) {
                return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
    		} else {
                return UnmarshallResult.newMissingElement(this);
    		}
		}
		
		//start tag is found: consume and ignore xml stream until end tag
		staxReader.skipToElementEnd();
        
		if (actionAfterUnmarshalling != null) {
			actionAfterUnmarshalling.execute(javaContext);
		}
		
		return UnmarshallResult.NO_RESULT;
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
		//do nothing, i.o.w: ignore!
	}
	
	@Override
	public boolean generatesOutput(Object javaContext) {
		return false;
	}

	@Override
	public void setParent(IBinding parent) {
    	if (parent == null) {
    		throw new NullPointerException("Parent IBinding cannot be null");
    	}
    	if ((this.parent != null) && !this.parent.equals(parent)) {
    	    throw new IllegalArgumentException(String.format("This binding '%s' is already part of a binding tree.", this));
    	}
    	this.parent = parent;
	}

	@Override
	public IBinding getParent() {
    	return this.parent;
	}

	@Override
	public QName getElement() {
        return this.element;
	}

	@Override
	public IBinding addAttribute(Attribute attribute, String fieldName) {
		return this;
	}

	@Override
	public Class<?> getJavaType() {
		return null;
	}

	@Override
	public Object newInstance() {
		return null;
	}

	@Override
	public Object getProperty(Object contextInstance) {
		return null;
	}

	@Override
	public boolean setProperty(Object contextInstance, Object propertyValue) {
		return false;
	}

	@Override
	public IBinding setGetter(IGetter getter) {
		return this;
	}

	@Override
	public IBinding setSetter(ISetter setter) {
		return this;
	}

	@Override
	public boolean isOptional() {
		return this.isOptional;
	}

	@Override
	public IBinding setOptional(boolean isOptional) {
		this.isOptional = isOptional;
		return this;
	}

    @Override
    public IBinding setActionAfterUnmarshalling(IUnmarshallingAction action) {
    	if (action == null) {
    		throw new NullPointerException("You must provide an IAction implementation");
    	}
    	this.actionAfterUnmarshalling = action;
    	return this;
    }
    
	@Override
	public String getPath() {
    	List<String> pathToRoot = new ArrayList<String>();
    	IBinding binding = this;
    	while (binding != null) {
    		String bindingType = binding.getClass().getSimpleName();
    		if (binding.getElement() != null) {
    			bindingType = bindingType.concat("<").concat(binding.getElement().getLocalPart()).concat(">");
    		}
   			pathToRoot.add(bindingType);
    		binding = binding.getParent();
    	}
    	
    	StringBuffer sb = new StringBuffer();
    	for (int i=pathToRoot.size() - 1; i >= 0; i--) {
    		sb.append("/").append(pathToRoot.get(i));
    	}
    	return sb.toString();
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), this.element);
	}
	
}
