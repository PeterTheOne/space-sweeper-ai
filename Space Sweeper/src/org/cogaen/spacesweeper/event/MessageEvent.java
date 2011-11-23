package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class MessageEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("Message");

	private String message;
	
	public MessageEvent(String message) {
		this.message = message;
	}

	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

	public String getMessage() {
		return message;
	}

}
