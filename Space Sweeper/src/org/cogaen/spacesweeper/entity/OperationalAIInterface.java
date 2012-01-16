package org.cogaen.spacesweeper.entity;

import org.cogaen.name.CogaenId;

public interface OperationalAIInterface {
	
	//TODO: change name
	
	public static final CogaenId ATTR_ID = new CogaenId("OPERATIONAL_AI");
	
	public void setTarget(double targetPosX, double targetPosY);

	public void setTargetSpeed(double speed);
}
