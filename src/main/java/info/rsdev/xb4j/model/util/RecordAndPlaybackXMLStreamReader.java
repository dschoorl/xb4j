package info.rsdev.xb4j.model.util;

import java.util.LinkedList;
import java.util.Queue;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * <p>Decorate an XMLStreamReader to add recording/playback functionality, to allow previous XMLStreamReader events 
 * to be replayed once again.</p>
 * <p>This class is not thread safe</p>
 * 
 * @author Dave Schoorl
 */
public class RecordAndPlaybackXMLStreamReader {
    
    /**
     * The {@link XMLStreamReader} that parses an xml stream
     */
    private XMLStreamReader staxReader = null;
    
    /**
     * New recorded XMLStreamReader events are placed in this queue. When this queue is null,
     * it means that currently, no events are recorded. 
     */
    private Queue<ParseEventData> recordingQueue = null;
    
    /**
     * The queue of parsing events that will be surved to the user, when it is not empty. If it is empty, on the 
     * otherhand, then the next events are pulled from the {@link #staxReader} instead.
     */
    private Queue<ParseEventData> playbackQueue = new LinkedList<ParseEventData>();
    
    private ParseEventData currentEvent = null;
    
    public RecordAndPlaybackXMLStreamReader(XMLStreamReader staxReader) throws XMLStreamException {
        if (staxReader == null) {
            throw new NullPointerException("XMLStreamReader cannot be null");
        }
        this.staxReader = staxReader;
    }
    
    /**
     * <p>Keep track of all parsing events as of now. Parsing events read from the stream are both returned and recorded.
     * When we were already recording, all current recodings are discarded, as if first {@link #stopAndWipeRecording()} was
     * called.</p>
     * <p>When startRecording is called while playing back recordings, no current recordings must be dropped.</p>
     */
    public void startRecording() {
        clearRecordings();
        recordingQueue = new LinkedList<ParseEventData>();
    }
    
    /**
     * Stop recording parsing events and throw away any recordings you have
     */
    public void stopAndWipeRecording() {
        clearRecordings();
    }
    
    /**
     * Rewind to the parse event following the most recent call to {@link #startRecording()} and start playback 
     * of these recorded parsing events. When {@link #startRecording()} is called during playback, ... happens. 
     */
    public void rewindAndPlayback() {
        if ((recordingQueue != null) && !recordingQueue.isEmpty()) {
            playbackQueue.addAll(recordingQueue);   //move parsing events over from recording to playbackQueue
        }
        clearRecordings();
    }
    
    public int nextTag() throws XMLStreamException {
        //todo: if we must record, read and record all events in 
        this.currentEvent = nextParseEventData();
        if (this.recordingQueue != null) {
            this.recordingQueue.add(this.currentEvent);
        }
        return this.currentEvent.eventType;
    }
    
    public QName getName() throws XMLStreamException {
        if (this.currentEvent == null) {
            throw new XMLStreamException("Not the proper moment to call getName()");
        }
        return this.currentEvent.name;
    }

    public void close() throws XMLStreamException {
        clearRecordings();
        this.playbackQueue.clear();
        this.staxReader.close();
    }
    
    private void clearRecordings() {
        if (this.recordingQueue != null) {
            this.recordingQueue.clear();
            this.recordingQueue = null;
        }
    }
    
    private ParseEventData nextParseEventData() throws XMLStreamException {
        if (!playbackQueue.isEmpty()) {
            return playbackQueue.poll();
        }
        
        //read next tag from staxReader
        int eventType = staxReader.nextTag();
        return ParseEventData.newParseEventData(eventType, staxReader);
    }

    private static final class ParseEventData {
        
        private QName name = null;
        private int eventType = -1;
        
        private static ParseEventData newParseEventData(int eventType, XMLStreamReader staxReader) throws XMLStreamException {
            if ((eventType != XMLStreamConstants.START_ELEMENT) && (eventType != XMLStreamConstants.END_ELEMENT)) {
                throw new XMLStreamException("This type of event is currently unsupported: "+eventType);
            }
            
            ParseEventData eventData = new ParseEventData();
            eventData.eventType = eventType;
            eventData.name = staxReader.getName();
            return eventData;
        }
        
    }

}
