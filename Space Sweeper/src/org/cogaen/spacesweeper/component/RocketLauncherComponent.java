package org.cogaen.spacesweeper.component;

import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.EntityService;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.lwjgl.input.ControllerState;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.entity.Pose2D;
import org.cogaen.spacesweeper.entity.RocketEntity;
import org.cogaen.spacesweeper.entity.ShipEntity;
import org.cogaen.spacesweeper.event.RocketReloadUpdateEvent;
import org.cogaen.spacesweeper.physics.Body;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class RocketLauncherComponent extends UpdateableComponent {

	public static final CogaenId ROCKET_FIRED_EVENT = new CogaenId("RocketFired");
	private static final double TIME_BETWEEN_SHOTS = 5.0;
	
	private EntityService entSrv;
	private EventService evtSrv;
	private ControllerState ctrl;
	private Timer timer;
	private Pose2D pose2d;
	private double shotTime;
	private int fireButton;
	private boolean fireButtonReleased;
	private Body body;
	private CogaenId bodyAttrId;
	
	public RocketLauncherComponent(int fireButton, CogaenId bodyAttrId) {
		this.fireButton = fireButton;
		this.bodyAttrId = bodyAttrId;
	}

	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
				
		this.entSrv = EntityService.getInstance(getCore());
		this.evtSrv = EventService.getInstance(getCore());
		this.timer = TimeService.getInstance(getCore()).getTimer();
		this.ctrl = (ControllerState) getParent().getAttribute(ControllerState.ID);
		this.pose2d = (Pose2D) getParent().getAttribute(Pose2D.ATTR_ID);
		this.body = (Body) getParent().getAttribute(this.bodyAttrId);
	}
	
	@Override
	public void engage() {
		super.engage();
		this.shotTime = this.timer.getTime();
		this.fireButtonReleased = true;
	}

	@Override
	public void update() {
		if (this.shotTime > this.timer.getTime()) {
			this.evtSrv.dispatchEvent(new RocketReloadUpdateEvent(1 - (this.shotTime - this.timer.getTime()) / TIME_BETWEEN_SHOTS));
		}
		
		// ensures that only one shot is fired per button press
		if (!this.ctrl.getButton(this.fireButton)) {
			this.fireButtonReleased = true;
			return;
		}
		
		if (!this.ctrl.getButton(this.fireButton) || !this.fireButtonReleased || this.shotTime > this.timer.getTime()) {
			return;
		}

		fireRocket();
		
		this.fireButtonReleased = false;
		this.shotTime = this.timer.getTime() + TIME_BETWEEN_SHOTS;
	}

	private double calcSpeed() {
		double phi = this.body.getAngularPosition();
		double vx = this.body.getVelocityX();
		double vy = this.body.getVelocityY();
		
		double x = -Math.sin(phi);
		double y = Math.cos(phi);
		
		return vx * x + vy * y;
	}
	
	private void fireRocket() {
		

		RocketEntity rocket = new RocketEntity(getCore());
		double offsetX = -ShipEntity.RADIUS * Math.sin(pose2d.getAngle());
		double offsetY = ShipEntity.RADIUS * Math.cos(pose2d.getAngle());

		rocket.initialize(this.pose2d.getPosX() + offsetX, this.pose2d.getPosY() + offsetY, this.pose2d.getAngle(), calcSpeed());
		
		this.entSrv.addEntity(rocket);			
		
		this.evtSrv.dispatchEvent(new SimpleEvent(ROCKET_FIRED_EVENT));
	}
}
