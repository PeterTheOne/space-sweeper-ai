package org.cogaen.spacesweeper.view;

import java.util.Random;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.lwjgl.input.KeyCode;
import org.cogaen.lwjgl.input.KeyPressedEvent;
import org.cogaen.lwjgl.input.KeyboardController;
import org.cogaen.lwjgl.scene.Camera;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.FadeInTask;
import org.cogaen.lwjgl.scene.FontHandle;
import org.cogaen.lwjgl.scene.ParticleSystem;
import org.cogaen.lwjgl.scene.ParticleSystemService;
import org.cogaen.lwjgl.scene.PointEmitter;
import org.cogaen.lwjgl.scene.PointVisual;
import org.cogaen.lwjgl.scene.RectangleVisual;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.SpriteHandle;
import org.cogaen.lwjgl.scene.SpriteVisual;
import org.cogaen.lwjgl.scene.TextureHandle;
import org.cogaen.lwjgl.sound.Sound;
import org.cogaen.lwjgl.sound.SoundHandle;
import org.cogaen.lwjgl.sound.SoundService;
import org.cogaen.lwjgl.sound.Source;
import org.cogaen.math.EaseInOut;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.resource.ResourceService;
import org.cogaen.resource.TextHandle;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.spacesweeper.entity.AIShipEntity;
import org.cogaen.spacesweeper.entity.BigAsteroid;
import org.cogaen.spacesweeper.entity.BulletEntity;
import org.cogaen.spacesweeper.entity.MediumAsteroid;
import org.cogaen.spacesweeper.entity.PowerUp;
import org.cogaen.spacesweeper.entity.RocketEntity;
import org.cogaen.spacesweeper.entity.ShipEntity;
import org.cogaen.spacesweeper.entity.SmallAsteroid;
import org.cogaen.spacesweeper.event.DestroyedEvent;
import org.cogaen.spacesweeper.event.SpawnEvent;
import org.cogaen.spacesweeper.event.TargetDeselectedEvent;
import org.cogaen.spacesweeper.event.TargetSelectedEvent;
import org.cogaen.spacesweeper.hud.ConsoleHud;
import org.cogaen.spacesweeper.hud.GameHud;
import org.cogaen.spacesweeper.hud.MessageHud;
import org.cogaen.spacesweeper.hud.MiniMapHud;
import org.cogaen.spacesweeper.physics.PositionUpdateEvent;
import org.cogaen.spacesweeper.representation.BaseRepresentation;
import org.cogaen.spacesweeper.representation.BigAsteroidRepresentation;
import org.cogaen.spacesweeper.representation.BulletRepresentation;
import org.cogaen.spacesweeper.representation.MediumAsteroidRepresentation;
import org.cogaen.spacesweeper.representation.PowerUpRepresentation;
import org.cogaen.spacesweeper.representation.RocketRepresentation;
import org.cogaen.spacesweeper.representation.ShipRepresentation;
import org.cogaen.spacesweeper.representation.SmallAsteroidRepresentation;
import org.cogaen.spacesweeper.state.GameLogic;
import org.cogaen.spacesweeper.state.PlayState;
import org.cogaen.spacesweeper.task.CameraShakerTask;
import org.cogaen.task.AbstractTask;
import org.cogaen.task.TaskService;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;
import org.cogaen.view.View;

public class PlayView extends View implements EventListener {

	public static final CogaenId JET_STREAM_POOL = new CogaenId("JetStream");
	public static final CogaenId ROCKET_JET_STREAM_POOL = new CogaenId("RocketJetStream");
	public static final CogaenId SMALl_FIRE_POOL = new CogaenId("SmallFire");
	public static final CogaenId SMOKE_POOL = new CogaenId("Smoke");
	public static final CogaenId SMALL_EXPLOSION_POOL = new CogaenId("SmallExplosion");
	public static final CogaenId BULLET_PARTICLE_POOL = new CogaenId("BulletParticle");
	
	public static final CogaenId SOUND_BIG_EXPLOSION_POOL = new CogaenId("BixExplosion");
	public static final CogaenId SOUND_MEDIUM_EXPLOSION_POOL = new CogaenId("MediumExplosion");
	public static final CogaenId SOUND_SMALL_EXPLOSION_POOL = new CogaenId("SmallExplosion");
	public static final CogaenId SOUND_BULLET_EXPLOSION_POOL = new CogaenId("BulletExplosion");
	public static final CogaenId SOUND_SHIP_DESTROYED_POOL = new CogaenId("ShipDestroyed");
	public static final CogaenId ROCKET_LAUNCH_POOL = new CogaenId("RocketLaunch");
	public static final CogaenId SOUND_SHOT_POOL = new CogaenId("Shot");
	
