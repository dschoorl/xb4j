package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.IChooser;
import info.rsdev.xb4j.model.java.InstanceOfChooser;
import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.FetchFromParentStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

/**
 * From the children in this group, only one can be choosen. However, a choice can be placed in a {@link SequenceBinding} and
 * be repeatable.
 * 
 * @author Dave Schoorl
 */
public class ChoiceBinding extends AbstractSingleBinding {
	
	private Map<IChooser, IBindingBase> choices = new HashMap<IChooser, IBindingBase>();
	
	/**
	 * Create a new {@link ChoiceBinding}. No {@link IElementFetchStrategy} nor {@link IObjectFetchStrategy} are currently
	 * set (so this won't work)
	 */
	public ChoiceBinding() {
		setElementFetchStrategy(new FetchFromParentStrategy(this));
	}
	
	public IBindingBase addChoice(IBindingBase choice, String fieldName, IChooser selector) {
		//Why not add getter/setter to IObjectFetchStrategy -- together with copy()-command
		FieldAccessProvider provider = new FieldAccessProvider(fieldName);
		choice.setGetter(provider);
		choice.setSetter(provider);
		
		return add(choice, selector);
	}
	
	/**
	 * Convenience method. The {@link IBindingBase choice} will be registered with this {@link ChoiceBinding}, and an {@link InstanceOfChooser} 
	 * will be generated for selection of this choice when marshalling. 
	 * @param choice
	 * @return
	 */
	public IBindingBase addChoice(IBindingBase choice) {
		Class<?> javaType = choice.getJavaType();
		if (javaType == null) {
			throw new Xb4jException(String.format("Cannot generate InstanceOfChooser, because the choice '%s' does not define" +
					"a Java type", choice));
		}
		return add(choice, new InstanceOfChooser(javaType));
	}
	
	private IBindingBase add(IBindingBase choice, IChooser selector) {
		this.choices.put(selector, choice);
		choice.setParent(this); //maintain bidirectional relationship
		return choice;
	}
	
	public SequenceBinding addChoice(SequenceBinding choice, String fieldName, IChooser selector) {
		addChoice((IBindingBase)choice, fieldName, selector);
		return choice;
	}
	
	private IBindingBase selectBinding(Object javaContext) {
		for (Entry<IChooser, IBindingBase> entry: this.choices.entrySet()) {
			if (entry.getKey().matches(javaContext)) {
				return entry.getValue();
			}
		}
		throw new Xb4jException(String.format("%s could not select a choice for java context value %s", this, javaContext));
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
		IBindingBase selected = selectBinding(javaContext);
		selected.toXml(staxWriter, javaContext);	//how determine getter/setter to use
	}
	
	@Override
	public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
		Object newJavaContext = newInstance();
		Object result = null;
		IBindingBase resultBinding = null;
		for (IBindingBase candidate: this.choices.values()) {
			staxReader.startRecording(); //TODO: support multiple simultaneous recordings (markings)
			try {
				result = candidate.toJava(staxReader, select(javaContext, newJavaContext));
				if (result != null) {
					staxReader.stopAndWipeRecording();
					resultBinding = candidate; 
					break;	//TODO: check ambiguity?
				}
			} finally {
				staxReader.rewindAndPlayback();
			}
		}
		
		if (resultBinding != null) {
		    javaContext = select(javaContext, newJavaContext);
		    resultBinding.setProperty(javaContext, result);
		}
		
		return newJavaContext;
	}
	
}
