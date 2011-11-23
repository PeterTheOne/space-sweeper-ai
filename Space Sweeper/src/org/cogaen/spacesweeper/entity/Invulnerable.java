package org.cogaen.spacesweeper.entity;

import org.cogaen.name.CogaenId;

public interface Invulnerable {

	public static final CogaenId ATTR_ID = new CogaenId("INVULNERABLE");
	
	public boolean isInvulnerable();
	public void makeInvulnerable(double time);
}
