package org.cogaen.spacesweeper.physics;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;

public class PositionUpdateEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("Moved");
	private CogaenId entityId;
	private double posX;
	private double posY;
	private double angle;

	public PositionUpdateEvent(CogaenId entityId, double x, double y, double angle) {
		this.entityId = entityId;
		this.posX = x;
		this.posY = y;
		this.angle = angle;
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public CogaenId getEntityId() {
		return this.entityId;
	}

	public double getPosX() {
		return posX;
	}

	public void setPosX(double posX) {
		this.posX = posX;
	}

	public double getPosY() {
		return posY;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}
	
}
