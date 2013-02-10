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
package info.rsdev.xb4j.model.java.constructor;

import info.rsdev.xb4j.exceptions.Xb4jException;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader.Marker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

/**
 * This implementation of {@link ICreator} is capable of calling a constructor that takes arguments. The arguments are
 * read from the xml stream and can come from xml attributes as well as xml elements.
 * 
 * @author Dave Schoorl
 */
public class ArgsConstructor implements ICreator, XMLStreamConstants {
	
	private final Class<?> javaType;
	
	private QName[] pathNames = null;
	
	public ArgsConstructor(Class<?> javaType, QName... pathNames) {
		if ((pathNames == null) || (pathNames.length == 0)) {
			throw new NullPointerException("You must provide at least one xml childElement or attribute that " +
					"yields a Java constructor argument, otherwise, use the DefaultConstructor");
		}
		this.javaType = javaType;
		this.pathNames = pathNames;
	}
	
	@Override
	public Object newInstance(RecordAndPlaybackXMLStreamReader staxReader) {
		try {
			Object[] argumentValues = readArguments(staxReader);
			Constructor<?> constructorToUse = getConstructor(argumentValues);
			return constructorToUse.newInstance(argumentValues);
		} catch (Exception e) {
			throw new Xb4jException(String.format("Could not create %s", javaType), e);
		}
	}
	
	private Constructor<?> getConstructor(Object[] argumentValues) {
		Constructor<?>[] candidates = javaType.getDeclaredConstructors();
		LinkedList<Constructor<?>> candidatesWithMatchingArgs = new LinkedList<Constructor<?>>();
		Class<?>[] argumentTypes = asArgumentTypes(argumentValues);
		for (Constructor<?> candidate: candidates) {
			Class<?>[] constructorTypes = candidate.getParameterTypes();
			if (constructorTypes.length == argumentTypes.length) {
				boolean allArgumentsMatch = true;
				for (int i=0; i<argumentValues.length && allArgumentsMatch; i++) {
					if (argumentTypes[i] == null) {
						allArgumentsMatch = !constructorTypes[i].isPrimitive();
						continue;	//when a non-primitive is expected and null is passed, we consider that always a positive match
					}
					allArgumentsMatch = constructorTypes[i].isAssignableFrom(argumentTypes[i]);
				}
				if (allArgumentsMatch) {
					candidatesWithMatchingArgs.add(candidate);
				}
			}
		}
		
		if (candidatesWithMatchingArgs.isEmpty()) {
			throw new Xb4jException(String.format("No constructor found on %s that could accept these arguments: %s", 
					javaType, Arrays.toString(argumentValues)));
		}
		if (candidatesWithMatchingArgs.size() > 1) {
			throw new Xb4jException(String.format("Found %d constructors on %s that could accept these arguments: %s, don't " +
					"know which one to use", candidatesWithMatchingArgs.size(), javaType, Arrays.toString(argumentValues)));
		}
		
		Constructor<?> match = candidatesWithMatchingArgs.getFirst();
		if (!Modifier.isPublic(((Member)match).getModifiers()) || !Modifier.isPublic(((Member)match).getDeclaringClass().getModifiers())) {
			match.setAccessible(true);
		}
		return match;
	}
	
	private Class<?>[] asArgumentTypes(Object[] argumentValues) {
		Class<?>[] types = new Class<?>[argumentValues.length];
		for (int i=0; i<argumentValues.length; i++) {
			if (argumentValues[i] == null) {
				types[i] = null;
			} else {
				types[i] = argumentValues[i].getClass();
			}
		}
		return types;
	}
	
	/**
	 * A first simple solution. The permanent solution will probably be integrated into the Bindings, as soon as we
	 * need to read something else than Strings.
	 * 
	 * @param staxReader
	 * @return
	 * @throws XMLStreamException
	 */
	private Object[] readArguments(RecordAndPlaybackXMLStreamReader staxReader) throws XMLStreamException {
		if (!staxReader.isAtElement()) {
			throw new IllegalStateException("Not at the start of an element; cannot read constructor arguments");
		}
		Object[] arguments = new Object[pathNames.length];
		QName currentName = staxReader.getName();
		for (int i=0; i<pathNames.length; i++) {
			Marker currentLocation = staxReader.startRecording();
			String argumentValue = null;
			if (moveToChild(staxReader, pathNames[i], currentName)) {
				argumentValue = staxReader.getElementText();
			}
			arguments[i] = argumentValue;
			staxReader.rewindAndPlayback(currentLocation);
		}
		return arguments;
	}
	
	private boolean moveToChild(RecordAndPlaybackXMLStreamReader staxReader, QName childElement, QName parentElement) throws XMLStreamException {
		int event = -1;
		while ((event = staxReader.nextTag()) != END_DOCUMENT) {
			if ((event == END_ELEMENT) && staxReader.getName().equals(parentElement)) {
				return false;	//child is not found within the parent xml
			}
			if (event == START_ELEMENT) { 
				if (staxReader.getName().equals(childElement)) {
					return true;
//				} else {
//					staxReader.getElementText();
				}
			}
		}
		return false;	//child not in remainder of xml document
	}
	
	@Override
	public Class<?> getJavaType() {
		return this.javaType;
	}
	
}
