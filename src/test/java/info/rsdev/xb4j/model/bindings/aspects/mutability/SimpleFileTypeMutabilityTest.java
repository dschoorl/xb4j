/* Copyright 2013 Red Star Development / Dave Schoorl
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
package info.rsdev.xb4j.model.bindings.aspects.mutability;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.rsdev.xb4j.exceptions.Xb4jMutabilityException;
import info.rsdev.xb4j.model.bindings.Root;
import info.rsdev.xb4j.model.bindings.SimpleFileType;

class SimpleFileTypeMutabilityTest extends BaseBindingMutabilityTest<SimpleFileType> {

    @BeforeEach
    public void setUp() {
        Root root = new Root(new QName("root"), Object.class);
        immutableElement = new SimpleFileType(new QName("level1"), false);
        root.setChild(immutableElement);
        root.makeImmutable();
    }

    @Test
    void testCannotSetCodingtypeFrom() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.setCodingtypeFrom(new QName("encodingAttribute"), "Base64"));
    }

    @Test
    void testCannotSetFilenameHintFrom() {
        assertThrows(Xb4jMutabilityException.class, () -> immutableElement.setFilenameHintFrom(new QName("filenameAttribute"), "archive.zip"));
    }

}
