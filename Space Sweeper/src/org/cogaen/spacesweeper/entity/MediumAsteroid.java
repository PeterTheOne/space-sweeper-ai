package org.cogaen.spacesweeper.entity;

import org.cogaen.core.Core;
import org.cogaen.name.CogaenId;
import org.cogaen.name.IdService;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.component.DestructComponent;
import org.cogaen.spacesweeper.physics.Body;

public class MediumAsteroid extends PhysicsEntity {

	private static final int DEFAULT_HIT_COUNT = 4;
	private static final String HIT_COUNT_PROP = "mediumAsteroidHitCount";
	public static final CogaenId TYPE = new CogaenId("MediumAsteroid");
	public static final double RADIUS = 0.75;

	private Body body;
	
	public MediumAsteroid(Core core) {
		super(core, IdService.getInstance(core).generateId(), TYPE, RADIUS);

		addComponent(new DestructComponent(PropertyService.getInstance(getCore()).getIntProperty(HIT_COUNT_PROP, DEFAULT_HIT_COUNT)));
		this.body = (Body) getAttribute(PhysicsEntity.BodyAttrId);
		this.body.setCollisionFlag(0x0004);
		this.body.setCollisionMask(0xFFFF & ~0x0004);
	}


	public void initialize(double x, double y, double angle, double speed, double omega) {
		this.body.setPosition(x, y);
		this.body.setAngularPosition(angle);
		this.body.setSpeed(speed);
		this.body.setAngularVelocity(omega);
	}
	
	@Override
	public void engage() {
		super.engage();
	}
}
