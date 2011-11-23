package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class ScoreUpdateEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("ScoreUpdate");

	private int score;
	
	public ScoreUpdateEvent(int score) {
		this.score = score;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public int getScore() {
		return this.score;
	}

}
