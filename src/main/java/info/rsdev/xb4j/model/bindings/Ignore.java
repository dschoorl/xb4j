package info.rsdev.xb4j.model.bindings;

import info.rsdev.xb4j.exceptions.Xb4jMutabilityException;
import info.rsdev.xb4j.model.bindings.action.ActionManager;
import info.rsdev.xb4j.model.bindings.action.IPhasedAction;
import info.rsdev.xb4j.model.bindings.action.IPhasedAction.ExecutionPhase;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.IGetter;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.model.java.constructor.IJavaArgument;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This binding type allows you to swallow an element (tree) in the xml stream. The containing element tags are recognized and 
 * the entire tree (if any) is ignored. This class supports repeated elements.
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
		if (!staxReader.isNextAnElementStart(expectedElement)) {
    		if (isOptional()) {
                return UnmarshallResult.MISSING_OPTIONAL_ELEMENT;
    		} else {
                return UnmarshallResult.newMissingElement(this);
    		}
		}
		
		this.actionManager.executeActions(ExecutionPhase.BEFORE_UNMARSHALLING, javaContext);

		//start tag is found: consume and ignore xml stream until end tag and do so for all repeating elements (if any)
		while(staxReader.skipToElementEnd()) {
		    if (!staxReader.isNextAnElementStart(expectedElement)) {
		        break;
		    }
		}
        
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
    	
    	ISemaphore topLevel = getSemaphore();
		topLevel.lock();
		try {
			validateMutability();
	    	this.parent = parent;
		} finally {
			topLevel.unlock();
		}
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
		getSemaphore().lock();
		try {
			validateMutability();
			return this;
		} finally {
			getSemaphore().unlock();
		}
	}
	
	@Override
	public IBinding addAttribute(IAttribute attribute, IGetter getter, ISetter setter) {
		getSemaphore().lock();
		try {
			validateMutability();
			return this;
		} finally {
			getSemaphore().unlock();
		}
	}

	@Override
	public Class<?> getJavaType() {
		return null;
	}

	@Override
	public JavaContext newInstance(RecordAndPlaybackXMLStreamReader staxReader, JavaContext currentContext) {
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
		getSemaphore().lock();
		try {
			validateMutability();
			return this;
		} finally {
			getSemaphore().unlock();
		}
	}

	@Override
	public IBinding setSetter(ISetter setter) {
		getSemaphore().lock();
		try {
			validateMutability();
			return this;
		} finally {
			getSemaphore().unlock();
		}
	}

	@Override
	public boolean isOptional() {
		return this.isOptional;
	}

	@Override
	public IBinding setOptional(boolean isOptional) {
		getSemaphore().lock();
		try {
			validateMutability();
			this.isOptional = isOptional;
			return this;
		} finally {
			getSemaphore().unlock();
		}
	}

    @Override
    public IBinding addAction(IPhasedAction action) {
    	if (action == null) {
    		throw new NullPointerException("You must provide an IPhasedAction implementation");
    	}
		getSemaphore().lock();
		try {
			validateMutability();
	    	this.actionManager.addAction(action);
	    	return this;
		} finally {
			getSemaphore().unlock();
		}
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

	@Override
	public ISemaphore getSemaphore() {
        IBinding modelAwareBinding = this;
        while (modelAwareBinding.getParent() != null) {
        	modelAwareBinding = modelAwareBinding.getParent();
        }
        
        if (!(modelAwareBinding instanceof IModelAware)) {
        	return NullSafeSemaphore.INSTANCE;	//provide nullsafe lock/unlock utility for cases where the binding is not yet part of a full tree
        }
        return (ISemaphore)modelAwareBinding;
	}

	@Override
	public IModelAware getModelAware() {
        IBinding modelAwareBinding = this;
        while (modelAwareBinding.getParent() != null) {
        	modelAwareBinding = modelAwareBinding.getParent();
        }
        
        if (!(modelAwareBinding instanceof IModelAware)) {
        	return NullSafeModelAware.INSTANCE;	//provide nullsafe utility for cases where the binding is not yet part of a full tree
        }
        return (IModelAware)modelAwareBinding;
	}

	@Override
	public void validateMutability() {
		ISemaphore semaphore = getSemaphore();
		semaphore.lock();
		try {
			IModelAware topLevel = getModelAware();
    		if (topLevel.isImmutable()) {
    			throw new Xb4jMutabilityException(String.format("Cannot change (parts of the) immutable binding %s", semaphore));
    		}
		} finally {
			semaphore.unlock();
		}
	}

	@Override
	public void resolveReferences() {
		//there are no child bindings to resolve references for... nothing to do
	}

    @Override
    public IJavaArgument findArgumentBindingOrAttribute(QName argumentQName) {
        //this implementation ignores all xml as of this point; therfore, there no IJavaArgument in this xml tree is read
        return null;
    }
	
}
