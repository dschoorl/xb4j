package info.rsdev.xb4j.test;

public class ObjectA {
    
    private String name = null;
    
    @SuppressWarnings("unused")
    private ObjectA() {}
    
    public ObjectA(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    protected void setName(String newName) {
        this.name = newName;
    }

}
