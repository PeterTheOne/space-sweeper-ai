package org.cogaen.spacesweeper.entity;

import org.cogaen.core.Core;
import org.cogaen.entity.EntityService;
import org.cogaen.event.Event;
import org.cogaen.event.EventService;
import org.cogaen.name.CogaenId;
import org.cogaen.name.IdService;
import org.cogaen.spacesweeper.component.CruiseComponent;
import org.cogaen.spacesweeper.component.TimeToLiveComponent;
import org.cogaen.spacesweeper.event.BulletHitEvent;
import org.cogaen.spacesweeper.physics.Body;
import org.cogaen.spacesweeper.physics.CollisionEvent;

public class RocketEntity extends PhysicsEntity {

	public static final CogaenId TYPE = new CogaenId("Rocket");
	public static final double RADIUS = 0.1;
	private static final double TIME_TO_LIVE = 2.0;
	private static final double LINEAR_DAMPING = 1.0;
	private static final double START_SPEED = 1;
	private Body body; 
	private boolean destroyed;

	public RocketEntity(Core core) {
		super(core, IdService.getInstance(core).generateId(), TYPE, RADIUS);
		
		addComponent(new TimeToLiveComponent(TIME_TO_LIVE));
		addComponent(new CruiseComponent(PhysicsEntity.BodyAttrId));
		
		this.body = (Body) getAttribute(PhysicsEntity.BodyAttrId);
		this.body.setCollisionFlag(0x0002);
		this.body.setCollisionMask(0x0004);
	}
	
	public void initialize(double x, double y, double angle, double speed) {
		this.body.setPosition(x, y);
		this.body.setAngularPosition(angle);
		this.body.setSpeed(speed + START_SPEED);
		this.body.setLinearDamping(LINEAR_DAMPING);
		this.body.setAngularDamping(10);
	}
	
	@Override
	public void engage() {
		super.engage();
		this.destroyed = false;
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		
		if (event.isOfType(CollisionEvent.TYPE)) {
			EventService.getInstance(getCore()).dispatchEvent(new BulletHitEvent(getId()));
			destroy();
		} else if (event.isOfType(TimeToLiveComponent.END_OF_LIFE)) {
			destroy();
		}
	}
	
	private void destroy() {
		if (destroyed) {
			return;
		}
		EntityService.getInstance(getCore()).removeEntity(getId());		
		this.destroyed = true;
	}
}