	private static final String EXPLOSION_SOUND_VARIANCE_PROP = "explosionSoundVariance";
	private static final double DEFAULT_EXPLOSION_SOUND_VARIANCE = 0.3;
	private static final String SHOT_SOUND_VARIANCE_PROP = "shotSoundVariance";
	private static final double DEFAULT_SHOT_SOUND_VARIANCE = 0.05;
	public static final String VIEW_ZOOM_PROP = "viewZoom";
	private static final double DEFAULT_VIEW_ZOOM = 1.0;
	private static final String CAMERA_SPEED_PROP = "cameraSpeed";
	private static final double DEFAULT_CAMERA_SPEED = 0.2;
	private static final double FADE_TIME = 1.0;
	
	private SceneService scnSrv;
	private KeyboardController ctrl;
	private Camera camera;
	private GameHud gameHud;
	private MessageHud messageHud;
	private MiniMapHud miniMap;
	private ConsoleHud console;
	private PlayViewLogic viewLogic;
	private CameraMover camMover;
	private Source backgroundMusic;
	private boolean busy;
	
	public PlayView(Core core) {
		super(core);
		this.console = new ConsoleHud(core);
		this.gameHud = new GameHud(core);
		this.miniMap = new MiniMapHud(core);
		this.messageHud = new MessageHud(core, true);
		this.viewLogic = new PlayViewLogic(core);
		this.ctrl = new KeyboardController(getCore(), new CogaenId("Player1"));
		this.ctrl.addButton(KeyCode.KEY_LCONTROL);
		this.ctrl.addButton(KeyCode.KEY_LSHIFT);
		this.scnSrv = SceneService.getInstance(getCore());
	}
	
