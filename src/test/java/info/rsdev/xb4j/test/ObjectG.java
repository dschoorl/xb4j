package info.rsdev.xb4j.test;

public class ObjectG {
	
	private ObjectG subObject = null;
	
	private String name = null;
	
	protected ObjectG() { }
	
	public ObjectG(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ObjectG getSubObject() {
		return subObject;
	}

	public void setSubObject(ObjectG subObject) {
		this.subObject = subObject;
	}
	
}
