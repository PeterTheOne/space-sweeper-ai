package org.cogaen.spacesweeper.entity;

import org.cogaen.core.Core;
import org.cogaen.entity.Entity;
import org.cogaen.entity.EntityService;
import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.component.OperationalAIComponent;
import org.cogaen.spacesweeper.component.CannonComponent;
import org.cogaen.spacesweeper.component.InvulnerabilityComponent;
import org.cogaen.spacesweeper.component.MotionComponent;
import org.cogaen.spacesweeper.component.RocketLauncherComponent;
import org.cogaen.spacesweeper.component.ShieldComponent;
import org.cogaen.spacesweeper.component.TacticalAIComponent;
import org.cogaen.spacesweeper.event.ShieldState;
import org.cogaen.spacesweeper.physics.Body;
import org.cogaen.spacesweeper.physics.CollisionEvent;

public class AIShipEntity extends PhysicsEntity {


	public static final CogaenId TYPE = new CogaenId("AIShip");
	public static final double RADIUS = 0.8;
	private static final double INVULNERABILITY_TIME = 3.0;
	private static final double INVULNERABLE_HIT_TIME = 0.15;
	private static final int FIRE_BUTTON = 0;
	private static final int ROCKET_BUTTON = 1;
	private static final int NUM_BUTTONS = 2;
	
	private Body body; 
	private boolean destroyed;
	private ShieldState shieldState;
	private Invulnerable invulnerable;
	
	public AIShipEntity(Core core, CogaenId id) {
		super(core, id, TYPE, RADIUS);
		
		addComponent(new MotionComponent(PhysicsEntity.BodyAttrId));
		addComponent(new OperationalAIComponent(NUM_BUTTONS, PhysicsEntity.BodyAttrId));
		addComponent(new TacticalAIComponent(PhysicsEntity.BodyAttrId));
		addComponent(new CannonComponent(FIRE_BUTTON, PhysicsEntity.BodyAttrId));
		addComponent(new InvulnerabilityComponent(INVULNERABILITY_TIME));
		addComponent(new ShieldComponent());
		addComponent(new RocketLauncherComponent(ROCKET_BUTTON, PhysicsEntity.BodyAttrId));
		
		this.body = (Body) getAttribute(PhysicsEntity.BodyAttrId);
		this.body.setCollisionFlag(0x0001);
		this.shieldState = (ShieldState) getAttribute(ShieldState.ATTR_ID);
		
		this.invulnerable = (Invulnerable) getAttribute(Invulnerable.ATTR_ID);
	}
		
	public void initialize(double x, double y) {
		this.body.setPosition(x, y);
		this.body.setAngularPosition(0);
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		
		if (event.isOfType(CollisionEvent.TYPE)) {
			handleCollision((CollisionEvent) event);
		}
	}

	private void handleCollision(CollisionEvent event) {
		
		if (this.destroyed) {
			return;
		}

		Entity entity = EntityService.getInstance(getCore()).getEntity(event.getOpponent(getId()));
		if (entity.getType().equals(PowerUp.CANNON_POWERUP1_ID)
			|| entity.getType().equals(PowerUp.CANNON_POWERUP2_ID)
			|| entity.getType().equals(PowerUp.SHIELD_POWERUP_ID)
			|| entity.getType().equals(PowerUp.COOLER_POWERUP_ID)) {
			return;
		}
		
		if (this.shieldState.getState() > 0.0) {
			this.invulnerable.makeInvulnerable(INVULNERABLE_HIT_TIME);
			return;
		}
		
		this.destroyed = true;
		EntityService.getInstance(getCore()).removeEntity(getId());
	}
	
}
