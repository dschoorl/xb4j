package info.rsdev.xb4j.model.util;

import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * <p>Decorate an XMLStreamReader to add recording/playback functionality, kind of like mark/reset on an
 * {@link InputStream}, however, no bytes are recorded, but instead parsing events.</p>
 * 
 * @author Dave Schoorl
 */
public class RecordAndPlayBackXMLStreamReader {
    
    private XMLStreamReader staxReader = null;
    
    public RecordAndPlayBackXMLStreamReader(XMLStreamReader staxReader) throws XMLStreamException {
        if (staxReader == null) {
            throw new NullPointerException("XMLStreamReader cannot be null");
        }
        this.staxReader = staxReader;
    }
    
    /**
     * Keep track of all parsing events as of now. Parsing events read from the stream are both returned and recorded.
     * When we were already recording, all current recodings are discarded, as if first {@link #stopRecording()} was
     * called.
     */
    public void startRecording() {
        //TODO: safe state of XMLStreamReader and mark the InpuStream
    }
    
    /**
     * Stop recording parsing events and throw away any recordings you have
     */
    public void stopRecording() {
        //TODO: implement
    }
    
    /**
     * Start playing back recorded parsing events. 
     */
    public void playback() {
        //TODO: reset InputStream, restore state in staxReader (or simpler: return new staxreader with restored state (namespaces etc.)
    }
    
    public int nextTag() throws XMLStreamException {
        //todo: if we must record, read and record all events in 
        return this.staxReader.nextTag();
    }
    
    private static final class ParseEventData {
        //TODO: impplement
    }

}
