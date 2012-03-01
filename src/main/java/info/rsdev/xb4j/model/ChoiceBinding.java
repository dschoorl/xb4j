package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.IChooser;
import info.rsdev.xb4j.model.java.InstanceOfChooser;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.InheritElementFetchStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * From the children in this group, only one can be choosen. However, a choice can be placed in a {@link SequenceBinding} and
 * be repeatable.
 * 
 * @author Dave Schoorl
 */
public class ChoiceBinding extends AbstractBinding {
	
	private Map<IChooser, IBinding> choices = new HashMap<IChooser, IBinding>();
	
	/**
	 * Create a new {@link ChoiceBinding}. No {@link IElementFetchStrategy} nor {@link IObjectFetchStrategy} are currently
	 * set (so this won't work)
	 */
	public ChoiceBinding() {
		setElementFetchStrategy(new InheritElementFetchStrategy(this));
	}
	
	/**
	 * Create new {@link ChoiceBinding}
	 * 
	 * @param element
	 * @param instantiator
	 */
	public ChoiceBinding(QName element, ICreator instantiator) { //we don't know what type to unmarshall to; that depends on the child xml.
		this(new DefaultElementFetchStrategy(element), instantiator);
	}
	
	public ChoiceBinding(IElementFetchStrategy elementFetcher, ICreator instantiator) {
		setElementFetchStrategy(elementFetcher);
		setObjectCreator(instantiator);
	}
	
	/**
	 * Create a new {@link AbstractBinding} where the javaType will be created with a {@link DefaultConstructor}
	 * 
	 * @param element
	 * @param javaType
	 */
	public ChoiceBinding(QName element, Class<?> javaType) {
		this(element, new DefaultConstructor(javaType));
	}
	
	public IBinding addChoice(IBinding choice, String fieldName, IChooser selector) {
		//Why not add getter/setter to IObjectFetchStrategy -- together with copy()-command
		FieldAccessProvider provider = new FieldAccessProvider(fieldName);
		choice.setGetter(provider);
		choice.setSetter(provider);
		
		return add(choice, selector);
	}
	
	/**
	 * Convenience method. The {@link IBinding choice} will be registered with this {@link ChoiceBinding}, and an {@link InstanceOfChooser} 
	 * will be generated for selection of this choice when marshalling. 
	 * @param choice
	 * @return
	 */
	public IBinding addChoice(IBinding choice) {
		Class<?> javaType = choice.getJavaType();
		if (javaType == null) {
			throw new Xb4jException(String.format("Cannot generate InstanceOfChooser, because the choice '%s' does not define" +
					"a Java type", choice));
		}
		return add(choice, new InstanceOfChooser(javaType));
	}
	
	private IBinding add(IBinding choice, IChooser selector) {
		this.choices.put(selector, choice);
		choice.setParent(this); //maintain bidirectional relationship
		return choice;
	}
	
	public SequenceBinding addChoice(SequenceBinding choice, String fieldName, IChooser selector) {
		addChoice((IBinding)choice, fieldName, selector);
		return choice;
	}
	
	private IBinding selectBinding(Object javaContext) {
		for (Entry<IChooser, IBinding> entry: this.choices.entrySet()) {
			if (entry.getKey().matches(javaContext)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
		IBinding selected = selectBinding(javaContext);
		selected.toXml(staxWriter, selected.getProperty(javaContext));	//how determine getter/setter to use
	}
	
	@Override
	public Object toJava(RecordAndPlaybackXMLStreamReader staxReader) throws XMLStreamException {
		Object result = null;
		IBinding resultBinding = null;
		for (IBinding candidate: this.choices.values()) {
			staxReader.startRecording(); //TODO: support multiple simultaneous recordings (markings)
			try {
				result = candidate.toJava(staxReader);
				if (result != null) {
					staxReader.stopAndWipeRecording();
					resultBinding = candidate; 
					break;	//TODO: check ambiguity?
				}
			} finally {
				staxReader.rewindAndPlayback();
			}
		}
		
		Object javaContext = null;
		if (resultBinding != null) {
		    javaContext = newInstance(); //get current object from stack???
		    resultBinding.setProperty(javaContext, result);
		}
		
		return javaContext;
	}
	
}
