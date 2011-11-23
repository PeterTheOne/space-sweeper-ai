package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.entity.Pose2D;

public class DestroyedEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("Destroyed");
	private CogaenId entityId;
	private CogaenId entityTypeId;
	private double x;
	private double y;
	private double angle;

	public DestroyedEvent(CogaenId entityId, CogaenId entityTypeId, Pose2D pose) {
		this.entityId = entityId;
		this.entityTypeId = entityTypeId;
		this.x = pose.getPosX();
		this.y = pose.getPosY();
		this.angle = pose.getAngle();
	}
	
	@Override
	public CogaenId getTypeId() {
		return TYPE_ID;
	}
	
	public CogaenId getEntityId() {
		return this.entityId;
	}

	public CogaenId getEntityTypeId() {
		return entityTypeId;
	}
	
	public double getPosX() {
		return this.x;
	}
	
	public double getPosY() {
		return this.y;
	}
	
	public double getAngle() {
		return this.angle;
	}
}
