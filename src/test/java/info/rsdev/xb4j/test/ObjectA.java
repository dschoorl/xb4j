package info.rsdev.xb4j.test;

public class ObjectA {
    
    private String name = null;
    
    protected ObjectA() {}
    
    public ObjectA(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    protected ObjectA setName(String newName) {
        if (newName == null) {
            throw new NullPointerException("Name cannot be null");
        }
        this.name = newName;
        return this;
    }

}
