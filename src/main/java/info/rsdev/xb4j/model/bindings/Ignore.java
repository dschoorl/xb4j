package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.model.bindings.action.ActionManager;
import info.rsdev.xb4j.model.bindings.action.IPhasedAction;
import info.rsdev.xb4j.model.bindings.action.IPhasedAction.ExecutionPhase;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
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
	
	private ActionManager actionManager = null;
	
	private QName element = null;
	
    private IBinding parent = null;
    
    private boolean isOptional = false; //by default, everything is mandatory, unless explicitly made optional
    
    public Ignore(QName element) {
    	this(element, false);
    }
    
    public Ignore(QName element, boolean isOptional) {
    	if (element == null) {
    		throw new NullPointerException("Element cannot be null");
    	}
    	this.element = element;
    	this.isOptional = isOptional;
    	this.actionManager = new ActionManager();
    }
    
	@Override
	public UnmarshallResult toJava(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
        QName expectedElement = getElement();
		if (!staxReader.isAtElementStart(expectedElement)) {
    		if (isOptional()) {
                return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
    		} else {
                return UnmarshallResult.newMissingElement(this);
    		}
		}
		
		this.actionManager.executeActions(ExecutionPhase.BEFORE_UNMARSHALLING, javaContext);

		//start tag is found: consume and ignore xml stream until end tag
		staxReader.skipToElementEnd();
        
		this.actionManager.executeActions(ExecutionPhase.AFTER_UNMARSHALLING, javaContext);
		
		return UnmarshallResult.NO_RESULT;
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
		this.actionManager.executeActions(ExecutionPhase.BEFORE_MARSHALLING, javaContext);
		//no marshalling to be done; i.o.w: ignore!
		this.actionManager.executeActions(ExecutionPhase.AFTER_MARSHALLING, javaContext);
	}
	
	@Override
	public boolean generatesOutput(JavaContext javaContext) {
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
	public IBinding addAttribute(IAttribute attribute, String fieldName) {
		return this;
	}
	
	@Override
	public IBinding addAttribute(IAttribute attribute, IGetter getter, ISetter setter) {
		return this;
	}

	@Override
	public Class<?> getJavaType() {
		return null;
	}

	@Override
	public JavaContext newInstance(JavaContext currentContext) {
		JavaContext newContext = currentContext.newContext(null);
		this.actionManager.executeActions(ExecutionPhase.AFTER_OBJECT_CREATION, newContext);
		return newContext;
	}

	@Override
	public JavaContext getProperty(JavaContext javaContext) {
		return javaContext;
	}

	@Override
	public boolean setProperty(JavaContext contextInstance, Object propertyValue) {
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
    public IBinding addAction(IPhasedAction action) {
    	if (action == null) {
    		throw new NullPointerException("You must provide an IPhasedAction implementation");
    	}
    	this.actionManager.addAction(action);
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
