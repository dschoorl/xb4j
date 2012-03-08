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

import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.FetchFromParentStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * <p>This class is a special {@link ElementBinding}; it can only contain a single {@link ComplexTypeBinding}. The 
 * ComplexTypeBinding can be anonymous or a type reference. as it's child so that a binding can be re-used in multiple 
 * {@link RootBinding} hierarchies.</p>
 * 
 * TODO: setChild methods from ElementBinding are available to the outside world: solve this!
 * 
 * @author Dave Schoorl
 */
public class ComplexTypeReference extends ElementBinding {

    private String identifier = null;

    private String namespaceUri = null;
    
    public ComplexTypeReference(String identifier, String namespaceUri) {
        setElementFetchStrategy(NoElementFetchStrategy.INSTANCE);
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
    }

    public ComplexTypeReference(QName element, Class<?> javaType, String identifier, String namespaceUri) {
        super(element, javaType);
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
    }

    public ComplexTypeReference(Class<?> javaType, String identifier, String namespaceUri) {
        super(javaType);
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
    }
    
    public ComplexTypeReference(QName element, String identifier, String namespaceUri) {
        super(element);
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
    }

    /**
     * Create a ComplexTypeReference for an anonymous ComplexType (not registered with {@link BindingModel}.
     * This method is not to be called directly, only by the framework to establish an anonymous complextype 
     * mechanism.
     * 
     * @param element
     * @param referencedBinding
     */
    ComplexTypeReference(QName element, ComplexTypeBinding referencedBinding) {
    	//TODO: simplify -> can we not skip ComplexTypeReference when dealing with anonymous type (just use only a ComplexTypeBinding)
        if (referencedBinding == null) {
            throw new NullPointerException("ComplexTypeBinding cannot be null");
        }
        setChild(referencedBinding);
        setElementFetchStrategy(new DefaultElementFetchStrategy(element));	//anonymous type element fetch strategy
    }

    private void setIdentifier(String newIdentifier) {
        if (newIdentifier == null) {
            throw new NullPointerException("Identifier cannot be null");
        }
        this.identifier = newIdentifier;
    }
    
    private void setNamespaceUri(String newNamespaceUri) {
        if (newNamespaceUri == null) {
            newNamespaceUri = XMLConstants.NULL_NS_URI;
        }
        this.namespaceUri = newNamespaceUri;
    }
    
    public ComplexTypeBinding getChildBinding() {
        ComplexTypeBinding referenced = (ComplexTypeBinding)super.getChildBinding();
        if (referenced == null) {
            IModelAware root = getModelAware();
            ComplexTypeBinding complexType = root.getModel().getComplexType(identifier, namespaceUri);
            referenced = complexType.copy();    //copy without parent
            setChild(referenced);
            referenced.setElementFetchStrategy(new FetchFromParentStrategy(referenced));	//referenced type element fetch strategy
        }
        return referenced;
    }
    
    @Override
    public String toString() {
        if (this.identifier != null) {
            String fqClassName = getClass().getName();
            int dotIndex = Math.max(0, fqClassName.lastIndexOf('.') + 1);
            return String.format("%s[references complexType: identifier=%s, namespace=%s]", fqClassName.substring(dotIndex), identifier, namespaceUri);
        } else {
            return super.toString();
        }
    }
    
}
