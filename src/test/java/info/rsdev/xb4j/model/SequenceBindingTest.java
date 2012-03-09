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

import static org.junit.Assert.assertEquals;
import info.rsdev.xb4j.test.ObjectC;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import org.junit.Test;

public class SequenceBindingTest {
	
	@Test
	public void testMarshallMultipleElementsNoNamespace() {
		RootBinding root = new RootBinding(new QName("root"), ObjectC.class);
		SequenceBinding sequence = root.setChild(new SequenceBinding());
		sequence.add(new SimpleTypeBinding(new QName("naam")), "name");
		sequence.add(new SimpleTypeBinding(new QName("omschrijving")), "description");
		BindingModel model = new BindingModel().register(root);
		
		ObjectC instance = new ObjectC().setName("tester").setDescription("Ik test dingen");
		
        String expected = "<root><naam>tester</naam><omschrijving>Ik test dingen</omschrijving></root>";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.toXml(stream, instance);
        assertEquals(expected, stream.toString());
	}
	
}
