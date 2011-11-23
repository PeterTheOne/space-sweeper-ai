package org.cogaen.spacesweeper.component;

import java.util.Random;

import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.Entity;
import org.cogaen.entity.EntityService;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.event.Event;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.input.ControllerState;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.entity.BulletEntity;
import org.cogaen.spacesweeper.entity.Pose2D;
import org.cogaen.spacesweeper.entity.PowerUp;
import org.cogaen.spacesweeper.entity.ShipEntity;
import org.cogaen.spacesweeper.event.CannonPowerUpEvent;
import org.cogaen.spacesweeper.event.CoolerPowerUpEvent;
import org.cogaen.spacesweeper.event.HeatUpdateEvent;
import org.cogaen.spacesweeper.physics.Body;
import org.cogaen.spacesweeper.physics.CollisionEvent;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class CannonComponent extends UpdateableComponent {

	public static final CogaenId SHOT_FIRED_EVENT = new CogaenId("ShotFired");
	public static final CogaenId OVERHEAT_EVENT = new CogaenId("Overheat");
	private static final double TIME_BETWEEN_SHOTS = 0.15;
	private static final double DEFAULT_BULLET_SPEED = 17.5;
	private static final double DEFAULT_BULLET_SPREAD = 0.1;
	private static final String BULLET_SPREAD_PROP = "bulletSpread";
	private static final String BULLET_SPEED_PROP = "bulletSpeed";
	private static final String BULLET_SPEED_VARIANCE = "bulletSpeedVariance";
	private static final double DEFAULT_BULLET_SPEED_VARIANCE = 0.25;
	private static final String HEAT_PER_SHOT_PROP = "heatPerShot";
	private static final double DEFAULT_HEAT_PER_SHOT = 0.05;
	private static final String COOL_DOWN_TIME_PROP = "coolDown";
	private static final double DEFAULT_COOL_DOWN_TIME = 1.0 / 10;
	private static final String HEAT_LIMIT_PROP = "heatLimit";
	private static final double DEFAULT_HEAT_LIMIT = 0.95;
	private static final int DEFAULT_SHOTS_PER_KEY = 4;
	private static final String SHOTS_PER_KEY_PROP = "shotsPerKey";
	
	private static Random random = new Random();
	
	private EntityService entSrv;
	private EventService evtSrv;
	private ControllerState ctrl;
	private Timer timer;
	private Pose2D pose2d;
	private double shotTime;
	private double heat;
	private int fireButton;
	private int powerUp;
	private int coolerPowerUp;
	private boolean fireButtonReleased;
	private double bulletSpeed;
	private double bulletSpeedVariance;
	private double bulletSpread;
	private double heatPerShot;
	private double coolDown;
	private double heatLimit;
	private Body body;
	private CogaenId bodyAttrId;
	private int cntShots;
	private int numShotsPerKey;
	
	public CannonComponent(int fireButton, CogaenId bodyAttrId) {
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

		PropertyService propSrv = PropertyService.getInstance(getCore());
		this.bulletSpeed = propSrv.getDoubleProperty(BULLET_SPEED_PROP, DEFAULT_BULLET_SPEED);
		this.bulletSpeedVariance = propSrv.getDoubleProperty(BULLET_SPEED_VARIANCE, DEFAULT_BULLET_SPEED_VARIANCE);
		this.bulletSpread = propSrv.getDoubleProperty(BULLET_SPREAD_PROP, DEFAULT_BULLET_SPREAD);
		this.heatPerShot = propSrv.getDoubleProperty(HEAT_PER_SHOT_PROP, DEFAULT_HEAT_PER_SHOT);
		this.coolDown = propSrv.getDoubleProperty(COOL_DOWN_TIME_PROP, DEFAULT_COOL_DOWN_TIME);
		this.heatLimit = propSrv.getDoubleProperty(HEAT_LIMIT_PROP, DEFAULT_HEAT_LIMIT);
		this.numShotsPerKey = propSrv.getIntProperty(SHOTS_PER_KEY_PROP, DEFAULT_SHOTS_PER_KEY);
		
		this.shotTime = this.timer.getTime();
		this.powerUp = 0;
		this.coolerPowerUp = 0;
		this.heat = 0;
		this.fireButtonReleased = true;
		this.cntShots = 0;
		
		this.evtSrv.dispatchEvent(new CannonPowerUpEvent(this.powerUp));
		this.evtSrv.dispatchEvent(new CoolerPowerUpEvent(this.coolerPowerUp));
	}

	@Override
	public void update() {
		// calculate current heat
		this.heat = Math.max(this.heat - this.timer.getDeltaTime() * this.coolDown, 0);
		this.evtSrv.dispatchEvent(new HeatUpdateEvent(this.heat));

		// ensures that only one shot is fired per button press
		if (!this.ctrl.getButton(this.fireButton)) {
			this.fireButtonReleased = true;
			this.cntShots = 0;
			return;
		}
		
		if (!this.ctrl.getButton(this.fireButton) || (!this.fireButtonReleased && this.cntShots >= this.numShotsPerKey) || this.shotTime > this.timer.getTime()) {
			return;
		}

		if (heat < this.heatLimit) {
			fireBullet();
			this.cntShots++;
			heat = Math.min(1, heat + this.heatPerShot);
		} else {
			this.evtSrv.dispatchEvent(new SimpleEvent(OVERHEAT_EVENT));
		}
		
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
	
	private void fireBullet() {
		
		if (this.powerUp == 2) {
			double delta = this.bulletSpread / 2;
			for (int i = 0; i < 3; ++i)  {
				BulletEntity bullet = new BulletEntity(getCore());
				double offsetX = -ShipEntity.RADIUS * Math.sin(pose2d.getAngle());
				double offsetY = ShipEntity.RADIUS * Math.cos(pose2d.getAngle());
				double speed = calcSpeed() + this.bulletSpeed - this.bulletSpeedVariance / 2 + CannonComponent.random.nextDouble() * this.bulletSpeedVariance * 2;
				bullet.initialize(this.pose2d.getPosX() + offsetX, this.pose2d.getPosY() + offsetY, this.pose2d.getAngle() - this.bulletSpread / 2 + delta * i, speed);
				this.entSrv.addEntity(bullet);										
			}
		} else if (this.powerUp == 1) {			
			
			double delta = this.bulletSpread;
			for (int i = 0; i < 2; ++i)  {
				BulletEntity bullet = new BulletEntity(getCore());
				double offsetX = -ShipEntity.RADIUS * Math.sin(pose2d.getAngle());
				double offsetY = ShipEntity.RADIUS * Math.cos(pose2d.getAngle());
				double speed = calcSpeed() + this.bulletSpeed - this.bulletSpeedVariance / 2 + CannonComponent.random.nextDouble() * this.bulletSpeedVariance * 2;
				bullet.initialize(this.pose2d.getPosX() + offsetX, this.pose2d.getPosY() + offsetY, this.pose2d.getAngle() - this.bulletSpread / 2 + delta * i, speed);
				this.entSrv.addEntity(bullet);										
			}
		} else {
			BulletEntity bullet = new BulletEntity(getCore());
			double offsetX = -ShipEntity.RADIUS * Math.sin(pose2d.getAngle());
			double offsetY = ShipEntity.RADIUS * Math.cos(pose2d.getAngle());
			double speed = calcSpeed() + this.bulletSpeed - this.bulletSpeedVariance / 2 + CannonComponent.random.nextDouble() * this.bulletSpeedVariance * 2;

			bullet.initialize(this.pose2d.getPosX() + offsetX, this.pose2d.getPosY() + offsetY, this.pose2d.getAngle(), speed);
			
			this.entSrv.addEntity(bullet);			
		}
		
		this.evtSrv.dispatchEvent(new SimpleEvent(SHOT_FIRED_EVENT));
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		
		if (event.isOfType(CollisionEvent.TYPE)) {
			handleCollision((CollisionEvent) event);
		}
	}

	private void handleCollision(CollisionEvent event) {
		Entity opponent = EntityService.getInstance(getCore()).getEntity(event.getOpponent(getParent().getId()));
		if (opponent.getType().equals(PowerUp.CANNON_POWERUP1_ID)) {
			if (this.powerUp == 0) {
				this.powerUp++;
				EventService.getInstance(getCore()).dispatchEvent(new CannonPowerUpEvent(this.powerUp));
				LoggingService.getInstance(getCore()).logInfo("GAME", "cannon set to level " + this.powerUp);
				EntityService.getInstance(getCore()).removeEntity(opponent.getId());
			}
		} if (opponent.getType().equals(PowerUp.CANNON_POWERUP2_ID)) {
			if (this.powerUp == 1) {
				this.powerUp++;
				EventService.getInstance(getCore()).dispatchEvent(new CannonPowerUpEvent(this.powerUp));
				LoggingService.getInstance(getCore()).logInfo("GAME", "cannon set to level " + this.powerUp);
				EntityService.getInstance(getCore()).removeEntity(opponent.getId());
			}
		} else if (opponent.getType().equals(PowerUp.COOLER_POWERUP_ID)) {
			if (this.coolerPowerUp == 0) {
				this.coolerPowerUp++;
				this.coolDown *= 1.5;
				EventService.getInstance(getCore()).dispatchEvent(new CoolerPowerUpEvent(this.coolerPowerUp));
				LoggingService.getInstance(getCore()).logInfo("GAME", "cooler set to level " + this.coolerPowerUp);
				EntityService.getInstance(getCore()).removeEntity(opponent.getId());				
			}
		}
	}
}
