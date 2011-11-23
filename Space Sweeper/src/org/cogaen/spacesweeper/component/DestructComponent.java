package org.cogaen.spacesweeper.component;

import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.Entity;
import org.cogaen.entity.EntityService;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.event.Event;
import org.cogaen.event.EventService;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.entity.RocketEntity;
import org.cogaen.spacesweeper.entity.ShipEntity;
import org.cogaen.spacesweeper.event.EnimyDestroyedEvent;
import org.cogaen.spacesweeper.physics.CollisionEvent;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class DestructComponent extends UpdateableComponent {

	private static final String DESTRUCTION_DELAY_PROP = "destructDelay";
	private static final double DEFAULT_DESTRUCTION_DELAY = 0.1;
	private boolean destroyed;
	private boolean destruct;
	private int numHits;
	private int cntHits;
	private Timer timer;
	private double destructionTime;
	
	public DestructComponent(int numHits) {
		super();
		this.numHits = numHits;
	}

	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
		this.timer = TimeService.getInstance(getCore()).getTimer();
	}

	@Override
	public void engage() {
		super.engage();
		this.cntHits = this.numHits;
		this.destroyed = false;
		this.destruct = false;
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		if (event.isOfType(CollisionEvent.TYPE)) {
			handleCollision((CollisionEvent) event);
		}
	}

	private void handleCollision(CollisionEvent event) {
		if (this.destroyed || this.destruct) {
			return;
		}
		
		Entity opponent = EntityService.getInstance(getCore()).getEntity(event.getOpponent(getParent().getId()));
		if (opponent.getType().equals(ShipEntity.TYPE)) {
			destroy();
		}
		
		if (opponent.getType().equals(RocketEntity.TYPE)) {
			this.cntHits = 0;
		} else {
			this.cntHits--;
		}
		
		if (this.cntHits <= 0) {
			this.destruct = true;
			double delay = PropertyService.getInstance(getCore()).getDoubleProperty(DESTRUCTION_DELAY_PROP, DEFAULT_DESTRUCTION_DELAY);
			this.destructionTime = this.timer.getTime() + delay;
		}
	}

	private void destroy() {
		this.destroyed = true;
		EntityService entSrv = EntityService.getInstance(getCore());
		entSrv.removeEntity(getParent().getId());
	}

	@Override
	public void update() {
		if (this.destroyed) {
			return;
		}
		
		if (!this.destruct || this.destructionTime > this.timer.getTime()) {
			return;
		}
		destroy();
		EventService.getInstance(getCore()).dispatchEvent(new EnimyDestroyedEvent(getParent().getType()));
	}

}
