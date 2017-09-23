package info.rsdev.xb4j.model.bindings;

public interface ISemaphore {

    /**
     * Lock this binding tree, so no other threads can make changes to it
     */
    public abstract void lock();

    /**
     * Release the lock that was previously obtained by {@link #lock()}.
     */
    public abstract void unlock();

}