	@Override
	public void registerResources(CogaenId groupId) {
		PropertyService propSrv = PropertyService.getInstance(getCore());
		double worldWidth = propSrv.getDoubleProperty(PlayState.WORLD_WIDTH_PROP, PlayState.DEFAULT_WORLD_WIDTH);

		ResourceService resSrv = ResourceService.getInstance(getCore());

		this.gameHud.registerResources(groupId);
		this.messageHud.registerResources(groupId);
		
		// textures
		resSrv.declareResource("ShipTex", groupId, new TextureHandle("PNG", "images/ship_standard_64x64.png"));
		resSrv.declareResource("JetTex", groupId, new TextureHandle("PNG", "images/smoke.png", TextureHandle.NEAREST_FILTER));
		resSrv.declareResource("BigAsteroidTex", groupId, new TextureHandle("PNG", "images/asteroid1_128x128.png"));
		resSrv.declareResource("MediumAsteroidTex", groupId, new TextureHandle("PNG", "images/asteroid3_64x64.png"));
		resSrv.declareResource("SmallAsteroidTex", groupId, new TextureHandle("PNG", "images/asteroid3_32x32.png"));
		resSrv.declareResource("CannonPowerUp1Tex", groupId, new TextureHandle("PNG", "images/cannon_powerup1_64x64.png"));
		resSrv.declareResource("CannonPowerUp1SmlTex", groupId, new TextureHandle("PNG", "images/cannon_powerup1_32x32.png"));
		resSrv.declareResource("CannonPowerUp2Tex", groupId, new TextureHandle("PNG", "images/cannon_powerup2_64x64.png"));
		resSrv.declareResource("CannonPowerUp2SmlTex", groupId, new TextureHandle("PNG", "images/cannon_powerup2_32x32.png"));
		resSrv.declareResource("CoolerPowerUpTex", groupId, new TextureHandle("PNG", "images/cooler_powerup_64x64.png"));
		resSrv.declareResource("CoolerPowerUpSmlTex", groupId, new TextureHandle("PNG", "images/cooler_powerup_32x32.png"));
		resSrv.declareResource("ShieldPowerUpTex", groupId, new TextureHandle("PNG", "images/shield_powerup_64x64.png"));
		resSrv.declareResource("ShieldPowerUpSmlTex", groupId, new TextureHandle("PNG", "images/shield_powerup_32x32.png"));
		resSrv.declareResource("BackgroundTex", groupId, new TextureHandle("JPG", "images/space_1024x768.jpg", TextureHandle.NEAREST_FILTER));
		
		// sprites
		double ar = SceneService.getInstance(getCore()).getAspectRatio();
		resSrv.declareResource("ShipSpr", groupId, new SpriteHandle("ShipTex", ShipEntity.RADIUS * 2, ShipEntity.RADIUS * 2));
		resSrv.declareResource("JetSpr", groupId, new SpriteHandle("JetTex", 0.5, 0.5));
		resSrv.declareResource("BigAsteroidSpr", groupId, new SpriteHandle("BigAsteroidTex", BigAsteroid.RADIUS * 2, BigAsteroid.RADIUS * 2));
		resSrv.declareResource("MediumAsteroidSpr", groupId, new SpriteHandle("MediumAsteroidTex", MediumAsteroid.RADIUS * 2, MediumAsteroid.RADIUS * 2));
		resSrv.declareResource("SmallAsteroidSpr", groupId, new SpriteHandle("SmallAsteroidTex", SmallAsteroid.RADIUS * 2, SmallAsteroid.RADIUS * 2));
		resSrv.declareResource("CannonPowerUp1Spr", groupId, new SpriteHandle("CannonPowerUp1Tex", PowerUp.RADIUS * 2, PowerUp.RADIUS * 2));
		resSrv.declareResource("CannonPowerUp1MiniMapSpr", groupId, new SpriteHandle("CannonPowerUp1SmlTex", PowerUp.RADIUS * 4, PowerUp.RADIUS * 4));
		resSrv.declareResource("CannonPowerUp2Spr", groupId, new SpriteHandle("CannonPowerUp2Tex", PowerUp.RADIUS * 2, PowerUp.RADIUS * 2));
		resSrv.declareResource("CannonPowerUp2MiniMapSpr", groupId, new SpriteHandle("CannonPowerUp2SmlTex", PowerUp.RADIUS * 4, PowerUp.RADIUS * 4));
		resSrv.declareResource("ShieldPowerUpSpr", groupId, new SpriteHandle("ShieldPowerUpTex", PowerUp.RADIUS * 2, PowerUp.RADIUS * 2));
		resSrv.declareResource("ShieldPowerUpMiniMapSpr", groupId, new SpriteHandle("ShieldPowerUpSmlTex", PowerUp.RADIUS * 4, PowerUp.RADIUS * 4));
		resSrv.declareResource("CoolerPowerUpSpr", groupId, new SpriteHandle("CoolerPowerUpTex", PowerUp.RADIUS * 2, PowerUp.RADIUS * 2));
		resSrv.declareResource("CoolerPowerUpMiniMapSpr", groupId, new SpriteHandle("CoolerPowerUpSmlTex", PowerUp.RADIUS * 4, PowerUp.RADIUS * 4));
		resSrv.declareResource("BackSpr", groupId, new SpriteHandle("BackgroundTex", worldWidth, worldWidth / ar));
		resSrv.declareResource("StdFont", groupId, new FontHandle("D3 Euronism", FontHandle.PLAIN, 25));		
		resSrv.declareResource("CmlFont", groupId, new FontHandle("SansSerif", FontHandle.PLAIN, 15));		
		
		// sounds
		resSrv.declareResource("ShotSnd", groupId, new SoundHandle("sound/laser1.wav"));
		resSrv.declareResource("BigExplosionSnd", groupId, new SoundHandle("sound/explosion1.wav"));
		resSrv.declareResource("MediumExplosionSnd", groupId, new SoundHandle("sound/explosion2.wav"));
		resSrv.declareResource("SmallExplosionSnd", groupId, new SoundHandle("sound/explosion3.wav"));
		resSrv.declareResource("BulletExplosionSnd", groupId, new SoundHandle("sound/bulletexplosion5.wav"));
		resSrv.declareResource("ShipDestroyedSnd", groupId, new SoundHandle("sound/destroyed.wav"));
		resSrv.declareResource("PowerUpEngageSnd", groupId, new SoundHandle("sound/powerup2.wav"));
		resSrv.declareResource("CannonPowerUpSnd", groupId, new SoundHandle("sound/powerup.wav"));
		resSrv.declareResource("ShieldPowerUpSnd", groupId, new SoundHandle("sound/powerup.wav"));
		resSrv.declareResource("CoolerPowerUpSnd", groupId, new SoundHandle("sound/powerup.wav"));
		resSrv.declareResource("OverheatSnd", groupId, new SoundHandle("sound/overheat.wav"));
		resSrv.declareResource("ThrustSnd", groupId, new SoundHandle("sound/thrust4.wav"));
		resSrv.declareResource("RocketLaunchSnd1", groupId, new SoundHandle("sound/rocket1.wav"));
		resSrv.declareResource("RocketLaunchSnd2", groupId, new SoundHandle("sound/rocket2.wav"));
		resSrv.declareResource("RocketLaunchSnd3", groupId, new SoundHandle("sound/rocket3.wav"));
		resSrv.declareResource("RocketExplosionSnd", groupId, new SoundHandle("sound/explosion4.wav"));
		resSrv.declareResource("AlarmSnd", groupId, new SoundHandle("sound/alarm1.wav"));
		resSrv.declareResource("BackgroundSnd", groupId, new SoundHandle("music/001-Stephen_-1208_2.wav"));
		
		// text
		resSrv.declareResource("GuideTxt", groupId, new TextHandle("text/guide.txt"));		
		resSrv.declareResource("RandomTxt", groupId, new TextHandle("text/random.txt"));		
	}

