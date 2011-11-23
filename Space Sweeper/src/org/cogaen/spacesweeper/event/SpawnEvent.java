package org.cogaen.spacesweeper.event;

import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.entity.Pose2D;

public class SpawnEvent extends Event {

	public static final CogaenId TYPE_ID = new CogaenId("Spawn");
	private CogaenId entityId;
	private CogaenId entityType;
	private double x;
	private double y;
	private double angle;

	public SpawnEvent(CogaenId entityId, CogaenId entityType, Pose2D pose) {
		this.entityId = entityId;
		this.entityType = entityType;
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
	
	public CogaenId getEntityType() {
		return this.entityType;
	}
	
	public boolean isEntityType(CogaenId entityType) {
		return this.entityType.equals(entityType);
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
