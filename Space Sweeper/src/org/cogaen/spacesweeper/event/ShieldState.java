package org.cogaen.spacesweeper.event;

import org.cogaen.name.CogaenId;

public interface ShieldState {

	public static final CogaenId ATTR_ID = new CogaenId("SHIELD_STATE");
	
	public double getState();
}