	public void engage() {
		super.engage();
		
		// check view width integrity
		PropertyService propSrv = PropertyService.getInstance(getCore());

		double zoom = propSrv.getDoubleProperty(VIEW_ZOOM_PROP, DEFAULT_VIEW_ZOOM);
		if (zoom < 1.0) {
			propSrv.setDoubleProperty(VIEW_ZOOM_PROP, DEFAULT_VIEW_ZOOM);
		}
		
		EventService evtSrv = EventService.getInstance(getCore());
		evtSrv.addListener(this, KeyPressedEvent.TYPE_ID);
		evtSrv.addListener(this, SpawnEvent.TYPE_ID);
		evtSrv.addListener(this, DestroyedEvent.TYPE_ID);
		evtSrv.addListener(this, PositionUpdateEvent.TYPE_ID);
		evtSrv.addListener(this, SpaceSweeper.PAUSED);
		evtSrv.addListener(this, SpaceSweeper.STARTED);
		evtSrv.addListener(this, TargetSelectedEvent.TYPE_ID);
		evtSrv.addListener(this, TargetDeselectedEvent.TYPE_ID);
				
		SceneService scnSrv = SceneService.getInstance(getCore());
		scnSrv.ensuerNumOfLayers(3);
		
		initParticleSystems();
		initSoundPools();

		this.ctrl.engage();
		setupCamera();
		setupBackgroundSprite();
				
		this.gameHud.engage();
		this.miniMap.engage();
		this.messageHud.engage();
		this.viewLogic.engage();
		if (propSrv.getDoubleProperty(VIEW_ZOOM_PROP) == 1) {
			this.miniMap.setActive(false);
		}
		this.console.engage();
				
		// init background music
		this.backgroundMusic = SoundService.getInstance(getCore()).createSource();
		this.backgroundMusic.assignSound((Sound) ResourceService.getInstance(getCore()).getResource("BackgroundSnd"));
		this.backgroundMusic.setLooping(true);
		this.backgroundMusic.playSound();	
		
		this.busy = false;
	}

	public void disengage() {
		TimeService.getInstance(getCore()).getTimer().setPaused(false);
		TaskService.getInstance(getCore()).destroyTask(this.camMover);
		this.viewLogic.disengage();
		this.messageHud.disengage();
		this.console.disengage();
		this.gameHud.disengage();
		this.miniMap.disengage();
		this.ctrl.disengage();
		
		removeAllRepresentations();
		EventService.getInstance(getCore()).removeListener(this);
		SceneService.getInstance(getCore()).destroyAll();
		ParticleSystemService.getInstance(getCore()).destroyAll();
		SoundService.getInstance(getCore()).destroyAll();
		super.disengage();
	}
	
