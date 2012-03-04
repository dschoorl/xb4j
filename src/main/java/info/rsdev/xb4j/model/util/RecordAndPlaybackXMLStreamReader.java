package info.rsdev.xb4j.model.util;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    private LinkedList<ParseEventData> recordingQueue = null;
    
    /**
     * The queue of parsing events that will be surved to the user, when it is not empty. If it is empty, on the 
     * otherhand, then the next events are pulled from the {@link #staxReader} instead.
     */
    private LinkedList<ParseEventData> playbackQueue = new LinkedList<ParseEventData>();
    
    private List<Marker> publishedMarkers = new ArrayList<Marker>();
    
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
    public Marker startRecording() {
        if (recordingQueue == null) {
            recordingQueue = new LinkedList<ParseEventData>();
        }
        
        //hashCode is used as a check to identify if the marker points to the expected ParseEventData instance
        int hashCode = 0;
        if (!recordingQueue.isEmpty()) {
            hashCode = recordingQueue.peek().hashCode();
        }
        Marker marker = new Marker(recordingQueue.size(), hashCode);
        publishedMarkers.add(marker);
        return marker;
    }
    
    /**
     * Stop recording parsing events and throw away any recordings upto and including the one pointed at by the {@link Marker}
     */
    public void stopAndWipeRecording() {
        clearRecordings();
    }
    
    /**
     * Rewind to the parse event following the most recent call to {@link #startRecording()} and start playback 
     * of these recorded parsing events. When {@link #startRecording()} is called during playback, ... happens. 
     */
    public void rewindAndPlayback(Marker marker) {
        if (isMarkerObsolete(marker)) {
            throw new Xb4jException("Marker is obsolete");
        }
        removeMarker(marker);
        List<ParseEventData> playbackEvents = recordingQueue.subList(marker.markedAt(), recordingQueue.size());
        LinkedList<ParseEventData> newPlaybackQueue = new LinkedList<ParseEventData>(playbackEvents);
        newPlaybackQueue.addAll(playbackQueue);
        playbackQueue = newPlaybackQueue;
        playbackEvents.clear();
        if (publishedMarkers.isEmpty()) {
            clearRecordings();
        }
    }
    
    private void removeMarker(Marker marker) {
        int indexOfMarker = publishedMarkers.indexOf(marker);
        if (indexOfMarker >= 0) {
            //Remove this marker and anyone published after this one
            publishedMarkers.subList(indexOfMarker, publishedMarkers.size()).clear();
        }
    }
    
    /**
     * 
     * @param marker
     */
    public void stopRecording(Marker marker) {
        removeMarker(marker);
        if (publishedMarkers.isEmpty()) {
            clearRecordings();
        }
    }
    
    public boolean isMarkerObsolete(Marker marker) {
        boolean isObsolete = !publishedMarkers.contains(marker);
        isObsolete = isObsolete || recordingQueue == null;
        isObsolete = isObsolete || (recordingQueue.size() < marker.markedAt());
        isObsolete = isObsolete || (!marker.isAtHead() && (recordingQueue.get(marker.markedAt() - 1).hashCode() != marker.getHashCode()));
        return isObsolete;
    }
    
    public boolean isRecording() {
        return this.recordingQueue != null;
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
        this.publishedMarkers.clear();
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
        
        
        @Override
        public String toString() {
            String data = text==null?name.toString():text;
            return String.format("ParseEventData[eventType=%d, data=%s, hascode=%d]", eventType, data, hashCode());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + eventType;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((text == null) ? 0 : text.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof ParseEventData)) {
                return false;
            }
            ParseEventData other = (ParseEventData) obj;
            if (eventType != other.eventType) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (text == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!text.equals(other.text)) {
                return false;
            }
            return true;
        }

    }

    public static final class Marker {
        
        /**
         * Pointer to the first recorded element since recording was started.
         */
        private int indexIntoRecordingQueue = 0;
        
        /**
         * The hasCode of the {@link ParseEventData} that preceeds the element that the indexIntoRecordingQueue
         * points to
         */
        private final int hashCode;
        
        private Marker(int recordingQueueSize, int hashCode) {
            this.hashCode = hashCode;
            indexIntoRecordingQueue = recordingQueueSize;
        }
        
        public int markedAt() {
            return this.indexIntoRecordingQueue;
        }
        
        private int getHashCode() {
            return hashCode;
        }
        
        public boolean isAtHead() {
            return this.indexIntoRecordingQueue == 0;
        }
        
        @Override
        public String toString() {
            return String.format("Marker[indexIntoRecordingQueue=%d, hashcode=%d]", indexIntoRecordingQueue, hashCode);
        }
    }
    
}
