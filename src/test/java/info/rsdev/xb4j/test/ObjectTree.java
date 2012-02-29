package info.rsdev.xb4j.test;

public class ObjectTree {
    private ObjectA myObject = null;
    public ObjectTree setMyObject(ObjectA mo) { this.myObject = mo; return this; };
    public ObjectA getMyObject() { return this.myObject; }
}