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
import info.rsdev.xb4j.model.java.IChooser;
import info.rsdev.xb4j.model.java.InstanceOfChooser;
import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * From the children in this group, only one can be choosen. However, a choice can be placed in a {@link Sequence} and
 * be repeatable.
 * 
 * @author Dave Schoorl
 */
public class Choice extends AbstractSingleBinding {
	
	private List<IBinding> choices = new LinkedList<IBinding>();
	private List<IChooser> choosers = new LinkedList<IChooser>();
	
	/**
	 * Create a new {@link Choice}. No {@link IElementFetchStrategy} nor {@link IObjectFetchStrategy} are currently
	 * set (so this won't work)
	 */
	public Choice() {
		super(NoElementFetchStrategy.INSTANCE, null);
	}
	
    public Choice(QName element) {
		super(new DefaultElementFetchStrategy(element), null);
    }
    
	@Override
	public IBinding addAttribute(Attribute attribute, String fieldName) {
		throw new Xb4jException(String.format("You cannot add attributes to the Choice-binding itself; you must add it to " +
				"the options instead (%s)", attribute));
	}
	
	public IBinding addChoice(IBinding choice, String fieldName, IChooser selector) {
		//Why not add getter/setter to IObjectFetchStrategy -- together with copy()-command
		FieldAccessProvider provider = new FieldAccessProvider(fieldName);
		choice.setGetter(provider);
		choice.setSetter(provider);
		
		return addChoice(choice, selector);
	}
	
	/**
	 * Convenience method. The {@link IBinding choice} will be registered with this {@link Choice}, and an {@link InstanceOfChooser} 
	 * will be generated for selection of this choice when marshalling. 
	 * @param choice
	 * @return
	 */
	public <T extends IBinding> T addChoice(T choice) {
		Class<?> javaType = choice.getJavaType();
		if (javaType == null) {
			throw new Xb4jException(String.format("Cannot generate InstanceOfChooser, because the choice '%s' does not define " +
					"a Java type", choice));
		}
		return addChoice(choice, new InstanceOfChooser(javaType));
	}
	
	public <T extends IBinding> T addChoice(T choice, IChooser selector) {
		choices.add(choice);
		choosers.add(selector);
		choice.setParent(this); //maintain bidirectional relationship
		return choice;
	}
	
	private IBinding selectBinding(Object javaContext) {
		for (int i=0; i<choosers.size(); i++) {
			if (choosers.get(i).matches(javaContext)) {
				return choices.get(i);
			}
		}
		
		return null;
	}
	
	@Override
	public UnmarshallResult toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
        //check if we are on the right element -- consume the xml when needed
        QName expectedElement = getElement();
    	boolean startTagFound = false;
    	if (expectedElement != null) {
    		if (!staxReader.isAtElementStart(expectedElement)) {
	    		if (!isOptional()) {
	    			return UnmarshallResult.newMissingElement(this);
	    		}
    		} else {
    			startTagFound = true;
    		}
    	}
        
        //Should we start recording to return to this element when necessary - currently this is responsibility of choices
        boolean choiceFound = false;
        UnmarshallResult result = null;
		for (IBinding candidate: choices) {
			result = candidate.toJava(staxReader, getProperty(javaContext));
			if (result.isUnmarshallSuccessful()) {
				choiceFound = true;
				if (result.mustHandleUnmarshalledObject()) {
					if (setProperty(javaContext, result.getUnmarshalledObject())) {
						result.setHandled();
					}
				}
				break;	//TODO: check ambiguity?
			}
		}
		
		if (!choiceFound && !isOptional()) {
			return new UnmarshallResult(ErrorCodes.MISSING_MANDATORY_ERROR, String.format("No matching option found in xml for mandatory %s", this), this);
		}
		
        if ((expectedElement != null) && !staxReader.isAtElementEnd(expectedElement) && startTagFound) {
    		String encountered =  (staxReader.isAtElement()?String.format("(%s)", staxReader.getName()):"");
    		throw new Xb4jException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", expectedElement,
    				staxReader.getEventName(), encountered));
        }
		return result;
	}
	
	@Override
	public void elementToXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
		if (!generatesOutput(javaContext)) { return; }
		
        //mixed content is not yet supported -- there are either child elements or there is content
        QName element = getElement();
        javaContext = getProperty(javaContext);
        IBinding selected = selectBinding(javaContext);
        boolean isEmptyElement = selected == null;
        if (element != null) {
            staxWriter.writeElement(element, getAttributes(), isEmptyElement);
        }
        
        if (selected != null) {
            selected.toXml(staxWriter, javaContext);
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
	}
	
	@Override
	public boolean generatesOutput(Object javaContext) {
        javaContext = getProperty(javaContext);
		if (javaContext != null) {
			IBinding child = selectBinding(javaContext);
			if ((child != null) && child.generatesOutput(javaContext)) {
				return true;
			}
		}
		
		//At this point, there is no childBinding to output content
		return (getElement() != null) && (hasAttributes() || !isOptional());	//suppress optional empty elements (empty means: no content and no attributes)
	}
	
}
