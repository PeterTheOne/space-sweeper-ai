package org.cogaen.spacesweeper.entity;

import org.cogaen.core.Core;
import org.cogaen.name.CogaenId;
import org.cogaen.name.IdService;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.component.DestructComponent;
import org.cogaen.spacesweeper.physics.Body;

public class SmallAsteroid extends PhysicsEntity {

	public static final CogaenId TYPE = new CogaenId("SmallAsteroid");
	public static final double RADIUS = 0.375;
	private static final int DEFAULT_HIT_COUNT = 2;
	private static final String HIT_COUNT_PROP = "smallAsteroidHitCount";
	
	private Body body;
	
	public SmallAsteroid(Core core) {
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
