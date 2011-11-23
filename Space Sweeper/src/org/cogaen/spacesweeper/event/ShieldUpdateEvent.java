package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class ShieldUpdateEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("ShieldUpdate");

	private double shieldState;
	
	public ShieldUpdateEvent(double state) {
		this.shieldState = state;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public double getShieldState() {
		return this.shieldState;
	}
}
