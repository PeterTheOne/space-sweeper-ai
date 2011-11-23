package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class CannonPowerUpEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("CoolerPowerUp");
	
	private int level;
	
	public CannonPowerUpEvent(int level) {
		this.level = level;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}

	public int getLevel() {
		return level;
	}

}
