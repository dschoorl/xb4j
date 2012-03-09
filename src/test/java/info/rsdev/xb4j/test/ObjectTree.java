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
package info.rsdev.xb4j.test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ObjectTree {
	
	private ObjectA myObject = null;
	
	private List<String> messages = null;
	
	public ObjectTree setMyObject(ObjectA mo) {
		this.myObject = mo;
		return this;
	};
	
	public ObjectA getMyObject() {
		return this.myObject;
	}
	
	public Collection<String> getMessages() {
		if (this.messages == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(this.messages);
	}
	
	public void addMessage(String newMessage) {
		if (newMessage == null) { return; }
		if (this.messages == null) {
			this.messages = new LinkedList<String>();
		}
		this.messages.add(newMessage);
	}
}