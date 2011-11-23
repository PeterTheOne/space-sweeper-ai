package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class ThrustEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("Thrust");
	private CogaenId entityId;
	private double thrust;

	public ThrustEvent(CogaenId entityId, double thrust) {
		this.entityId = entityId;
		this.thrust = thrust;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public CogaenId getEntityId() {
		return this.entityId;
	}
	
	public double getThrust() {
		return this.thrust;
	}

}
