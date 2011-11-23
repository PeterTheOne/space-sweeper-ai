package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class EnimyDestroyedEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("AsteroidDestroyed");

	private CogaenId enimyType;
	
	public EnimyDestroyedEvent(CogaenId enimyType) {
		this.enimyType = enimyType;
	}

	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

	public CogaenId getEnimyType() {
		return enimyType;
	}

}
