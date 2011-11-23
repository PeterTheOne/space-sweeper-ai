package org.cogaen.spacesweeper.entity;

import org.cogaen.action.EventAction;
import org.cogaen.core.Core;
import org.cogaen.entity.ActionComponent;
import org.cogaen.entity.EntityService;
import org.cogaen.event.Event;
import org.cogaen.name.CogaenId;
import org.cogaen.name.IdService;
import org.cogaen.spacesweeper.component.TimeToLiveComponent;
import org.cogaen.spacesweeper.event.PowerUpWarningEvent;
import org.cogaen.spacesweeper.physics.Body;

public class PowerUp extends PhysicsEntity {

	public static final CogaenId CANNON_POWERUP1_ID = new CogaenId("CannonPowerUp1");
	public static final CogaenId CANNON_POWERUP2_ID = new CogaenId("CannonPowerUp2");
	public static final CogaenId SHIELD_POWERUP_ID = new CogaenId("ShieldPowerUp");
	public static final CogaenId COOLER_POWERUP_ID = new CogaenId("CoolerPowerUp");
	public static final double RADIUS = 0.75;
	public static final CogaenId POWERUP_WARNING = new CogaenId("PowerUpWarning");
	private static final double TIME_TO_LIVE = 10;
	
	private Body body;
	
	public PowerUp(Core core, CogaenId typeId) {
		super(core, IdService.getInstance(core).generateId(), typeId, RADIUS);
		
		addComponent(new TimeToLiveComponent(TIME_TO_LIVE));

		EventAction eventAction = new EventAction(core, new PowerUpWarningEvent(getId()));
		ActionComponent actionComponent = new ActionComponent(eventAction, TIME_TO_LIVE * 0.75, false);
		addComponent(actionComponent);
		
		this.body = (Body) getAttribute(PhysicsEntity.BodyAttrId);
		this.body.setCollisionFlag(0x0008);
		this.body.setCollisionMask(0x0001);
	}

	public void initialize(double x, double y, double angle, double speed) {
		this.body.setPosition(x, y);
		this.body.setAngularPosition(angle);
		this.body.setSpeed(speed);
	}
	
	@Override
	public void engage() {
		super.engage();
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		
		if (event.isOfType(TimeToLiveComponent.END_OF_LIFE)) {
			EntityService.getInstance(getCore()).removeEntity(getId());		
		}
	}
}
