package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class StageUpdateEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("StageUpdate");

	private int stage;
	
	public StageUpdateEvent(int stage) {
		this.stage = stage;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public int getStage() {
		return this.stage;
	}

}
