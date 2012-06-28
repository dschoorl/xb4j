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

import info.rsdev.xb4j.model.bindings.IBinding;
import info.rsdev.xb4j.model.java.accessor.ISetter;
import info.rsdev.xb4j.util.RecordAndPlaybackXMLStreamReader;

/**
 * An {@link ICreator} is used during the unmarshalling process (from xml to java). It creates a new javaContext for 
 * the IBinding that it is owned by. Any {@link ISetter} that is defined on the binding will be applied to this new 
 * javaContext. The new javaContext is returned to the parent binding by the {@link IBinding#toJava(RecordAndPlaybackXMLStreamReader)} 
 * method.
 * 
 * @author Dave Schoorl
 */
public interface ICreator {
    
    public Object newInstance();
    
    /**
     * The Java type that this {@link ICreator} will create with a call to {@link #newInstance()}
     * @return the Java type
     */
    public Class<?> getJavaType();
    
    public int hashCode();
    
    public boolean equals(Object obj);
    
}
