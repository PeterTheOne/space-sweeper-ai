package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class GroupLoadUpdateEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("GroupLoadUpdate");
	
	private CogaenId groupId;
	private double percentage;
	
	public GroupLoadUpdateEvent(CogaenId groupId, double percentage) {
		super();
		this.groupId = groupId;
		this.percentage = percentage;
	}

	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

	public double getPercentage() {
		return percentage;
	}

	public CogaenId getGroupId() {
		return groupId;
	}
	
	public boolean isLoaded() {
		return this.percentage == 1.0;
	}
}
