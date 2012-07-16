package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class TargetChangeEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("Target Change");

	private double posX;
	private double posY;
	private double targetPosX;
	private double targetPosY;

	public TargetChangeEvent(double posX, double posY, double targetPosX, double targetPosY) {
		this.posX = posX;
		this.posY = posY;
		this.targetPosX = targetPosX;
		this.targetPosY = targetPosY;
	}

	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public double getPosX() {
		return this.posX;
	}
	
	public double getPosY() {
		return this.posY;
	}
	
	public double getTargetPosX() {
		return this.targetPosX;
	}
	
	public double getTargetPosY() {
		return this.targetPosY;
	}

}