	private void initParticleSystems() {
		ParticleSystemService psSrv = ParticleSystemService.getInstance(getCore());
		
		// particle pool 'jet stream'
		psSrv.createPool(JET_STREAM_POOL);
		ParticleSystem ps = psSrv.createParticleSystem();
		ps.setParticlesPerSecond(125);		
		PointEmitter pe = new PointEmitter();
		pe.setRadius(Math.PI / 6);
		pe.setMinTimeToLive(0.25);
		pe.setMaxTimeToLive(0.5);
		pe.setAcceleration(0, 12, 0);
		ps.setEmitter(pe);		
		SpriteVisual pVisual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("JetSpr");
		pVisual.setMask(0x0001);
		ps.setVisual(pVisual);
		ps.setStartSize(0.5);
		ps.setEndSize(4.0);
		ps.setStartColor(new Color(1, 1, 1, 1));
		ps.setEndColor(new Color(1, 1, 1, 0));
		psSrv.addToPool(JET_STREAM_POOL, ps, 4);

		// particle pool 'rocket jet stream'
		psSrv.createPool(ROCKET_JET_STREAM_POOL);
		ps = psSrv.createParticleSystem();
		ps.setParticlesPerSecond(500);		
		pe = new PointEmitter();
		pe.setRadius(Math.PI / 6);
		pe.setMinTimeToLive(0.2);
		pe.setMaxTimeToLive(0.7);
		pe.setAcceleration(0, 0, 0);
		pe.setMaxLinearSpeed(2);
		pe.setMinLinearSpeed(1);
		pe.setDrag(1.0);
		ps.setEmitter(pe);		
		pVisual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("JetSpr");
		pVisual.setMask(0x0001);
		ps.setVisual(pVisual);
		ps.setStartSize(0.1);
		ps.setEndSize(1.5);
		ps.setStartColor(new Color(1, 1, 1, 1));
		ps.setEndColor(new Color(0.5, 0.5, 0.5, 0));
		psSrv.addToPool(ROCKET_JET_STREAM_POOL, ps, 3);
		
		// particle pool 'Smoke'
		psSrv.createPool(SMOKE_POOL);
		ps = psSrv.createParticleSystem();
		ps.setParticlesPerSecond(750);
		pe = new PointEmitter();
		pe.setMinLinearSpeed(0.5);
		pe.setMaxLinearSpeed(3);
		pe.setMinTimeToLive(0.3);
		pe.setMaxTimeToLive(0.4);		
		pe.setDrag(1);
		ps.setEmitter(pe);
		
		pVisual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("JetSpr");
		pVisual = pVisual.newInstance();
		pVisual.setAdditive(false);
		pVisual.setMask(0x0001);
		ps.setVisual(pVisual);
		ps.setStartColor(new Color(1, 1, 1, 1));
		ps.setEndColor(new Color(0.1, 0.1, 0.1, 0));
		ps.setStartSize(0.1);
		ps.setEndSize(2.5);
		psSrv.addToPool(SMOKE_POOL, ps, 5);
		
		// particle pool 'Small Fire'
		psSrv.createPool(SMALl_FIRE_POOL);
		ps = psSrv.createParticleSystem();
		ps.setParticlesPerSecond(500);
		pe = new PointEmitter();
		pe.setMinLinearSpeed(0.5);
		pe.setMaxLinearSpeed(3);
		pe.setMinTimeToLive(0.05);
		pe.setMaxTimeToLive(0.2);		
		pe.setDrag(1);
		ps.setEmitter(pe);
		
		pVisual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("JetSpr");
		pVisual = pVisual.newInstance();
		pVisual.setAdditive(true);
		pVisual.setMask(0x0001);
		ps.setVisual(pVisual);
		ps.setStartColor(new Color(1, 0.9, 0.0, 1));
		ps.setEndColor(new Color(0.8, 0.1, 0.1, 0));
		ps.setStartSize(0.1);
		ps.setEndSize(2.5);
		psSrv.addToPool(SMALl_FIRE_POOL, ps, 5);
		
		// particle pool 'Small Explosion'
		psSrv.createPool(SMALL_EXPLOSION_POOL);
		ps = psSrv.createParticleSystem();
		ps.setParticlesPerSecond(500);
		pe = new PointEmitter();
		pe.setMinLinearSpeed(0.5);
		pe.setMaxLinearSpeed(4);
		pe.setMinTimeToLive(0.1);
		pe.setMaxTimeToLive(0.4);
		ps.setEmitter(pe);
		
		PointVisual pv = new PointVisual();
		pv.setColor(Color.ORANGE);
		pv.setMask(0x0001);
		
		ps.setVisual(pv);;
		ps.setStartColor(Color.GREEN);
		ps.setEndColor(new Color(1, 1, 0, 0));
		psSrv.addToPool(SMALL_EXPLOSION_POOL, ps, 5);
		
		// particle pool 'bullet particles'
		psSrv.createPool(BULLET_PARTICLE_POOL);
		ps = psSrv.createParticleSystem();
		ps.setParticlesPerSecond(350);
		pe = new PointEmitter();
		pe.setMinLinearSpeed(0.1);
		pe.setMaxLinearSpeed(2.0);
		pe.setMinTimeToLive(0.5);
		pe.setMaxTimeToLive(0.7);
		pe.setRadius(0.7);
		ps.setEmitter(pe);

		pv = new PointVisual();
		pv.setColor(new Color(0.9, 0.5, 0.3));
		pv.setMask(0x0001);
		ps.setVisual(pv);
		ps.setStartColor(Color.YELLOW);
		ps.setEndColor(new Color(1, 0, 0, 0));
		psSrv.addToPool(BULLET_PARTICLE_POOL, ps, 10);
	}
	
