package org.cogaen.spacesweeper.entity;

import org.cogaen.name.CogaenId;

public interface OperationalAIInterface {
	
	//TODO: change name
	
	public static final CogaenId ATTR_ID = new CogaenId("OPERATIONAL_AI");

	public void setTargetAngle(double finalAngle);

	public void setTargetSpeed(double speed);

	public void setShoot(boolean shoot);
}
