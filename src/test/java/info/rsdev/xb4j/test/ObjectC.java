package info.rsdev.xb4j.test;

import java.util.List;

/**
 * 
 * @author Dave Schoorl
 */
public class ObjectC extends ObjectA {
	
	private int max = 0;
	
	private String description = null;
	
	private List<String> details = null;
	
	protected boolean isInitialized = false;
	
	public ObjectC() {
		super();
	}

	public int getMax() {
		return this.max;
	}

	public ObjectC setMax(int max) {
		this.max = max;
		return this;
	}

	public ObjectC setName(String name) {
		super.setName(name);
		return this;
	}

	public String getDescription() {
		return this.description;
	}

	public ObjectC setDescription(String description) {
		this.description = description;
		return this;
	}

	public List<String> getDetails() {
		return this.details;
	}

	public ObjectC setDetails(List<String> details) {
		this.details = details;
		return this;
	}

	public boolean isInitialized() {
		return this.isInitialized;
	}

	public ObjectC setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
		return this;
	}
	
}
