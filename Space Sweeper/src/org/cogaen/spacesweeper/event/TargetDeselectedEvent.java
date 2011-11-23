package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class TargetDeselectedEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("TargetDeselected");

	private CogaenId targetId;
	
	public TargetDeselectedEvent(CogaenId targetId) {
		this.targetId = targetId;
	}

	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

	public CogaenId getTargetId() {
		return targetId;
	}

}
