package org.cogaen.spacesweeper.state;

import java.util.Random;

import org.cogaen.core.Core;
import org.cogaen.core.Engageable;
import org.cogaen.entity.EntityService;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.spacesweeper.entity.AIShipEntity;
import org.cogaen.spacesweeper.entity.BigAsteroid;
import org.cogaen.spacesweeper.entity.MediumAsteroid;
import org.cogaen.spacesweeper.entity.PowerUp;
import org.cogaen.spacesweeper.entity.ShipEntity;
import org.cogaen.spacesweeper.entity.SmallAsteroid;
import org.cogaen.spacesweeper.event.CannonPowerUpEvent;
import org.cogaen.spacesweeper.event.CoolerPowerUpEvent;
import org.cogaen.spacesweeper.event.DestroyedEvent;
import org.cogaen.spacesweeper.event.EnimyDestroyedEvent;
import org.cogaen.spacesweeper.event.LivesUpdateEvent;
import org.cogaen.spacesweeper.event.ScoreUpdateEvent;
import org.cogaen.spacesweeper.event.StageUpdateEvent;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class GameLogic implements Engageable, EventListener {

	public static final CogaenId PLAYER_ONE_ID = new CogaenId("Player1");
	public static final CogaenId AI_PLAYER_ONE_ID = new CogaenId("AIPlayer1");
	public static final CogaenId NEW_SHIP = new CogaenId("NewShip");
	public static final CogaenId NEW_AI_SHIP = new CogaenId("NewAIShip");
	public static final CogaenId NEW_STAGE = new CogaenId("NewStage");
	public static final CogaenId POWER_UP = new CogaenId("PowerUp");
	public static final CogaenId GAME_OVER = new CogaenId("GameOver");
	public static final CogaenId TRIPLE_KILL = new CogaenId("TripleKill");;
	
	private static final String LOGGING_SOURCE = "GAME";
	private static final int BIG_ASTEROID_SCORE = 15;
	private static final int MEDIUM_ASTEROID_SCORE = 10;
	private static final int SMALL_ASTEROID_SCORE = 5;
	private static final int NUM_DEBRIS = 4;
	private static final int NUM_LIVES = 3 ;
	private static final double DEFAULT_ASTEROID_MAX_SPEED = 7.5;
	private static final double DEFAULT_ASTEROID_MIN_SPEED = 2.5;
	private static final double ASTEROID_MAX_ANGULAR_SPEED = 3.0;
	private static final double ASTEROID_MIN_ANGULAR_SPEED = 0.5;
	private static final double NEW_SHIP_DELAY = 4.0;
	private static final double NEW_STAGE_DELAY = 5.0;
	private static final double POWER_UP_SPEED = 1.0;
	private static final String DROP_RATE_TIME_PROP = "dropRate";
	private static final String MAX_DROP_RATE_PROP = "maxDropRate";
	private static final String ASTEROID_MAX_SPEED_PROP = "asteroidMaxSpeed";
	private static final String ASTEROID_MIN_SPEED_PROP = "asteroidMinSpeed";
	private static final double DEFAULT_MAX_DROP_RATE = 0.25;
	private static final double DEFAULT_DROP_RATE = 0.1;
	private static final double DEFAULT_DRIPPLE_KILL_TIME = 0.1;
	private static final String TRIPLE_KILL_TIME_PROP = "tipleKillTime";
	private static final int TRIPLE_KILL_SCORE = 100;
	
	private Random random = new Random();
	private boolean engaged;
	private Core core;
	private int score;
	private int cntAsteroids;
	private int numLives;
	private EntityService entitySrv;
	private EventService eventSrv;
	private LoggingService logger;
	private int stage;
	private double lastDrop;
	private Timer timer;
	private PropertyService propService;
	private double killTime[] = new double[3];
	private int cannonPowerUp;
	private int coolerPowerUp;
	
	public GameLogic(Core core) {
		this.core = core;
		this.logger = LoggingService.getInstance(this.core);
		this.entitySrv = EntityService.getInstance(this.core);
		this.eventSrv = EventService.getInstance(this.core);
		this.propService = PropertyService.getInstance(this.core);
		this.timer = TimeService.getInstance(this.core).getTimer();
	}
	
	@Override
	public void engage() {
		this.eventSrv.addListener(this, DestroyedEvent.TYPE_ID);
		this.eventSrv.addListener(this, NEW_SHIP);
		this.eventSrv.addListener(this, NEW_STAGE);
		this.eventSrv.addListener(this, POWER_UP);
		this.eventSrv.addListener(this, EnimyDestroyedEvent.TYPE_ID);
		this.eventSrv.addListener(this, SpaceSweeper.PAUSED);
		this.eventSrv.addListener(this, SpaceSweeper.STARTED);
		this.eventSrv.addListener(this, CannonPowerUpEvent.TYPE_ID);
		this.eventSrv.addListener(this, CoolerPowerUpEvent.TYPE_ID);
		
		reset();
		
		this.stage = 0;
		createShip();
		createAIShip();
		this.eventSrv.dispatchEvent(new StageUpdateEvent(this.stage + 1));
		this.eventSrv.dispatchEvent(new SimpleEvent(NEW_STAGE), NEW_STAGE_DELAY);

		this.lastDrop = this.timer.getTime();
		this.engaged = true;
		
		this.eventSrv.dispatchEvent(new LivesUpdateEvent(this.numLives));
		this.eventSrv.dispatchEvent(new ScoreUpdateEvent(this.score));
	}
	
	@Override
	public void disengage() {
		this.eventSrv.removeListener(this);
		this.entitySrv.removeAllEntities();
		this.engaged = false;
	}

	@Override
	public boolean isEngaged() {
		return this.engaged;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(DestroyedEvent.TYPE_ID)) {
			handleDestroyed((DestroyedEvent) event);
		} else if(event.isOfType(NEW_SHIP)) {
			createShip();
		} else if(event.isOfType(NEW_AI_SHIP)) {
			createAIShip();
		} else if (event.isOfType(NEW_STAGE)) {
			createNewState();
		} else if (event.isOfType(POWER_UP)) {
			createPowerUp(0, 0);
		} else if (event.isOfType(EnimyDestroyedEvent.TYPE_ID)) {
			handleEnimyDestroyed((EnimyDestroyedEvent) event);
		} else if (event.isOfType(SpaceSweeper.PAUSED)) {
			TimeService.getInstance(this.core).getTimer().setPaused(true);
		} else if (event.isOfType(SpaceSweeper.STARTED)) {
			TimeService.getInstance(this.core).getTimer().setPaused(false);			
		} else if (event.isOfType(CannonPowerUpEvent.TYPE_ID)) {
			this.cannonPowerUp = ((CannonPowerUpEvent) event).getLevel();
		} else if (event.isOfType(CoolerPowerUpEvent.TYPE_ID)) {
			this.coolerPowerUp = ((CoolerPowerUpEvent) event).getLevel();
		}
	}
	
	private void handleEnimyDestroyed(EnimyDestroyedEvent event) {
		if (event.getEnimyType().equals(BigAsteroid.TYPE)) {
			incScore(BIG_ASTEROID_SCORE);
		} else if (event.getEnimyType().equals(MediumAsteroid.TYPE)) {
			incScore(MEDIUM_ASTEROID_SCORE);			
		} else if (event.getEnimyType().equals(SmallAsteroid.TYPE)) {
			incScore(SMALL_ASTEROID_SCORE);			
		}

		this.killTime[0] = this.killTime[1];
		this.killTime[1] = this.killTime[2];
		this.killTime[2] = this.timer.getTime();
		if (this.killTime[2] - this.killTime[0] < this.propService.getDoubleProperty(TRIPLE_KILL_TIME_PROP, DEFAULT_DRIPPLE_KILL_TIME)) {
			incScore(TRIPLE_KILL_SCORE);
			this.eventSrv.dispatchEvent(new SimpleEvent(TRIPLE_KILL));	
			this.killTime[0] = this.killTime[1] = this.killTime[2] = 0;
		}
	}

	private void handleDestroyed(DestroyedEvent event) {
		if (event.getEntityTypeId().equals(BigAsteroid.TYPE)) {
			for (int i = 0; i < NUM_DEBRIS; ++i) {
				createMediumAsteroid(event.getPosX(), event.getPosY());
			}
			decAsteroidCounter(1);
			if (checkForPowerup()) {
				createPowerUp(event.getPosX(), event.getPosY());
			}
		} else if (event.getEntityTypeId().equals(MediumAsteroid.TYPE)) {
			for (int i = 0; i < NUM_DEBRIS; ++i) {
				createSmallAsteroid(event.getPosX(), event.getPosY());
			}
			decAsteroidCounter(1);
			if (checkForPowerup()) {
				createPowerUp(event.getPosX(), event.getPosY());
			}
		} else if (event.getEntityTypeId().equals(SmallAsteroid.TYPE)) {
			decAsteroidCounter(1);
			if (checkForPowerup()) {
				createPowerUp(event.getPosX(), event.getPosY());
			}
		} else if (event.getEntityTypeId().equals(ShipEntity.TYPE)) {
			decLive(1);
		}
	}

	private void createShip() {
		if (this.entitySrv.hasEntity(PLAYER_ONE_ID)) {
			return;
		}
		ShipEntity ship = new ShipEntity(this.core, PLAYER_ONE_ID);
		ship.initialize(-5,  0);
		this.entitySrv.addEntity(ship);
	}

	private void createAIShip() {
		if (this.entitySrv.hasEntity(AI_PLAYER_ONE_ID)) {
			return;
		}
		AIShipEntity ship = new AIShipEntity(this.core, AI_PLAYER_ONE_ID);
		ship.initialize(0, 0);
		this.entitySrv.addEntity(ship);
	}
	
	private void createNewState() {
		this.stage++;
		this.logger.logInfo(LOGGING_SOURCE, "creating new stage: " + this.stage);
		for (int i = 0; i < this.stage; ++i) {
			createBigAsteroid();
		}
	}
	
	private void createBigAsteroid() {
		double x;
		double y;
		double ar = SceneService.getInstance(this.core).getAspectRatio();
		PropertyService propSrv = PropertyService.getInstance(this.core);
		double worldWidth = propSrv.getDoubleProperty(PlayState.WORLD_WIDTH_PROP);
		
		if (this.random.nextBoolean()) {
			// create asteroid at top or bottom of playfield
			x = -worldWidth / 2 + worldWidth * this.random.nextDouble();
			if (this.random.nextBoolean()) {
				y = worldWidth / ar + BigAsteroid.RADIUS;				
			} else {
				y = -worldWidth / ar - BigAsteroid.RADIUS;
			}
		} else {
			// create asteroid at left or right side playfield
			y = -worldWidth / ar / 2 - (worldWidth / ar) * this.random.nextDouble();
			if (this.random.nextBoolean()) {
				x = -worldWidth / 2 - BigAsteroid.RADIUS;
			} else {
				x = worldWidth / 2 + BigAsteroid.RADIUS;
			}			
		}
		double phi = Math.PI * 2 * this.random.nextDouble();
		double minSpeed = this.propService.getDoubleProperty(ASTEROID_MIN_SPEED_PROP, DEFAULT_ASTEROID_MIN_SPEED);
		double maxSpeed = this.propService.getDoubleProperty(ASTEROID_MAX_SPEED_PROP, DEFAULT_ASTEROID_MAX_SPEED);
		
		double speed = minSpeed + (maxSpeed - minSpeed) * this.random.nextDouble();
		double angularSpeed = ASTEROID_MIN_ANGULAR_SPEED + (ASTEROID_MIN_ANGULAR_SPEED - ASTEROID_MAX_ANGULAR_SPEED) * this.random.nextDouble();
		if (this.random.nextBoolean()) {
			angularSpeed *= -1;
		}
		
		BigAsteroid asteroid = new BigAsteroid(this.core);
		asteroid.initialize(x, y, phi, speed, angularSpeed);
		this.entitySrv.addEntity(asteroid);
		
		this.cntAsteroids++;
	}
	
	private void createMediumAsteroid(double x, double y) {
		double phi = Math.PI * 2 * this.random.nextDouble();
		double minSpeed = this.propService.getDoubleProperty(ASTEROID_MIN_SPEED_PROP, DEFAULT_ASTEROID_MIN_SPEED);
		double maxSpeed = this.propService.getDoubleProperty(ASTEROID_MAX_SPEED_PROP, DEFAULT_ASTEROID_MAX_SPEED);

		double speed = minSpeed + (maxSpeed - minSpeed) * this.random.nextDouble();
		double angularSpeed = ASTEROID_MIN_ANGULAR_SPEED + (ASTEROID_MIN_ANGULAR_SPEED - ASTEROID_MAX_ANGULAR_SPEED) * this.random.nextDouble();
		if (this.random.nextBoolean()) {
			angularSpeed *= -1;
		}
		
		MediumAsteroid asteroid = new MediumAsteroid(this.core);
		asteroid.initialize(x, y, phi, speed, angularSpeed);
		this.entitySrv.addEntity(asteroid);
		
		this.cntAsteroids++;
	}

	private void createSmallAsteroid(double x, double y) {
		double phi = Math.PI * 2 * this.random.nextDouble();
		double minSpeed = this.propService.getDoubleProperty(ASTEROID_MIN_SPEED_PROP, DEFAULT_ASTEROID_MIN_SPEED);
		double maxSpeed = this.propService.getDoubleProperty(ASTEROID_MAX_SPEED_PROP, DEFAULT_ASTEROID_MAX_SPEED);
		
		double speed = minSpeed + (maxSpeed - minSpeed) * this.random.nextDouble();
		double angularSpeed = ASTEROID_MIN_ANGULAR_SPEED + (ASTEROID_MIN_ANGULAR_SPEED - ASTEROID_MAX_ANGULAR_SPEED) * this.random.nextDouble();
		if (this.random.nextBoolean()) {
			angularSpeed *= -1;
		}
		
		SmallAsteroid asteroid = new SmallAsteroid(this.core);
		asteroid.initialize(x, y, phi, speed, angularSpeed);
		this.entitySrv.addEntity(asteroid);
		
		this.cntAsteroids++;
	}
	
	private void decAsteroidCounter(int n) {
		this.cntAsteroids -= n;
		if (this.cntAsteroids <= 0) {
			this.logger.logInfo(LOGGING_SOURCE, "stage cleared");						
			this.eventSrv.dispatchEvent(new StageUpdateEvent(this.stage + 1));
			this.eventSrv.dispatchEvent(new SimpleEvent(NEW_STAGE), NEW_STAGE_DELAY);
		} else {
			this.logger.logInfo(LOGGING_SOURCE, "asteroids: " + this.cntAsteroids);			
		}		
	}
	
	private boolean checkForPowerup() {
		double dropRate = (this.timer.getTime() - this.lastDrop) / 60 * this.propService.getDoubleProperty(DROP_RATE_TIME_PROP, DEFAULT_DROP_RATE);
		dropRate = Math.min(dropRate, this.propService.getDoubleProperty(MAX_DROP_RATE_PROP, DEFAULT_MAX_DROP_RATE));
		
		this.logger.logDebug(LOGGING_SOURCE, "drop set to " + String.format("%2.2f%%", dropRate * 100));
		
		if (this.random.nextDouble() <= dropRate) {
			this.lastDrop = this.timer.getTime();
			return true;
		}		
		
		return false;
	}
	
	private void decLive(int i) {
		this.numLives--;
		if (numLives <= 0) {
			this.logger.logInfo(LOGGING_SOURCE, "game over");						
			this.eventSrv.dispatchEvent(new SimpleEvent(GAME_OVER));
		} else {
			this.logger.logInfo(LOGGING_SOURCE, "lives: " + this.numLives);						
			this.eventSrv.dispatchEvent(new SimpleEvent(NEW_SHIP), NEW_SHIP_DELAY);
		}

		if (this.numLives < 0) {
			// using the in-game console might add lives independent from live counter
			this.numLives = 0;
		}
		this.eventSrv.dispatchEvent(new LivesUpdateEvent(this.numLives));
	}
	
	private void createPowerUp(double x, double y) {
		
		PowerUp powerUp = null;
		
		while (powerUp == null) {
			switch (this.random.nextInt(4)) {
			case 0:
				if (this.cannonPowerUp == 0) {
					powerUp = new PowerUp(this.core, PowerUp.CANNON_POWERUP1_ID);
				}
				break;
				
			case 1:
				if (this.cannonPowerUp == 1) {
					powerUp = new PowerUp(this.core, PowerUp.CANNON_POWERUP2_ID);
				}
				break;
				
			case 2:
				powerUp = new PowerUp(this.core, PowerUp.SHIELD_POWERUP_ID);
				break;
				
			case 3:
				if (this.coolerPowerUp == 0) {
					powerUp = new PowerUp(this.core, PowerUp.COOLER_POWERUP_ID);
				}
				break;
			}
		}
		
		double phi = Math.PI * 2 * this.random.nextDouble();
		powerUp.initialize(x, y, phi, POWER_UP_SPEED);
		this.entitySrv.addEntity(powerUp);
	}
	
	private void incScore(int n) {
		this.score += n;
		this.eventSrv.dispatchEvent(new ScoreUpdateEvent(this.score));
	}
	
	private void reset() {
		this.score = 0;
		this.cntAsteroids = 0;
		this.numLives = NUM_LIVES;
		this.stage = 0;
	}
}
