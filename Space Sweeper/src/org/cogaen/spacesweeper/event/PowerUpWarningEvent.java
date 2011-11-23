package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class PowerUpWarningEvent extends Event {
	
	public static final CogaenId TYPE_ID = new CogaenId("PowerUpWarning");
	private CogaenId entityId;

	public PowerUpWarningEvent(CogaenId entityId) {
		this.entityId = entityId;
	}

	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

	public CogaenId getEntityId() {
		return entityId;
	}

}
