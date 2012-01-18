package org.cogaen.spacesweeper.component;

import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.EntityService;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.event.Event;
import org.cogaen.event.EventService;
import org.cogaen.logging.LoggingService;
import org.cogaen.math.Vector2;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.entity.BigAsteroid;
import org.cogaen.spacesweeper.entity.MediumAsteroid;
import org.cogaen.spacesweeper.entity.SmallAsteroid;
import org.cogaen.spacesweeper.event.DestroyedEvent;
import org.cogaen.spacesweeper.event.TargetDeselectedEvent;
import org.cogaen.spacesweeper.event.TargetSelectedEvent;
import org.cogaen.spacesweeper.physics.Body;
import org.cogaen.spacesweeper.util.PidController;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class CruiseComponent extends UpdateableComponent {

	private static final double TARGET_DELAY = 0.25;
	private static final double SEARCH_ANGLE = Math.PI / 4;
	private static final double DEFAULT_MISSILE_SPEED = 15.0;
	private static final String MISSILE_SPEED_PROP = "missileSpeed";
	private CogaenId bodyAttrId;
	private Body body;
	private CogaenId targetId;
	private Body target;
	private PidController pid = new PidController(150.0, 20.0, 0);
	private Timer timer;
	private double timeStamp;
	private double speed;
	
	public CruiseComponent(CogaenId bodyAttrId) {
		super();
		this.bodyAttrId = bodyAttrId;
	}

	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
		this.body = (Body) getParent().getAttribute(this.bodyAttrId);
	}
	
	@Override
	public void engage() {
		super.engage();
		EventService.getInstance(getCore()).addListener(this, DestroyedEvent.TYPE_ID);
		this.speed = PropertyService.getInstance(getCore()).getDoubleProperty(MISSILE_SPEED_PROP, DEFAULT_MISSILE_SPEED);
		this.timer = TimeService.getInstance(getCore()).getTimer();
		this.pid.setTarget(1.0);
		this.timeStamp = this.timer.getTime() + TARGET_DELAY;
	}

	@Override
	public void disengage() {
		EventService.getInstance(getCore()).removeListener(this);
		if (this.targetId != null) {
			EventService.getInstance(getCore()).dispatchEvent(new TargetDeselectedEvent(this.targetId));
		}
		super.disengage();
	}

	@Override
	public void update() {
		if (this.target == null && this.timeStamp < this.timer.getTime()) {
			searchTarget();
			if (this.target != null) {
				LoggingService.getInstance(getCore()).logDebug("GAME", "cruise target selected: " + this.targetId);
			}
		}
		
		if (this.target == null) {
			this.body.setSpeed(this.speed);
			return;
		}
		
		// calculate interception point
		double dx = this.target.getPositionX() - this.body.getPositionX();
		double dy = this.target.getPositionY() - this.body.getPositionY();
		
		if (dx == 0 && dy == 0) {
			this.body.setSpeed(this.speed);
			return;
		}
		
		double dl = Math.sqrt(dx * dx + dy * dy);
		double vrx = this.target.getVelocityX() - this.body.getVelocityX();
		double vry = this.target.getVelocityY() - this.body.getVelocityY();
		double vrl = Math.sqrt(vrx * vrx + vry * vry);
		double t = dl / vrl;

		double px = this.target.getPositionX() + this.target.getVelocityX() * t;
		double py = this.target.getPositionY() + this.target.getVelocityY() * t;
		
		// transform target point into object space
		double tx = px - this.body.getPositionX();
		double ty = py - this.body.getPositionY();
		
		double txr = tx * Math.cos(-this.body.getAngularPosition()) - ty * Math.sin(-this.body.getAngularPosition());
		double tyr = ty * Math.cos(-this.body.getAngularPosition()) + tx * Math.sin(-this.body.getAngularPosition());
		
		double l = Math.sqrt(txr * txr + tyr * tyr);
		txr /= l;
		tyr /= l;
		
		double currentValue = txr * 0 + tyr * 1.0;
		
		
		this.pid.update(currentValue, this.timer.getDeltaTime());
		if (txr < 0) {
			this.body.setAngularAcceleration(this.pid.getOutput());			
		} else {
			this.body.setAngularAcceleration(-this.pid.getOutput());						
		}
		
		this.body.setSpeed(this.speed);
	}

	private void searchTarget() {
		EntityService entSrv = EntityService.getInstance(getCore());

		double minSquareDistance = Double.MAX_VALUE;
		for (int i = 0; i < entSrv.numEntities(); ++i) {
			ComponentEntity entity = (ComponentEntity) entSrv.getEntity(i);
			if (!entity.getType().equals(BigAsteroid.TYPE) && !entity.getType().equals(MediumAsteroid.TYPE) && !entity.getType().equals(SmallAsteroid.TYPE)) {
				continue;
			}
			Body body = (Body) entity.getAttribute(bodyAttrId);
			if (body == null) {
				continue;
			}
			
			if (!isTarget(body)) {
				continue;
			}
			
			double dx = body.getPositionX() - this.body.getPositionX();
			double dy = body.getPositionY() - this.body.getPositionY();
			double squareDistance = dx * dx + dy * dy;
			if (squareDistance < minSquareDistance) {
				this.target = body;
				this.targetId = entity.getId();
				minSquareDistance = squareDistance;
			}
		}

		if (this.target != null) {
			EventService.getInstance(getCore()).dispatchEvent(new TargetSelectedEvent(this.targetId));
		}
	}

//	private boolean isTarget(Body body) {
//		double vx = body.getPositionX() - this.body.getPositionX();
//		double vy = body.getPositionY() - this.body.getPositionY();
//		double l = Math.sqrt(vx * vx + vy * vy);
//		vx /= l;
//		vy /= l;
//
//		double dx = -Math.sin(this.body.getAngularPosition());
//		double dy = Math.cos(this.body.getAngularPosition());
//
//		double cosA = vx * dx + vy * dy;
//		return cosA > 0.90;
//	}

	private boolean isTarget(Body body) {
		// determine angle between target and missile and compare with reference value
		Vector2 v1 = new Vector2(body.getPositionX() - this.body.getPositionX(), body.getPositionY() - this.body.getPositionY());
		v1.normalize();
		Vector2 v2 = new Vector2(0, 1);
		v2.rotate(this.body.getAngularPosition());
		
		return Math.acos(v1.dot(v2)) <= SEARCH_ANGLE;
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		if (event.isOfType(DestroyedEvent.TYPE_ID)) {
			handleDestroyed((DestroyedEvent) event);
		}
	}

	private void handleDestroyed(DestroyedEvent event) {
		if (event.getEntityId().equals(this.targetId)) {
			this.target = null;
		}
	}
	
}
