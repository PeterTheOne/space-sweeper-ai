package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class HeatUpdateEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("HeatUpdate");

	private double heatState;
	
	public HeatUpdateEvent(double state) {
		this.heatState = state;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public double getHeatState() {
		return this.heatState;
	}
}
