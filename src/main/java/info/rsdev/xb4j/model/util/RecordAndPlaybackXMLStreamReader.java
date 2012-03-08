/* Copyright 2012 Red Star Development / Dave Schoorl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.xb4j.model.util;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Decorate an XMLStreamReader to add recording/playback functionality, to allow previous XMLStreamReader events 
 * to be replayed once again. Events that are currently recorded are: start- and end element and characters.</p>
 * <p>This class is not thread safe</p>
 * 
 * @author Dave Schoorl
 */
public class RecordAndPlaybackXMLStreamReader implements XMLStreamConstants {
	
	private static final Logger logger = LoggerFactory.getLogger(RecordAndPlaybackXMLStreamReader.class);
	
	private static final String[] EVENTNAMES = new String[] {"Undefined", "start element", "end element", "processing instruction",
			"characters", "comment", "space", "start document", "end document", "entity reference", "attribute", "DTD", "cdata",
			"namespace", "notation declaration", "entity declaration"};
    
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
     * <p>Keep track of all parsing events as of now (including the current event). Parsing events read from the stream are both returned and recorded.
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
        	ParseEventData top = recordingQueue.peekLast();
        	if (top != null) {
        		hashCode = top.hashCode();
        	}
        }
        Marker marker = new Marker(recordingQueue.size(), hashCode, this.currentEvent);
        publishedMarkers.add(marker);
        
        return marker;
    }
    
    /**
     * Stop recording parsing events and throw away all recordings and markers
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
        this.currentEvent = marker.getCurrentEvent();
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
        if (!marker.isAtHead()) {
        	ParseEventData prior = recordingQueue.get(marker.markedAt() - 1);
            isObsolete = isObsolete || ((prior != null) && (prior.hashCode() != marker.getHashCode()));
        }
        return isObsolete;
    }
    
    public boolean isRecording() {
        return this.recordingQueue != null;
    }
    
    public String getEventName() {
        if (this.currentEvent == null) { return null; }
        return EVENTNAMES[currentEvent.eventType];
    }
    
    /**
     * Respond to START ELEMENT, END ELEMENT, CHARACTER and END DOCUMENT events
     * @return
     * @throws XMLStreamException
     */
    public int nextTag() throws XMLStreamException {
        int eventType = 0;
        while ((eventType != START_ELEMENT) && (eventType != END_ELEMENT) && (eventType != END_DOCUMENT)) {
            if (!playbackQueue.isEmpty()) {
                this.currentEvent = playbackQueue.poll();
                eventType = currentEvent.eventType;
            } else {
                while ((eventType != START_ELEMENT) && (eventType != END_ELEMENT) && (eventType != END_DOCUMENT)) {
                    if ((eventType > 0) && (this.currentEvent != null) && (this.currentEvent.eventType == START_ELEMENT)) {
                    	if (eventType == CHARACTERS) {
                    		String characters = staxReader.getText().trim();
                    		if (characters.length() > 0) {
                    			//TODO: salvage characters to recordingQueue
                    			if (logger.isTraceEnabled()) {
                    				logger.trace(String.format("Skipping over stax event %s: %s", EVENTNAMES[eventType], characters));
                    			}
                    		}
                    	} else {
                    		if (logger.isTraceEnabled()) {
                    			logger.trace(String.format("Skipping over stax event %s ", EVENTNAMES[eventType]));
                    		}
                    	}
                    }
                    eventType = staxReader.next();
                }
                if ((eventType == START_ELEMENT) || (eventType == END_ELEMENT)) {
                    this.currentEvent = ParseEventData.newParseEventData(eventType, staxReader);
                } else if (eventType == END_DOCUMENT) {
                    this.currentEvent = new ParseEventData(eventType, (String) null, staxReader.getLocation());
                }
            }
            if (this.recordingQueue != null) {
                this.recordingQueue.add(this.currentEvent);
            }
        }
        return this.currentEvent.eventType;
    }
    
    public QName getName() throws XMLStreamException {
        if (this.currentEvent == null) {
            throw new XMLStreamException("Not the proper moment to call getName()");
        }
        return this.currentEvent.name;
    }
    
    /**
     * Check if the next element in the xml file is the start of the element that this binding expects. If not, then the
     * staxReader is positioned prior to the element just read (so it can be re-read),
     * @param staxReader
     * @param expectedElement
     * @return true if this binding should continue processing the current element, false otherwise
     * @throws XMLStreamException any exception from the underlying stax reader is propagated up
     */
    public boolean isAtElementStart(QName expectedElement) throws XMLStreamException {
        boolean isAt = isAtElement(expectedElement, XMLStreamReader.START_ELEMENT);
        if (isAt && logger.isTraceEnabled()) {
            logger.trace(String.format("Found expected element <%s> open tag ", expectedElement));
        }
    	return isAt;
    }
    
    public boolean isAtElementEnd(QName expectedElement) throws XMLStreamException {
        boolean isAt = isAtElement(expectedElement, XMLStreamReader.END_ELEMENT);
        if (isAt && logger.isTraceEnabled()) {
            logger.trace(String.format("Found expected element </%s> close tag ", expectedElement));
        }
        return isAt;
    }
    
    private boolean isAtElement(QName expectedElement, int eventType) throws XMLStreamException {
        if (expectedElement != null) {
        	boolean matchesExpected = false;
        	Marker marker = startRecording();
        	int realEvent = 0;
        	QName encounteredName = null;
        	try {
        		realEvent = nextTag();
        		if (realEvent != END_DOCUMENT) {
		            if (realEvent == eventType) {	//should only be start- or end element
		            	encounteredName = getName();
		                matchesExpected = expectedElement.equals(encounteredName);
		            }
        		}
        	} finally {
                if (!matchesExpected) {
                	rewindAndPlayback(marker);
                	if (logger.isTraceEnabled()) {
	                	logger.trace(String.format("Expected %s (%s), but found %s (%s @ %s)", EVENTNAMES[eventType], expectedElement,
	                			EVENTNAMES[realEvent], encounteredName, getLocation()));
                	}
                } else {
        			stopRecording(marker);
                }
        	}
        	return matchesExpected;
        }
        return true;	//when we expect nothing, all is well
    }
    
    public Location getLocation() {
    	if (currentEvent != null) {
    		return currentEvent.location;
    	}
    	return null;
    }
    
    public String getElementText() throws XMLStreamException {
        if (!playbackQueue.isEmpty()) {
            this.currentEvent = playbackQueue.poll();
        } else {
            //read content of text-only element from staxReader
            String text = staxReader.getElementText();
            this.currentEvent = new ParseEventData(CHARACTERS, text, staxReader.getLocation());
        }
        
        if (this.recordingQueue != null) {
            this.recordingQueue.add(this.currentEvent);
        }
        if (this.currentEvent.eventType != CHARACTERS) {
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
    
    @Override
    public String toString() {
    	String separator = "";
    	StringBuffer sb = new StringBuffer("RecordAndPlaybackXMLStreamReader[");
    	if (this.currentEvent != null) {
    		sb.append("currentEvent=").append(EVENTNAMES[currentEvent.eventType]);
    		if (currentEvent.name != null) {
    			sb.append(" <");
    			if (currentEvent.eventType == END_ELEMENT) {
    				sb.append("/");
    			}
    			sb.append(currentEvent.name.toString()).append(">");
    		}
    		separator = ", ";
    	}
    	
    	sb.append(separator);
    	if ((recordingQueue != null) && !recordingQueue.isEmpty()) {
    		sb.append("recordingQueueSize=").append(recordingQueue.size());
    	} else {
    		sb.append("isRecording=").append(isRecording());
    	}
    	
    	sb.append(", ");
    	if (!playbackQueue.isEmpty()) {
    		sb.append("playbackQueueSize=").append(playbackQueue.size());
    	} else {
    		sb.append("isPlayingback=false");
    	}
    	sb.append("]");
    	
    	return sb.toString();
    }
    
    private static final class ParseEventData {
        
    	private Location location = null;
        private QName name = null;
        private int eventType = -1;
        private String text = null;
        
        private ParseEventData(int eventType, String elementText, Location location) {
            this.eventType = eventType;
            this.text = elementText;
            this.location = location;
        }
        
        private ParseEventData(int eventType, QName elementName, Location location) {
            this.eventType = eventType;
            this.name = elementName;
            this.location = location;
        }
        
        private static ParseEventData newParseEventData(int eventType, XMLStreamReader staxReader) throws XMLStreamException {
            if ((eventType != START_ELEMENT) && (eventType != END_ELEMENT)) {
                throw new XMLStreamException("This type of event is currently unsupported: "+eventType);
            }
            
            ParseEventData eventData = new ParseEventData(eventType, staxReader.getName(), staxReader.getLocation());
            //TODO: possibly set more data
            return eventData;
        }
        
        
        @Override
        public String toString() {
            String data = text==null?name.toString():text;
            return String.format("ParseEventData[eventType=%S, data=%s, hascode=%d]", EVENTNAMES[eventType], data, hashCode());
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
        
        /**
         * The currentEvent at the time the marking was set
         */
        private final ParseEventData currentEvent;
        
        private Marker(int recordingQueueSize, int hashCode, ParseEventData current) {
            this.hashCode = hashCode;
            this.indexIntoRecordingQueue = recordingQueueSize;
            this.currentEvent = current;
        }
        
        public int markedAt() {
            return this.indexIntoRecordingQueue;
        }
        
        private int getHashCode() {
            return hashCode;
        }
        
        private ParseEventData getCurrentEvent() {
        	return this.currentEvent;
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
