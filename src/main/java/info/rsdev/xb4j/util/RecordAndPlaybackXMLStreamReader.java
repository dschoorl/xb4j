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
package info.rsdev.xb4j.util;

import info.rsdev.xb4j.exceptions.Xb4jException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * <p>This class is NOT thread safe</p>
 * 
 * @author Dave Schoorl
 */
public class RecordAndPlaybackXMLStreamReader implements XMLStreamConstants {
	
	private final Logger logger = LoggerFactory.getLogger(RecordAndPlaybackXMLStreamReader.class);
	
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
     * The queue of parsing events that will be served to the user, when it is not empty. If it is empty, on the 
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
        clearAllRecordings();
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
            clearAllRecordings();
        }
        this.currentEvent = marker.getCurrentEvent();
    }
    
    /**
     * Remove the provided {@link Marker marker} and every marker that was set since. When the marker is no longer recorded,
     * nothing happens
     * 
     * @param marker
     * @return true if the marker was valid and was removed, false otherwise
     */
    private boolean removeMarker(Marker marker) {
    	boolean markerWasValid = false;
        int indexOfMarker = publishedMarkers.indexOf(marker);
        if (indexOfMarker >= 0) {
            //Remove this marker and anyone published after this one
            publishedMarkers.subList(indexOfMarker, publishedMarkers.size()).clear();
        }
        return markerWasValid;
    }
    
    /**
     * Indicate that the given {@link Marker marker} (and every marker that was set since) will never be used for rewinding; 
     * it is no longer needed. However, recorded events will only be removed from the recording queue when there are no more 
     * markers left, because only then we are certain that a rewind can no longer happen.
     * 
     * @param marker the {@link Marker} to dismiss
     */
    public void stopRecording(Marker marker) {
        removeMarker(marker);
        if (publishedMarkers.isEmpty()) {
            clearAllRecordings();
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
    
    public int getEvent() {
    	if (this.currentEvent == null) {
    		return 0;
    	}
    	return currentEvent.eventType;
    }
    
    public boolean isAtElement() {
    	int currentEvent = getEvent();
    	return currentEvent==XMLStreamConstants.START_ELEMENT||currentEvent==XMLStreamConstants.END_ELEMENT;
    }
    
    public Map<QName, String> getAttributes() {
    	if (currentEvent != null) {
    		return currentEvent.attributes;
    	}
    	return null;
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
        ParseEventData eventData = null;
        while ((eventType != START_ELEMENT) && (eventType != END_ELEMENT) && (eventType != END_DOCUMENT)) {
            if (!playbackQueue.isEmpty()) {
            	eventData = playbackQueue.poll();
                eventType = eventData.eventType;
            } else {
            	//read from stream
                eventType = staxReader.next();
                while ((eventType != START_ELEMENT) && (eventType != END_ELEMENT) && (eventType != END_DOCUMENT)) {
                	if (eventType == CHARACTERS || eventType == CDATA || eventType == ENTITY_REFERENCE) {
                        if ((this.currentEvent != null) && (this.currentEvent.eventType == START_ELEMENT)) {
                            StringBuffer content = new StringBuffer();
                            while (eventType == CHARACTERS || eventType == CDATA || eventType == ENTITY_REFERENCE) {
                                content.append(staxReader.getText());
                                eventType = staxReader.next();	//read uptil the proper END_ELEMENT
                            }
                            //TODO: check that endtag and starttag match -- or is that not our concern...
                            eventData = new ParseEventData(CHARACTERS, content.toString(), staxReader.getLocation());
                			if (logger.isTraceEnabled()) {
                				logger.trace(String.format("Skipping over stax event %s: %s", EVENTNAMES[eventType], content));
                			}
                            if (this.recordingQueue != null) {
                                this.recordingQueue.add(eventData);
                            }
                        } else {
                    		//ignore characters that do not directly follow a start-element section
                    		eventType = staxReader.next();
                        }
                	} else if ((eventType != START_ELEMENT) && (eventType != END_ELEMENT) && (eventType != END_DOCUMENT)) {
                		if (logger.isTraceEnabled()) {
                			logger.trace(String.format("Skipping over stax event %s ", EVENTNAMES[eventType]));
                		}
                		eventType = staxReader.next();
                	}
                }
                        
                //we have just read an 
                if ((eventType == START_ELEMENT) || (eventType == END_ELEMENT)) {
                	eventData = ParseEventData.newParseEventData(eventType, staxReader);
                } else if (eventType == END_DOCUMENT) {
                	eventData = new ParseEventData(eventType, (String) null, staxReader.getLocation());
                }
            }
            
            if (this.recordingQueue != null) {
                this.recordingQueue.add(eventData);
            }
        }
        
        this.currentEvent = eventData;
        return this.currentEvent.eventType;
    }
    
    public QName getName() {
        if (this.currentEvent == null) {
            throw new IllegalStateException("Not the proper moment to call getName()");
        }
        return this.currentEvent.name;
    }
    
    public boolean isCurrentAnElementStart(QName expectedElement) {
        //check if the current element matches the question, i.o.w. we do not need to probe the stream
        return (currentEvent != null) && (getEvent() == START_ELEMENT) && (getName().equals(expectedElement));
    }
    
    /**
     * Check if the next xml-tag in the stream is the start of the element that this binding expects. If not, then the
     * staxReader is positioned prior to the element just read (so it can be re-read),
     * @param expectedElement the QName of the next expected element
     * @return true if this binding should continue processing the current element, false otherwise
     * @throws XMLStreamException any exception from the underlying stax reader is propagated up
     */
    public boolean isNextAnElementStart(QName expectedElement) throws XMLStreamException {
        boolean isNext = isNextElement(expectedElement, XMLStreamReader.START_ELEMENT);
        if (isNext && logger.isTraceEnabled()) {
            logger.trace(String.format("Found expected element <%s> open tag %s", expectedElement, getRowColumn(getLocation())));
        }
    	return isNext;
    }
    
    public boolean isNextAnElementEnd(QName expectedElement) throws XMLStreamException {
        boolean isNext = isNextElement(expectedElement, XMLStreamReader.END_ELEMENT);
        if (isNext && logger.isTraceEnabled()) {
            logger.trace(String.format("Found expected element </%s> close tag %s", expectedElement, getRowColumn(getLocation())));
        }
        return isNext;
    }
    
    public boolean skipToElementEnd() throws XMLStreamException {
    	if (getEvent() != START_ELEMENT) {
    		throw new XMLStreamException(String.format("Can only skip to element end when we are currently on element start. " +
    				"Current event is '%s' %s).", EVENTNAMES[getEvent()], getRowColumn(getLocation())));
    	}
    	
    	//first disable recording queue, so that skipped elements won't get recorded -- that would not be useful 
    	LinkedList<ParseEventData> backupRecordingQueue = this.recordingQueue;
    	try {
    		this.recordingQueue = null;
	    	QName expectedElement = getName();
	    	int xmlElementLevelCount = 0;
	    	while (xmlElementLevelCount >= 0) {
		    	int eventType = nextTag();
		    	if (eventType == START_ELEMENT) {
		    		xmlElementLevelCount++;
		    	} else if (eventType == END_ELEMENT) {
		    		xmlElementLevelCount--;
		    	} else if (eventType == END_DOCUMENT) {
		    		throw new XMLStreamException(String.format("Unexpectedly reached end of xml document while searching for end " +
		    				"element (%s)", expectedElement));
		    	}
	    	}
    	
	    	//the current event should now be the expected end element tag. Let's check this
	    	if (!getName().equals(expectedElement)) {
	    		throw new XMLStreamException(String.format("Expected end element %s, but encountered unexpected end element %s ", 
	    				expectedElement, getName()), getLocation());
	    	}
	    	
    		//add END_ELEMENT of the skipped element to the recording queue
	    	if (backupRecordingQueue != null) {
	    		backupRecordingQueue.add(this.currentEvent);
	    	}
    	} finally {
    		this.recordingQueue = backupRecordingQueue;	//restore recording queue
    	}
    	
    	return true;
    }
    
    /**
     * Read the contents (text only) from the current element and send the bytes to the {@link OutputStream}
     * 
     * @param out
     * @throws XMLStreamException
     */
    public void elementContentToOutputStream(OutputStream out) throws XMLStreamException {
    	if (out == null) {
    		throw new NullPointerException("OutputStream cannot be null");
    	}
    	
    	if (getEvent() != START_ELEMENT) {
    		throw new XMLStreamException(String.format("Can only stream element to output when we are currently on element start. " +
    				"Current event is '%s' %s).", EVENTNAMES[getEvent()], getRowColumn(getLocation())));
    	}
    	
    	//first disable recording queue, so that content of what's streamed to outputstream won't get recorded -- this possibly is very large
        QName currentTextElement = getName();
    	LinkedList<ParseEventData> backupRecordingQueue = this.recordingQueue;
    	this.recordingQueue = null;
    	try {
            //read content of text-only element from staxReader
            int eventType = staxReader.next();
            while (eventType != END_ELEMENT) {
                if (eventType == CHARACTERS || eventType == CDATA || eventType == SPACE || eventType == ENTITY_REFERENCE) {
                    out.write(staxReader.getText().getBytes());	//TODO: what about encodings etc.
                } else if (eventType == PROCESSING_INSTRUCTION || eventType == COMMENT) {
                    //ignore -- really?, because this is strange...
                } else if (eventType == END_DOCUMENT) {
                	throw new XMLStreamException(String.format("Malformed xml; reached %s when reading text for <%s>", 
                			EVENTNAMES[eventType], currentTextElement), staxReader.getLocation());
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                	//mixed content is currently not supported
                	throw new XMLStreamException(String.format("Found %s <%s> while reading text for <%s>; mixed content is " +
                			"currently not supported %s", EVENTNAMES[eventType], staxReader.getName(), currentTextElement, 
                			getRowColumn(staxReader.getLocation())));
                } else {
                    throw new XMLStreamException(String.format("Unexpected %s", EVENTNAMES[eventType]), staxReader.getLocation());
                }
                eventType = staxReader.next();	//read END_ELEMENT
            }
            
            if (eventType == END_ELEMENT) {
            	if (!currentTextElement.equals(staxReader.getName())) {
            		throw new XMLStreamException(String.format("Malformed xml; expected end element </%s>, but encountered </%s>",
            				currentTextElement, staxReader.getName()), staxReader.getLocation());
            	}
            	playbackQueue.add(ParseEventData.newParseEventData(eventType, staxReader));	//push the end element on the playback queue
            }
    	} catch (IOException e) {
    		throw new XMLStreamException(String.format("Exception occured when streaming content of element %s to OutputStream", currentTextElement), e);
    	} finally {
    		this.recordingQueue = backupRecordingQueue;	//restore recording queue
    	}
    }
    
    private boolean isNextElement(QName expectedElement, int eventType) throws XMLStreamException {
        if (expectedElement != null) {
        	boolean matchesExpected = false;
        	Marker marker = startRecording();
        	int realEvent = 0;
        	QName encounteredName = null;
        	try {
        		realEvent = nextTag();
        		if (realEvent != END_DOCUMENT) {
        			if (realEvent == START_ELEMENT || realEvent == END_ELEMENT) {
        				encounteredName = getName();
        			}
		            if (realEvent == eventType) {	//should only be start- or end element
		                matchesExpected = expectedElement.equals(encounteredName);
		            }
        		}
        	} finally {
                if (!matchesExpected) {
                	rewindAndPlayback(marker);
                	if (logger.isTraceEnabled()) {
	                	logger.trace(String.format("Expected %s (%s), but found %s (%s %s)", EVENTNAMES[eventType], expectedElement,
	                			EVENTNAMES[realEvent], encounteredName, getRowColumn(getLocation())));
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
    
    /**
     * Read the text for the given element upto the end element tag. Next read will be the end element event.
     * @return the text of the text only element
     * @throws XMLStreamException
     */
    public String getElementText() throws XMLStreamException {
        if (!playbackQueue.isEmpty()) {
            this.currentEvent = playbackQueue.poll();
            if (logger.isTraceEnabled()) {
            	logger.trace(String.format("ParseEvent read from PlaybackQueue: %s", currentEvent));
            }
        } else {
            //read content of text-only element from staxReader
            if (getEvent() != START_ELEMENT) {
                throw new XMLStreamException("parser must be on START_ELEMENT to read text", getLocation());
            }
            
            QName currentTextElement = getName();
            
            int eventType = staxReader.next();
            StringBuffer content = new StringBuffer();
            while (eventType != END_ELEMENT) {
                if (eventType == CHARACTERS || eventType == CDATA || eventType == SPACE || eventType == ENTITY_REFERENCE) {
                    content.append(staxReader.getText());
                } else if (eventType == PROCESSING_INSTRUCTION || eventType == COMMENT) {
                    //ignore
                } else if (eventType == END_DOCUMENT) {
                	throw new XMLStreamException(String.format("Malformed xml; reached %s when reading text for <%s>", 
                			EVENTNAMES[eventType], currentTextElement), staxReader.getLocation());
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                	//mixed content is currently not supported
                	throw new XMLStreamException(String.format("Found %s <%s> while reading text for <%s>; mixed content is " +
                			"currently not supported %s", EVENTNAMES[eventType], staxReader.getName(), currentTextElement, 
                			getRowColumn(staxReader.getLocation())));
                } else {
                    throw new XMLStreamException(String.format("Unexpected %s", EVENTNAMES[eventType]), staxReader.getLocation());
                }
                eventType = staxReader.next();	//read END_ELEMENT
            }
            
            if (eventType == END_ELEMENT) {
            	if (!currentTextElement.equals(staxReader.getName())) {
            		throw new XMLStreamException(String.format("Malformed xml; expected end element </%s>, but encountered </%s>",
            				currentTextElement, staxReader.getName()), staxReader.getLocation());
            	}
            	playbackQueue.add(ParseEventData.newParseEventData(eventType, staxReader));	//push the end element on the playback queue
            }
            
            this.currentEvent = new ParseEventData(CHARACTERS, content.toString(), staxReader.getLocation());
            if (logger.isTraceEnabled()) {
            	logger.trace(String.format("ParseEvent read by StaxReader: %s", currentEvent));
            }
        }
        
        if (this.recordingQueue != null) {
            this.recordingQueue.add(this.currentEvent);
        }
        if (this.currentEvent.eventType != CHARACTERS) {
            throw new XMLStreamException("No element text could be read at this point in the stream");
        }
        
        return this.currentEvent.text;
    }

    /**
     * Cleanup the resources created by this object. This does not close the underlying {@link XMLStreamReader}; it's the 
     * responsibility of the creator of the {@link XMLStreamReader} to close it.
     */
    public void close() {
        clearAllRecordings();
        this.playbackQueue.clear();
    }
    
    public void close(boolean closeXmlStream) {
    	close();
    	if (closeXmlStream &&  (staxReader != null)) {
   			try {
				staxReader.close();
			} catch (XMLStreamException e) {
				logger.error("Exception occured when trying to close the underlying XMLStreamReader", e);
			}
    	}
    }
    
    private void clearAllRecordings() {
        if (this.recordingQueue != null) {
            this.recordingQueue.clear();
            this.recordingQueue = null;
        }
        this.publishedMarkers.clear();
    }
    
    private String getRowColumn(Location location) {
    	if (location == null) { return "@ Unknown line/column"; }
    	return String.format("@ line %d, column %d", location.getLineNumber(), location.getColumnNumber());
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
        private Map<QName, String> attributes = null;
        
        private ParseEventData(int eventType, String elementText, Location location) {
            this.eventType = eventType;
            this.text = elementText;
            this.location = location;
        }
        
        private ParseEventData(int eventType, QName elementName, Map<QName, String> attributes, Location location) {
            this.eventType = eventType;
            this.name = elementName;
            this.location = location;
            this.attributes = attributes;
        }
        
        private static ParseEventData newParseEventData(int eventType, XMLStreamReader staxReader) throws XMLStreamException {
            if ((eventType != START_ELEMENT) && (eventType != END_ELEMENT)) {
                throw new XMLStreamException("This type of event is currently unsupported: "+eventType);
            }
            Map<QName, String> attributes = null;
            if (eventType == START_ELEMENT) {
            	int attributeCount = staxReader.getAttributeCount();
            	if (attributeCount > 0) {
            		attributes = new HashMap<QName, String>(attributeCount);
	            	for (int i=0; i < attributeCount; i++) {
	            		attributes.put(staxReader.getAttributeName(i), staxReader.getAttributeValue(i));
	            	}
            	}
            }
            ParseEventData eventData = new ParseEventData(eventType, staxReader.getName(), attributes, staxReader.getLocation());
            return eventData;
        }
        
        
        @Override
        public String toString() {
            String data = text==null?name.toString():text;
            return String.format("ParseEventData[eventType=%S, data=%s, hashcode=%d]", EVENTNAMES[eventType], data, hashCode());
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
