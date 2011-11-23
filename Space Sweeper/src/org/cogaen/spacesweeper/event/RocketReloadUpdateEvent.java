package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class RocketReloadUpdateEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("RocketReloadUpdate");

	private double reloadState;
	
	public RocketReloadUpdateEvent(double state) {
		this.reloadState = state;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public double getReloadState() {
		return this.reloadState;
	}
}
