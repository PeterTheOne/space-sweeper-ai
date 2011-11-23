package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class InvulerableEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("Invulnerable");

	private CogaenId entityId;
	private double timeFrame;
	
	public InvulerableEvent(CogaenId entityId, double timeFrame) {
		this.entityId = entityId;
		this.timeFrame = timeFrame;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public CogaenId getEntityid() {
		return this.entityId;
	}
	
	public double getTimeFrame() {
		return this.timeFrame;
	}

}