	public void initSoundPools() {
		PropertyService propSrv = PropertyService.getInstance(getCore());
		SoundService sndSrv = SoundService.getInstance(getCore());
		ResourceService resSrv = ResourceService.getInstance(getCore());
		
		Random rnd = new Random();
		double variance = propSrv.getDoubleProperty(EXPLOSION_SOUND_VARIANCE_PROP, DEFAULT_EXPLOSION_SOUND_VARIANCE);
		
		// big explosion
		sndSrv.createPool(SOUND_BIG_EXPLOSION_POOL);
		for (int i = 0; i < 6; ++i) {
			Source src = sndSrv.createSource();
			src.assignSound((Sound) resSrv.getResource("BigExplosionSnd"));
			double pitch = 1.0 - variance / 2 + rnd.nextDouble() * variance;
			src.setPitch(pitch);
			sndSrv.addToPool(SOUND_BIG_EXPLOSION_POOL, src);
		}
		
		// medium explosion
		sndSrv.createPool(SOUND_MEDIUM_EXPLOSION_POOL);
		for (int i = 0; i < 10; ++i) {
			Source src = sndSrv.createSource();
			src.assignSound((Sound) resSrv.getResource("MediumExplosionSnd"));
			double pitch = 1.0 - variance / 2 + rnd.nextDouble() * variance;
			src.setPitch(pitch);
			sndSrv.addToPool(SOUND_MEDIUM_EXPLOSION_POOL, src);
		}
		
		// small explosion
		sndSrv.createPool(SOUND_SMALL_EXPLOSION_POOL);
		for (int i = 0; i < 15; ++i) {
			Source src = sndSrv.createSource();
			src.assignSound((Sound) resSrv.getResource("SmallExplosionSnd"));
			double pitch = 1.0 - variance / 2 + rnd.nextDouble() * variance;
			src.setPitch(pitch);
			sndSrv.addToPool(SOUND_SMALL_EXPLOSION_POOL, src);
		}
		
		// shots
		variance = propSrv.getDoubleProperty(SHOT_SOUND_VARIANCE_PROP, DEFAULT_SHOT_SOUND_VARIANCE);
		sndSrv.createPool(SOUND_SHOT_POOL);
		for (int i = 0; i < 20; ++i) {
			Source src = sndSrv.createSource();
			src.assignSound((Sound) resSrv.getResource("ShotSnd"));
			double pitch = 1.0 - variance / 2 + rnd.nextDouble() * variance;
			src.setPitch(pitch);
			sndSrv.addToPool(SOUND_SHOT_POOL, src);
		}
		
		// bullet explosion
		sndSrv.createPool(SOUND_BULLET_EXPLOSION_POOL);
		for (int i = 0; i < 20; ++i) {
			Source src = sndSrv.createSource();
			src.assignSound((Sound) resSrv.getResource("BulletExplosionSnd"));
			sndSrv.addToPool(SOUND_BULLET_EXPLOSION_POOL, src);
		}
		
		// ship destroyed
		sndSrv.createPool(SOUND_SHIP_DESTROYED_POOL);
		for (int i = 0; i < 1; ++i) {
			Source src = sndSrv.createSource();
			src.assignSound((Sound) resSrv.getResource("ShipDestroyedSnd"));
			sndSrv.addToPool(SOUND_SHIP_DESTROYED_POOL, src);
		}
		
		// rocket launch sound pool
		sndSrv.createPool(ROCKET_LAUNCH_POOL);
		for (int i = 0; i < 3; ++i) {
			Source src = sndSrv.createSource();
			src.assignSound((Sound) resSrv.getResource("RocketLaunchSnd" + (i + 1)));
			sndSrv.addToPool(ROCKET_LAUNCH_POOL, src);
		}
		
	}
	
	private void setupCamera() {
		camera = scnSrv.createCamera();
		camera.setPosition(0, 0);
		
		PropertyService propSrv = PropertyService.getInstance(getCore());
		
//		double height = scnSrv.getScreenHeight() -  scnSrv.getScreenHeight() * 0.045;
//		double width = height * scnSrv.getAspectRatio();
//		if (propSrv.getBoolProperty("keepAspectRatio", false)) {
//			camera.setViewPort((int) ((scnSrv.getScreenWidth() - width) / 2), 0, (int) width, (int) height);			
//		} else {
//			camera.setViewPort(0, 0, scnSrv.getScreenWidth(), (int) height);						
//		}
		
		double p = 0.98;
		int w = (int) (scnSrv.getScreenWidth() * p);
		int h = (int) (scnSrv.getScreenHeight() * p);
		int x = (scnSrv.getScreenWidth() - w) / 2;
		//int y = (scnSrv.getScreenHeight() - h) / 2;
		camera.setViewPort(x, 0, w, h);
		
		camera.setZoom(scnSrv.getScreenWidth() / (propSrv.getDoubleProperty(PlayState.WORLD_WIDTH_PROP) / propSrv.getDoubleProperty(VIEW_ZOOM_PROP)));
		camera.setMask(0x0001);
		
		this.camMover = new CameraMover(getCore(), camera, propSrv.getDoubleProperty(CAMERA_SPEED_PROP, DEFAULT_CAMERA_SPEED));
		TaskService.getInstance(getCore()).attachTask(camMover);
	}
	
