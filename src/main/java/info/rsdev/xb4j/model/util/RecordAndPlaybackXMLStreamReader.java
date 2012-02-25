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
        int eventType = -1; 
        if (!playbackQueue.isEmpty()) {
            this.currentEvent = playbackQueue.poll();
        } else {
            //read next tag from staxReader
            eventType = staxReader.nextTag();
            this.currentEvent = ParseEventData.newParseEventData(eventType, staxReader);
        }
        
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
    
    

    public String getElementText() throws XMLStreamException {
        if (!playbackQueue.isEmpty()) {
            this.currentEvent = playbackQueue.poll();
        } else {
            //read content of text-only element from staxReader
            String text = staxReader.getElementText();
            this.currentEvent = new ParseEventData(XMLStreamConstants.CHARACTERS, text);
        }
        
        if (this.recordingQueue != null) {
            this.recordingQueue.add(this.currentEvent);
        }
        if (this.currentEvent.eventType != XMLStreamConstants.CHARACTERS) {
            throw new XMLStreamException("No element text could be read at this point in the stream");
        }
        return this.currentEvent.text;
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
    
    private static final class ParseEventData {
        
        private QName name = null;
        private int eventType = -1;
        private String text = null;
        
        private ParseEventData(int eventType, String elementText) {
            this.eventType = eventType;
            this.text = elementText;
        }
        
        private ParseEventData(int eventType, QName elementName) {
            this.eventType = eventType;
            this.name = elementName;
        }
        
        private static ParseEventData newParseEventData(int eventType, XMLStreamReader staxReader) throws XMLStreamException {
            if ((eventType != XMLStreamConstants.START_ELEMENT) && (eventType != XMLStreamConstants.END_ELEMENT)) {
                throw new XMLStreamException("This type of event is currently unsupported: "+eventType);
            }
            
            ParseEventData eventData = new ParseEventData(eventType, staxReader.getName());
            //TODO: possibly set more data
            return eventData;
        }

    }

}
