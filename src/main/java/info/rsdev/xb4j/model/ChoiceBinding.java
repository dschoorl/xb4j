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
package info.rsdev.xb4j.model;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.model.java.IChooser;
import info.rsdev.xb4j.model.java.InstanceOfChooser;
import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

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
public class ChoiceBinding extends AbstractSingleBinding {
	
	private Map<IChooser, IBindingBase> choices = new HashMap<IChooser, IBindingBase>();
	
	/**
	 * Create a new {@link ChoiceBinding}. No {@link IElementFetchStrategy} nor {@link IObjectFetchStrategy} are currently
	 * set (so this won't work)
	 */
	public ChoiceBinding() {
		setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
	}
	
    public ChoiceBinding(QName element) {
        setElementFetchStrategy(new DefaultElementFetchStrategy(element));
    }
    
	public IBindingBase addChoice(IBindingBase choice, String fieldName, IChooser selector) {
		//Why not add getter/setter to IObjectFetchStrategy -- together with copy()-command
		FieldAccessProvider provider = new FieldAccessProvider(fieldName);
		choice.setGetter(provider);
		choice.setSetter(provider);
		
		return addChoice(choice, selector);
	}
	
	/**
	 * Convenience method. The {@link IBindingBase choice} will be registered with this {@link ChoiceBinding}, and an {@link InstanceOfChooser} 
	 * will be generated for selection of this choice when marshalling. 
	 * @param choice
	 * @return
	 */
	public <T extends IBindingBase> T addChoice(T choice) {
		Class<?> javaType = choice.getJavaType();
		if (javaType == null) {
			throw new Xb4jException(String.format("Cannot generate InstanceOfChooser, because the choice '%s' does not define" +
					"a Java type", choice));
		}
		return addChoice(choice, new InstanceOfChooser(javaType));
	}
	
	public <T extends IBindingBase> T addChoice(T choice, IChooser selector) {
		this.choices.put(selector, choice);
		choice.setParent(this); //maintain bidirectional relationship
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
        QName element = getElement();
        
        //mixed content is not yet supported -- there are either child elements or there is content
        javaContext = getProperty(javaContext);
        IBindingBase selected = selectBinding(javaContext);
        boolean isEmptyElement = selected == null;
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
        }
        
        if (selected != null) {
            selected.toXml(staxWriter, javaContext);
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
	}
	
	@Override
	public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();
        if ((expectedElement != null) && !staxReader.isAtElementStart(expectedElement)) {
            return null;
        }
        
        //Should we start recording to return to this element when necessary - currently this is responsibility of choices
		Object result = null;
		for (IBindingBase candidate: this.choices.values()) {
			result = candidate.toJava(staxReader, getProperty(javaContext));
			if (result != null) {
			    setProperty(javaContext, result);
				break;	//TODO: check ambiguity?
			}
		}
		
        if ((expectedElement != null) && !staxReader.isAtElementEnd(expectedElement)) {
            throw new Xb4jException("No End tag encountered: ".concat(expectedElement.toString()));
        }
        
		return result;
	}
	
}
