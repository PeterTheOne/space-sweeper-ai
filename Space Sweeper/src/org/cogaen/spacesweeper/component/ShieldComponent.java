package org.cogaen.spacesweeper.component;

import org.cogaen.entity.Component;
import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.Entity;
import org.cogaen.entity.EntityService;
import org.cogaen.event.Event;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.logging.LoggingService;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.entity.Invulnerable;
import org.cogaen.spacesweeper.entity.PowerUp;
import org.cogaen.spacesweeper.event.ShieldState;
import org.cogaen.spacesweeper.event.ShieldUpdateEvent;
import org.cogaen.spacesweeper.physics.CollisionEvent;

public class ShieldComponent extends Component implements ShieldState {

	public static final CogaenId SHIELD_POWERUP_EVENT = new CogaenId("ShieldPowerup");
	private static final String DAMAGE_PER_HIT_PROP = "damagePerHit";
	private static final double DEFAULT_DAMAGE_PER_HIT = 0.2;
	
	private EventService evtSrv;
	private Invulnerable invulnerable;
	private double state;
	private double damagePerHit;
	
	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
		getParent().addAttribute(ShieldState.ATTR_ID, this);
		
		PropertyService propSrv = PropertyService.getInstance(getCore());
		this.damagePerHit = propSrv.getDoubleProperty(DAMAGE_PER_HIT_PROP, DEFAULT_DAMAGE_PER_HIT);
		this.evtSrv = EventService.getInstance(getCore());
	}

	@Override
	public void engage() {
		super.engage();
		this.invulnerable = (Invulnerable) getParent().getAttribute(Invulnerable.ATTR_ID);
		this.state = 1.0;
		this.evtSrv.dispatchEvent(new ShieldUpdateEvent(this.state));		
	}

	@Override
	public void disengage() {
		this.invulnerable = null;
		super.disengage();
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		if (event.isOfType(CollisionEvent.TYPE)) {
			handleCollision((CollisionEvent) event);
		}
	}

	private void handleCollision(CollisionEvent event) {
		if (this.invulnerable.isInvulnerable()) {
			return;
		}
		
		EntityService entSrv = EntityService.getInstance(getCore());
		Entity opponent = entSrv.getEntity(event.getOpponent(getParent().getId()));
		if (opponent.getType().equals(PowerUp.CANNON_POWERUP1_ID) || opponent.getType().equals(PowerUp.CANNON_POWERUP2_ID) 
			|| opponent.getType().equals(PowerUp.COOLER_POWERUP_ID)) {
			return;
		}

		if (opponent.getType().equals(PowerUp.SHIELD_POWERUP_ID)) {
			if (this.state < 1.0) {
				this.state = 1.0;
				EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(SHIELD_POWERUP_EVENT));
				LoggingService.getInstance(getCore()).logInfo("GAME", "shield set " + this.state);
				EntityService.getInstance(getCore()).removeEntity(opponent.getId());
			}
		} else {
			this.state = Math.max(0, this.state - this.damagePerHit);			
		}
				
		this.evtSrv.dispatchEvent(new ShieldUpdateEvent(this.state));
	}

	@Override
	public double getState() {
		return this.state;
	}

}
