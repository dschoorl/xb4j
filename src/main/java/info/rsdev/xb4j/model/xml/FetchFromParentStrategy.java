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
package info.rsdev.xb4j.model.xml;

import info.rsdev.xb4j.model.IBindingBase;

import javax.xml.namespace.QName;

/**
 * Get the xml element from the parent binding
 * 
 * @author Dave Schoorl
 */
public class FetchFromParentStrategy implements IElementFetchStrategy {
    
    private IBindingBase thisBinding = null;
    
    /**
     * Create a new {@link FetchFromParentStrategy}
     * @param thisBinding the {@link IBindingBase} that owns this {@link FetchFromParentStrategy}
     */
    public FetchFromParentStrategy(IBindingBase thisBinding) {
        if (thisBinding == null) {
            throw new NullPointerException("IBinding cannot be null");
        }
        this.thisBinding = thisBinding;
    }

	@Override
	public QName getElement() {
		return getParentBinding().getElement();
	}
	
    private IBindingBase getParentBinding() {
    	IBindingBase parent = thisBinding.getParent();
    	if (parent == null) {
    		throw new NullPointerException("Parent is not set in ".concat(thisBinding.getClass().getName()));
    	}
    	return parent;
    }

}
