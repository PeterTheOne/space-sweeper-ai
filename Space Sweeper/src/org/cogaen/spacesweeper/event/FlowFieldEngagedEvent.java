package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class FlowFieldEngagedEvent extends Event {
	
	public static final CogaenId TYPE_ID = new CogaenId("FF Engaged");

	private int horizontalCount;
	private int verticalCount;
	
	public FlowFieldEngagedEvent(int horizontalCount, int verticalCount) {
		this.horizontalCount = horizontalCount;
		this.verticalCount = verticalCount;
	}
	
	public int getHorizontalCount() {
		return this.horizontalCount;
	}
	
	public int getVerticalCount() {
		return this.verticalCount;
	}

	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

}
