package org.cogaen.spacesweeper.physics;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class CollisionEvent extends Event {

	public static final CogaenId TYPE = new CogaenId("CollisionEvent");

	private CogaenId entityId1;
	private CogaenId entityId2;
	
	public CollisionEvent(CogaenId entityId1, CogaenId entityId2) {
		this.entityId1 = entityId1;
		this.entityId2 = entityId2;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE;
	}
	
	public CogaenId getEntityId1() {
		return this.entityId1;
	}
	
	public CogaenId getEntityId2() {
		return this.entityId2;
	}
	
	public CogaenId getOpponent(CogaenId entityId) {
		assert(this.entityId1.equals(entityId) || this.entityId2.equals(entityId));
		return entityId1.equals(entityId) ? this.entityId2 : this.entityId1;
	}

}
