package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class FlowFieldUpdatedEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("FF Updated");

	private double[][][] field;
	
	public FlowFieldUpdatedEvent(double[][][] field) {
		this.field = field;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

	public double[][][] getField() {
		return this.field;
	}

}
