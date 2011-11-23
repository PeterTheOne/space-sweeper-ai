package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class SoundEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("Sound");

	private String soundResource;
	
	public SoundEvent(String soundResource) {
		this.soundResource = soundResource;
	}

	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

	public String getSoundResource() {
		return soundResource;
	}

}