	private void setupBackgroundSprite() {
		SceneNode node = this.scnSrv.createNode();
		SpriteVisual visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("BackSpr");
		double width = PropertyService.getInstance(getCore()).getDoubleProperty(PlayState.WORLD_WIDTH_PROP);
		double height = width / this.scnSrv.getAspectRatio();
		visual.setSize(width, height);
		visual.setMask(0x0001);
		node.addVisual(visual);
		this.scnSrv.getLayer(2).addNode(node);
	}
		
	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(KeyPressedEvent.TYPE_ID)) {
			handleKeyPressed((KeyPressedEvent) event);
		} else if (event.isOfType(SpawnEvent.TYPE_ID)) {
			handleSpawn((SpawnEvent) event);
		} else if (event.isOfType(DestroyedEvent.TYPE_ID)) {
			handleDestroyed((DestroyedEvent) event);
		} else if (event.isOfType(PositionUpdateEvent.TYPE_ID)) {
			handlePositionUpdate((PositionUpdateEvent) event);
		} else if (event.isOfType(SpaceSweeper.PAUSED)) {
			this.backgroundMusic.stopSound();
		} else if (event.isOfType(SpaceSweeper.STARTED)) {
			this.backgroundMusic.playSound();
		} else if (event.isOfType(TargetSelectedEvent.TYPE_ID)) {
			handleTargetSelected((TargetSelectedEvent) event);
		} else if (event.isOfType(TargetDeselectedEvent.TYPE_ID)) {
			handleTargetDeselected((TargetDeselectedEvent) event);
		}
	}

	private void handleTargetDeselected(TargetDeselectedEvent event) {
		BaseRepresentation er = (BaseRepresentation) getRepresentation(event.getTargetId());
		if (er != null) {
			er.setMark(false);
		}
	}

	private void handleTargetSelected(TargetSelectedEvent event) {
		BaseRepresentation er = (BaseRepresentation) getRepresentation(event.getTargetId());
		if (er != null) {
			er.setMark(true);
		}
	}

	private void handlePositionUpdate(PositionUpdateEvent positionUpdate) {
		BaseRepresentation er = (BaseRepresentation) getRepresentation(positionUpdate.getEntityId());
		er.updatePosition(positionUpdate.getPosX(), positionUpdate.getPosY(), positionUpdate.getAngle());
		
		if (positionUpdate.getEntityId().equals(GameLogic.PLAYER_ONE_ID)) {
			this.camMover.setPosition(positionUpdate.getPosX(), positionUpdate.getPosY());
		}
	}
	
	private void handleDestroyed(DestroyedEvent destroyed) {
		BaseRepresentation er = (BaseRepresentation) getRepresentation(destroyed.getEntityId());
		er.destroy();
		removeRepresentation(destroyed.getEntityId());
		
		if (destroyed.getEntityTypeId().equals(ShipEntity.TYPE)) {
			shake();
		}
	}
	
	private void shake() {
		CameraShakerTask shaker = new CameraShakerTask(getCore(), this.camera);
		TaskService.getInstance(getCore()).attachTask(shaker);		
		this.camMover.reset(this.camera.getPosX(), this.camera.getPosY());
	}

	private void handleSpawn(SpawnEvent spawn) {
		if (spawn.isEntityType(ShipEntity.TYPE)) {
			ShipRepresentation er = new ShipRepresentation(getCore(), spawn.getEntityId());
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(AIShipEntity.TYPE)) {
			ShipRepresentation er = new ShipRepresentation(getCore(), spawn.getEntityId());
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(BigAsteroid.TYPE)) {
			BigAsteroidRepresentation er = new BigAsteroidRepresentation(getCore(), spawn.getEntityId());
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(BulletEntity.TYPE)) {
			BulletRepresentation er = new BulletRepresentation(getCore(), spawn.getEntityId());
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(MediumAsteroid.TYPE)) {
			MediumAsteroidRepresentation er = new MediumAsteroidRepresentation(getCore(), spawn.getEntityId());
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(SmallAsteroid.TYPE)) {
			SmallAsteroidRepresentation er = new SmallAsteroidRepresentation(getCore(), spawn.getEntityId());
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(PowerUp.CANNON_POWERUP1_ID)) {
			PowerUpRepresentation er = new PowerUpRepresentation(getCore(), spawn.getEntityId(), PowerUpRepresentation.Type.CANNON1);
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(PowerUp.CANNON_POWERUP2_ID)) {
			PowerUpRepresentation er = new PowerUpRepresentation(getCore(), spawn.getEntityId(), PowerUpRepresentation.Type.CANNON2);
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(PowerUp.SHIELD_POWERUP_ID)) {
			PowerUpRepresentation er = new PowerUpRepresentation(getCore(), spawn.getEntityId(), PowerUpRepresentation.Type.SHIELD);
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(PowerUp.COOLER_POWERUP_ID)) {
			PowerUpRepresentation er = new PowerUpRepresentation(getCore(), spawn.getEntityId(), PowerUpRepresentation.Type.COOLER);
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		} else if (spawn.isEntityType(RocketEntity.TYPE)) {
			RocketRepresentation er = new RocketRepresentation(getCore(), spawn.getEntityId());
			addRepresentation(spawn.getEntityId(), er);
			er.setPose(spawn.getPosX(), spawn.getPosY(), spawn.getAngle());
		}
	}

	private void handleKeyPressed(KeyPressedEvent event) {
		if (this.busy) {
			return;
		}
		
		switch (event.getKeyCode()) {
		case KeyCode.KEY_ESC:
			fadeOut();
			break;
			
		case KeyCode.KEY_M:
			if (!this.console.isActive()) {
				this.miniMap.setActive(!this.miniMap.isActive());
			}
			break;
			
		case KeyCode.KEY_P:
			if (!this.console.isActive()) {
				Timer timer = TimeService.getInstance(getCore()).getTimer();
				timer.setPaused(!timer.isPaused());
			}
			break;
			
		case KeyCode.KEY_F1:
			if (!this.console.isActive()) {
				TimeService.getInstance(getCore()).getTimer().setPaused(true);
				this.console.setActive(true, true);
			} else {
				TimeService.getInstance(getCore()).getTimer().setPaused(false);
				this.console.setActive(false, true);
			}
			break;
		}
	}
	
	private void fadeOut() {
		SceneNode node = this.scnSrv.createNode();
		node.setPose(0.5, 0.5 / scnSrv.getAspectRatio(), 0);
		RectangleVisual cover = new RectangleVisual(1.0, 1.0 / scnSrv.getAspectRatio());
		cover.setColor(Color.BLACK);
		node.addVisual(cover);
		scnSrv.getOverlayRoot().addNode(node);
		
		FadeInTask fadeIn = new FadeInTask(getCore(), cover, FADE_TIME);
		TaskService.getInstance(getCore());
		fadeIn.setFinishedEventId(PlayState.END_OF_PLAY);
		TaskService.getInstance(getCore()).attachTask(fadeIn);
		this.busy = true;
	}
		
	private static class CameraMover extends AbstractTask {

		private EaseInOut xPos = new EaseInOut(0, DEFAULT_CAMERA_SPEED);
		private EaseInOut yPos = new EaseInOut(0, DEFAULT_CAMERA_SPEED);
		private Camera camera;
		private Timer timer;
		private double horizontalMargin;
		private double verticalMargin;
		
		public CameraMover(Core core, Camera camera, double speed) {
			super(core, "camera mover");
			this.camera = camera;
			this.timer = TimeService.getInstance(getCore()).getTimer();

			PropertyService propSrv = PropertyService.getInstance(getCore());
			double worldWidth = propSrv.getDoubleProperty(PlayState.WORLD_WIDTH_PROP);
			double viewWidth = worldWidth / propSrv.getDoubleProperty(VIEW_ZOOM_PROP);			
			this.horizontalMargin = (worldWidth - viewWidth) / 2;
			
			double ar = SceneService.getInstance(getCore()).getAspectRatio();
			double worldHeight = worldWidth / ar;
			double viewHeight = viewWidth / ar;
			this.verticalMargin = (worldHeight - viewHeight) / 2;
			
			this.xPos.setEaseTime(speed);
			this.yPos.setEaseTime(speed);
		} 

		public void reset(double posX, double posY) {
			this.xPos.reset(posX);
			this.yPos.reset(posY);
		}

		@Override
		public void update() {
			this.xPos.update(this.timer.getDeltaTime());
			this.yPos.update(this.timer.getDeltaTime());
			
			this.camera.setPosition(xPos.getCurrentValue(), yPos.getCurrentValue());
		}

		@Override
		public void destroy() {
			// intentionally left empty
		}
		
		public void setPosition(double x, double y) {
			if (x > this.horizontalMargin) {
				this.xPos.setTargetValue(this.horizontalMargin);							
			} else if (x < -this.horizontalMargin) {
				this.xPos.setTargetValue(-this.horizontalMargin);
			} else {
				this.xPos.setTargetValue(x);				
			}
			
			if (y > this.verticalMargin) {
				this.yPos.setTargetValue(this.verticalMargin);
			} else if (y < -this.verticalMargin) {
				this.yPos.setTargetValue(-this.verticalMargin);
			} else {				
				this.yPos.setTargetValue(y);
			}
		}
	}
}
