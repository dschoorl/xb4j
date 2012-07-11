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
import info.rsdev.xb4j.exceptions.Xb4jUnmarshallException;
import info.rsdev.xb4j.model.bindings.chooser.ContextInstanceOf;
import info.rsdev.xb4j.model.bindings.chooser.IChooser;
import info.rsdev.xb4j.model.java.JavaContext;
import info.rsdev.xb4j.model.java.accessor.FieldAccessor;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.SimplifiedXMLStreamWriter;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * From the children in this group, only one can be choosen. However, a choice can be placed in a {@link Sequence} and
 * be repeatable.
 * 
 * @author Dave Schoorl
 */
public class Choice extends AbstractSingleBinding {
	
	private Logger logger = LoggerFactory.getLogger(Choice.class);
	
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
	public IBinding addAttribute(IAttribute attribute, String fieldName) {
		throw new Xb4jException(String.format("You cannot add attributes to the Choice-binding itself; you must add it to " +
				"the options instead (%s)", attribute));
	}
	
	public IBinding addChoice(IBinding choice, String fieldName, IChooser selector) {
		//Why not add getter/setter to IObjectFetchStrategy -- together with copy()-command
		FieldAccessor provider = new FieldAccessor(fieldName);
		choice.setGetter(provider);
		choice.setSetter(provider);
		
		return addChoice(choice, selector);
	}
	
	/**
	 * Convenience method. The {@link IBinding choice} will be registered with this {@link Choice}, and an {@link ContextInstanceOf} 
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
		return addChoice(choice, new ContextInstanceOf(javaType));
	}
	
	public <T extends IBinding> T addChoice(T choice, IChooser selector) {
		choices.add(choice);
		choosers.add(selector);
		choice.setParent(this); //maintain bidirectional relationship
		return choice;
	}
	
	private IBinding selectBinding(JavaContext javaContext) {
		for (int i=0; i<choosers.size(); i++) {
			IChooser candidate = choosers.get(i);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Marshal] Trying option %d (%s) of %s", i+1, candidate, this));
			}
			if (candidate.matches(javaContext)) {
				IBinding selectedBinding = choices.get(i);
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("[Marshal] Option %d (%s) selected, taking route: %s", i+1, candidate, selectedBinding));
				}
				return selectedBinding;
			} else if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Marshal] Option %d (%s) is not a match for %s:", i+1, candidate, this));
			}
		}
		
		return null;
	}
	
	@Override
	public UnmarshallResult unmarshall(RecordAndPlaybackXMLStreamReader staxReader, JavaContext javaContext) throws XMLStreamException {
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
        int optionCounter = 1;
		for (IBinding candidate: choices) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Unmarshal] Trying option %d of %s", optionCounter, this));
			}
			result = candidate.toJava(staxReader, getProperty(javaContext));
			if (result.isUnmarshallSuccessful()) {
				choiceFound = true;
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("[Unmarshal] Option %d for %s works out fine -- won't look any further", optionCounter, this));
				}
				if (result.mustHandleUnmarshalledObject()) {
					if (setProperty(javaContext, result.getUnmarshalledObject())) {
						result.setHandled();
					}
				}
				break;	//TODO: check ambiguity?
			} else if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Unmarshal] Option %d is not a match for %s: %s", optionCounter, this, result.getErrorMessage()));
			}
			optionCounter++;
		}
		
		if (!choiceFound && !isOptional()) {
			return new UnmarshallResult(ErrorCodes.MISSING_MANDATORY_ERROR, String.format("No matching option found in xml for mandatory %s", this), this);
		}
		
        if ((expectedElement != null) && !staxReader.isAtElementEnd(expectedElement) && startTagFound) {
    		String encountered =  (staxReader.isAtElement()?String.format("(%s)", staxReader.getName()):"");
    		throw new Xb4jUnmarshallException(String.format("Malformed xml; expected end tag </%s>, but encountered a %s %s", expectedElement,
    				staxReader.getEventName(), encountered), this);
        }
		return result;
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, JavaContext javaContext) throws XMLStreamException {
		if (!generatesOutput(javaContext)) { return; }
		
        //mixed content is not yet supported -- there are either child elements or there is content
        QName element = getElement();
        javaContext = getProperty(javaContext);
        IBinding selected = selectBinding(javaContext);
        boolean isEmptyElement = selected == null;
        if (element != null) {
            staxWriter.writeElement(element, isEmptyElement);
            attributesToXml(staxWriter, javaContext);
        }
        
        if (selected != null) {
            selected.toXml(staxWriter, javaContext);
        }
        
        if (!isEmptyElement && (element != null)) {
            staxWriter.closeElement(element);
        }
	}
	
	@Override
	public boolean generatesOutput(JavaContext javaContext) {
		//a quick check: when the element is not optional or it has attributes, generate output, regardless if the element has content
		if ((getElement() != null) && (hasAttributes() || !isOptional())) {
			return true;
		}
		
		//check if the element has any contents
        javaContext = getProperty(javaContext);
		if (javaContext != null) {
			IBinding child = selectBinding(javaContext);
			if ((child != null) && child.generatesOutput(javaContext)) {
				return true;
			}
		}
		
		return false;
	}
	
}
