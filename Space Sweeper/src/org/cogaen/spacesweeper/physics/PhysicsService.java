package org.cogaen.spacesweeper.physics;

import java.util.ArrayList;
import java.util.List;

import org.cogaen.core.AbstractService;
import org.cogaen.core.Core;
import org.cogaen.core.ServiceException;
import org.cogaen.core.Updateable;
import org.cogaen.entity.EntityService;
import org.cogaen.event.EventService;
import org.cogaen.name.CogaenId;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class PhysicsService extends AbstractService implements Updateable {

	public static final CogaenId ID = new CogaenId("org.cogaen.spacesweeper.physics.PhysicsService");
	public static final String NAME = "Space Sweeper Physics Service";

	private List<Body> bodies = new ArrayList<Body>();
	private CogaenId timerId;
	private Timer timer;
	private EventService evtSrv;
	private EntityService entSrv;
	private double halfWidth = 100;
	private double halfHeight = 100;

	public static PhysicsService getInstance(Core core) {
		return (PhysicsService) core.getService(ID);
	}
	
	public PhysicsService() {
		this(TimeService.DEFAULT_TIMER_ID);
	}
	
	public PhysicsService(CogaenId timerId) {
		addDependency(TimeService.ID);
		addDependency(EventService.ID);
		
		this.timerId = timerId;
	}
	
	@Override
	public CogaenId getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	protected void doPause() {
		getCore().removeUpdateable(this);
		super.doPause();
	}

	@Override
	protected void doResume() {
		getCore().addUpdateable(this);
		super.doResume();
	}

	@Override
	protected void doStart() throws ServiceException {
		super.doStart();
		getCore().addUpdateable(this);
		
		this.timer = TimeService.getInstance(getCore()).getTimer(this.timerId);
		this.evtSrv = EventService.getInstance(getCore());
		this.entSrv = EntityService.getInstance(getCore());
	}

	@Override
	protected void doStop() {
		if (getStatus() != Status.PAUSED) {
			getCore().removeUpdateable(this);
		}
		super.doStop();
	}

	@Override
	public void update() {
		double dt = this.timer.getDeltaTime();
		for (Body body : this.bodies) {
			body.update(dt);
			if (body.getPositionX() > this.halfWidth) {
				body.setPosition(-this.halfWidth, body.getPositionY());
			} else if (body.getPositionX() < - this.halfWidth) {
				body.setPosition(this.halfWidth, body.getPositionY());
			}
			if (body.getPositionY() > this.halfHeight) {
				body.setPosition(body.getPositionX(), -this.halfHeight);
			} else if (body.getPositionY() < - this.halfHeight) {
				body.setPosition(body.getPositionX(), this.halfHeight);
			}
			
			this.evtSrv.dispatchEvent(new PositionUpdateEvent(body.getId(), body.getPositionX(), body.getPositionY(), body.getAngularPosition()));
		}
		doCollisionTest();
	}

	public void addBody(Body body) {
		this.bodies.add(body);
	}
	
	public void removeBody(Body body) {
		this.bodies.remove(body);
	}
	
	private void doCollisionTest() {
		for (int i = 0; i < this.bodies.size(); ++i) {
			Body b1 = this.bodies.get(i);
			for (int j = i + 1; j < this.bodies.size(); ++j) {
				Body b2 = this.bodies.get(j);
				
				if (b1.isCollisionAllowed(b2) && b1.isColliding(b2)) {
					this.entSrv.getEntity(b1.getId()).handleEvent(new CollisionEvent(b1.getId(),  b2.getId()));
					this.entSrv.getEntity(b2.getId()).handleEvent(new CollisionEvent(b1.getId(),  b2.getId()));
				}
			}
		}		
	}
	
	public void setWorldSize(double width, double height) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("world dimensions must be greater zero");
		}
		
		this.halfWidth = width / 2;
		this.halfHeight = height / 2;
	}
}
