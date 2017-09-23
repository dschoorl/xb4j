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

import javax.xml.namespace.QName;

/**
 * Get the element stored in this strategy
 *
 * @author Dave Schoorl
 */
public class DefaultElementFetchStrategy implements IElementFetchStrategy {

    private QName element = null;

    /**
     * Create a new {@link DefaultElementFetchStrategy}. This implementation of {@link IElementFetchStrategy} is the simplest: the
     * bound element is stored in this strategy
     *
     * @param element the element bound
     */
    public DefaultElementFetchStrategy(QName element) {
        if (element == null) {
            throw new NullPointerException("QName must be provided");
        }
        this.element = element;
    }

    @Override
    public QName getElement() {
        return this.element;
    }

    @Override
    public String toString() {
        return String.format("DefaultElementFetchStrategy[element=%s]", this.element);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.element == null) ? 0 : this.element.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultElementFetchStrategy other = (DefaultElementFetchStrategy) obj;
        if (this.element == null) {
            if (other.element != null) {
                return false;
            }
        } else if (!this.element.equals(other.element)) {
            return false;
        }
        return true;
    }

}
