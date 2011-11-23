package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class BulletHitEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("BulletHit");
	private CogaenId entityId;

	public BulletHitEvent(CogaenId entityId) {
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
