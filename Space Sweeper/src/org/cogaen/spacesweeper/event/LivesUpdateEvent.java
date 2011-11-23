package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class LivesUpdateEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("LivesUpdate");

	private int lives;
	
	public LivesUpdateEvent(int lives) {
		this.lives = lives;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public int getLives() {
		return this.lives;
	}

}
