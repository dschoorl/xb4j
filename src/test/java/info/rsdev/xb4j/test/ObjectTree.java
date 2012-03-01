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