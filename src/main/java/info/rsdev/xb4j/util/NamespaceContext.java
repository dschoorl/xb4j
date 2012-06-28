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
package info.rsdev.xb4j.util;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * A helper class to keep track of the namespaces that are known and available within an xml element tree that is being streamed
 * 
 * @author dschoorl
 */
public class NamespaceContext {
	
	private static final String DEFAULT_NS_PREFIX = "ns";
	
    private int generatedPrefixCounter = 0;
	
    private Deque<ContextEntry> context = new LinkedList<ContextEntry>();
    
    public String registerNamespace(QName element) {
		if (element == null) {
			throw new NullPointerException("QName of element cannot be null");
		}
    	return registerNamespace(element, element.getNamespaceURI(), element.getPrefix());
    }
    
	/**
	 * Register the definition of the given prefix and namespace on the element. The element is assumed to be the current
	 * element that is being handled. Multiple namespaces can be registered on an element.
	 *  
	 * @param element
	 * @param namespaceUri
	 * @param prefix Optional prefix for the namespace. When no prefix is supplied
	 * @return the prefix that this namespaceUri is registered under and that should be used to qualify local names 
	 */
	public String registerNamespace(QName element, String namespaceUri, String prefix) {
		if (element == null) {
			throw new NullPointerException("QName of element cannot be null");
		}
		if ((namespaceUri == null) || (namespaceUri.equals(XMLConstants.NULL_NS_URI))) {
			return XMLConstants.DEFAULT_NS_PREFIX;
		}
		
		if (isRegistered(namespaceUri)) {
			return getPrefix(namespaceUri);
		}
		
		if (prefix == null) {
			prefix = generatePrefix();
		}
		
		ContextEntry contextEntry = getOrCreateContextEntry(element);
		contextEntry.addNamespace(namespaceUri, prefix);
		return prefix;
	}
	
	public void unregisterNamespacesFor(QName element) {
		if (element == null) {
			throw new NullPointerException("QName of element cannot be null");
		}
		//assume wellformed xml
		if (!context.isEmpty() && (context.peek().getElement().equals(element))) {
			context.pop();
		}
	}
	
	public String getPrefix(String namespaceUri) {
		Iterator<ContextEntry> it = context.iterator();
		while (it.hasNext()) {
			ContextEntry entry = it.next();
			if (entry.isRegistered(namespaceUri)) {
				return entry.getPrefix(namespaceUri);
			}
		}
		return null;
	}
	
	public boolean isRegistered(String namespaceUri) {
		Iterator<ContextEntry> it = context.iterator();
		while (it.hasNext()) {
			if (it.next().isRegistered(namespaceUri)) {
				return true;
			}
		}
		return false;
	}
	
	public void clear() {
		generatedPrefixCounter = 0;
		context.clear();
	}
	
	private ContextEntry getOrCreateContextEntry(QName element) {
		ContextEntry entry = null;
		if (!context.isEmpty() && (context.peek().getElement().equals(element))) {
			entry = context.peekLast();
		} else {
			//TODO: check if the element exists on a lower level and if so, disallow... or allow?
			entry = new ContextEntry(element);
			context.push(entry);
		}
		return entry;
	}
	
	private String generatePrefix() {
		return DEFAULT_NS_PREFIX + generatedPrefixCounter++;
	}
    
    private static class ContextEntry {
    	
    	private final QName element;
    	
        /**
         * keys are namespaceUri's and values are prefixes
         */
        private HashMap<String, String> namespacesInContext = new HashMap<String, String>(4);
        
        private ContextEntry(QName element) {
        	this.element = element;
        }
        
        public boolean isRegistered(String namespaceUri) {
			return namespacesInContext.containsKey(namespaceUri);
		}
        
        public String getPrefix(String namespaceUri) {
        	return namespacesInContext.get(namespaceUri);
        }

		private QName getElement() {
        	return element;
        }
        
        private void addNamespace(String namespaceUri, String prefix) {
        	if (!namespacesInContext.containsKey(namespaceUri)) {
        		namespacesInContext.put(namespaceUri, prefix);
        	}
        }
    } 

}
