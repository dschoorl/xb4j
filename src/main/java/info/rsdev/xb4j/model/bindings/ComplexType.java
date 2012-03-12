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

import info.rsdev.xb4j.model.BindingModel;
import info.rsdev.xb4j.model.java.accessor.FieldAccessProvider;
import info.rsdev.xb4j.model.util.RecordAndPlaybackXMLStreamReader;
import info.rsdev.xb4j.model.util.SimplifiedXMLStreamWriter;
import info.rsdev.xb4j.model.xml.DefaultElementFetchStrategy;
import info.rsdev.xb4j.model.xml.FetchFromParentStrategy;
import info.rsdev.xb4j.model.xml.IElementFetchStrategy;
import info.rsdev.xb4j.model.xml.NoElementFetchStrategy;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * <p>This binding can be used as an anonymous type in a RootBinding hierarchy, or it can be
 * registered as a type with a {@link BindingModel}, so that the definition can be reused. Reuse 
 * is accomplished by adding a {@link Reference} into the RootBinding hierarchy, that
 * references the ComplexTypeBinding.</p>
 * 
 * @see Reference
 * @author Dave Schoorl
 */
public class ComplexType extends AbstractSingleBinding implements IModelAware {
    
    private String identifier = null;   //only needed when registered with BindingModel
    
    private String namespaceUri = null; //only needed when registered with BindingModel
    
    private BindingModel model = null;  //this is set on ComplexTypeBindings that are registered with the BindingModel
    
    /**
     * Create a ComplexTypeReference for an anonymous ComplexType (not registered with {@link BindingModel}
     * @param element
     * @param referencedBinding
     */
    public ComplexType(QName element, IBinding parent, String fieldName) {
    	super(new DefaultElementFetchStrategy(element), null);
        if (parent == null) {
            throw new NullPointerException("Parent IBindingBase cannot be null");
        }
        Reference reference = new Reference(element, this);
        if (parent instanceof ISingleBinding) {
            ((ISingleBinding)parent).setChild(reference);
        } else if (parent instanceof IBindingContainer) {
            ((IBindingContainer)parent).add(reference);
        }
        
        //In the case of anonymous ComplexType, the setter must be on the ComplexType
        FieldAccessProvider provider = new FieldAccessProvider(fieldName);
        setGetter(provider);
        setSetter(provider);
    }

    /**
     * Create a new {@link ComplexType} with the purpose to be referenced by a {@link Reference}
     * @param identifier
     * @param namespaceUri
     */
    public ComplexType(String identifier, String namespaceUri) {
    	super(NoElementFetchStrategy.INSTANCE, null);
        setIdentifier(identifier);
        setNamespaceUri(namespaceUri);
        //the element fetch strategy will be replaced by a real one when this 'type' is copied into a binding hierarchy
    }

    /**
     * Copy constructor that creates a copy of ComplexTypeBinding with the given {@link Reference parent}
     * as it's parent 
     */
    private ComplexType(ComplexType original) {
        super(original, (IElementFetchStrategy)null);	//dirty hack, I want to do: new FetchFromParentStrategy(this), but cannot pass on this in super contructor call 
        setElementFetchStrategy(new FetchFromParentStrategy(this));
        this.identifier = original.identifier;
        this.namespaceUri = original.namespaceUri;
    }
    
	public String getIdentifier() {
	    return this.identifier;
	}
	
	private void setIdentifier(String newIdentifer) {
	    if (newIdentifer == null) {
	        throw new NullPointerException("Identifier cannot be null");
	    }
	    this.identifier = newIdentifer;
	}
	
	public String getNamespace() {
	    return this.namespaceUri;
	}

    private void setNamespaceUri(String newNamespaceUri) {
    	if (newNamespaceUri == null) {
    		newNamespaceUri = XMLConstants.NULL_NS_URI;
    	}
    	this.namespaceUri = newNamespaceUri;
    }

	public Object toJava(RecordAndPlaybackXMLStreamReader staxReader, Object javaContext) throws XMLStreamException {
		Object newJavaContext = newInstance();
		javaContext = select(javaContext, newJavaContext);
		Object result = getChildBinding().toJava(staxReader, javaContext);
		setProperty(javaContext, result);
		return newJavaContext;
	}
	
	@Override
	public void toXml(SimplifiedXMLStreamWriter staxWriter, Object javaContext) throws XMLStreamException {
        getChildBinding().toXml(staxWriter, getProperty(javaContext));
	}

    /**
     * Copy the ComplexTypeHierarchy and place it as a child under the supplied  {@link Reference parent}
     * @param complexTypeReference the parent in the hierarchy
     * @return a copy of this {@link ComplexType}
     */
    ComplexType copy() {
        return new ComplexType(this);
    }
    
    @Override
    public void setModel(BindingModel model) {
        if (model == null) {
            throw new NullPointerException("BindingModel cannot be null");
        }
        if ((this.model != null) && !this.model.equals(model)) {
            throw new IllegalArgumentException("It is currently not supported that a ComplexTypeBinding is added to multiple BindingModels");
        }
        this.model = model;
    }
    
    @Override
    public BindingModel getModel() {
        return this.model;
    }
    
}
