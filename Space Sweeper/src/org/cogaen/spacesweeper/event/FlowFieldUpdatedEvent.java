package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.state.FlowField;

public class FlowFieldUpdatedEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("FF Updated");

	private FlowField flowField;
	
	public FlowFieldUpdatedEvent(FlowField flowField) {
		this.flowField = flowField;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

	public FlowField getFlowField() {
		return this.flowField;
	}

}
