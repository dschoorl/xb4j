package info.rsdev.xb4j.test;

public class ObjectB {
    
    private String value = null;
    
    @SuppressWarnings("unused")
    private ObjectB() {}
    
    public ObjectB(String value) {
        if (value == null) {
            throw new NullPointerException("Name cannot be null");
        }
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    protected void setValue(String newValue) {
        this.value = newValue;
    }

}
