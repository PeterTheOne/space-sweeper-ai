package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class CoolerPowerUpEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("CannonPowerUp");
	
	private int level;
	
	public CoolerPowerUpEvent(int level) {
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
