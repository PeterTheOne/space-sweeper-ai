package org.cogaen.spacesweeper.entity;

import org.cogaen.core.Core;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.physics.CircleBodyComponent;

public class PhysicsEntity extends BaseEntity {

	public static final CogaenId BodyAttrId = new CogaenId("BODY");
	
	public PhysicsEntity(Core core, CogaenId id, CogaenId typeId, double radius) {
		super(core, id, typeId);
		
		addPhysics(radius);
	}

	private void addPhysics(double radius) {
		addComponent(new CircleBodyComponent(BodyAttrId, radius));
	}
	
}
