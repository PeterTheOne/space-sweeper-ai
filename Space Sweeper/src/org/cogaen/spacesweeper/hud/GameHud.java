package org.cogaen.spacesweeper.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.lwjgl.scene.Alignment;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.FontHandle;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.SpriteHandle;
import org.cogaen.lwjgl.scene.SpriteVisual;
import org.cogaen.lwjgl.scene.TextVisual;
import org.cogaen.lwjgl.scene.TextureHandle;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.spacesweeper.entity.ShipEntity;
import org.cogaen.spacesweeper.event.CannonPowerUpEvent;
import org.cogaen.spacesweeper.event.CoolerPowerUpEvent;
import org.cogaen.spacesweeper.event.DestroyedEvent;
import org.cogaen.spacesweeper.event.EnimyDestroyedEvent;
import org.cogaen.spacesweeper.event.HeatUpdateEvent;
import org.cogaen.spacesweeper.event.LivesUpdateEvent;
import org.cogaen.spacesweeper.event.MessageEvent;
import org.cogaen.spacesweeper.event.RocketReloadUpdateEvent;
import org.cogaen.spacesweeper.event.ScoreUpdateEvent;
import org.cogaen.spacesweeper.event.ShieldUpdateEvent;
import org.cogaen.spacesweeper.event.StageUpdateEvent;
import org.cogaen.spacesweeper.task.BarUpdateTask;
import org.cogaen.task.TaskService;
import org.cogaen.view.AbstractHud;

public class GameHud extends AbstractHud implements EventListener {

	private static final int CNT_DESTROYED_MESSAGE = 100;
	private SceneNode baseNode;
	private TextVisual score;
	private BarHud shields;
	private BarUpdateTask shieldUpdater;
	private BarHud heat;
	private BarUpdateTask heatUpdater;
	private BarHud rocket;
	private TextVisual rocketText;
	private BarUpdateTask rocketUpdater;
	private Visual lives[] = new Visual[5];
	private Visual powerUps[] = new Visual[3];
	private int hitCount;
	private List<String> destroyedMessages = new ArrayList<String>();
	private List<String> usedDestroyedMessages = new ArrayList<String>();
	private Random rnd = new Random();
	
	public GameHud(Core core) {
		super(core);
		this.shields = new BarHud(core, 0.11, 0.016);
		this.heat = new BarHud(core, 0.11, 0.016);
		this.rocket = new BarHud(core, 0.11, 0.016);
	}

	@Override
	public void registerResources(CogaenId groupId) {
		super.registerResources(groupId);
		
		ResourceService resSrv = ResourceService.getInstance(getCore());
		double ar = SceneService.getInstance(getCore()).getAspectRatio();
		resSrv.declareResource("HudTopTex", groupId, new TextureHandle("PNG", "images/hud_top.png", TextureHandle.NEAREST_FILTER));
		resSrv.declareResource("HudTopSpr", groupId, new SpriteHandle("HudTopTex", 1.0, 0.091 / ar));
		resSrv.declareResource("HudBottomTex", groupId, new TextureHandle("PNG", "images/hud_bottom.png", TextureHandle.NEAREST_FILTER));
		resSrv.declareResource("HudBottomSpr", groupId, new SpriteHandle("HudBottomTex", 1.0, 0.029 / ar));
		resSrv.declareResource("HudLeftTex", groupId, new TextureHandle("PNG", "images/hud_left.png", TextureHandle.NEAREST_FILTER));
		resSrv.declareResource("HudLeftSpr", groupId, new SpriteHandle("HudLeftTex", 0.03, 0.90 / ar));
		resSrv.declareResource("HudRightTex", groupId, new TextureHandle("PNG", "images/hud_right.png", TextureHandle.NEAREST_FILTER));
		resSrv.declareResource("HudRightSpr", groupId, new SpriteHandle("HudRightTex", 0.03, 0.90 / ar));

		resSrv.declareResource("HudFrameTex", groupId, new TextureHandle("PNG", "images/hud2_1024.png"));
		resSrv.declareResource("HudFrameSpr", groupId, new SpriteHandle("HudFrameTex", 1.0, 0.045));
		resSrv.declareResource("LifeTex", groupId, new TextureHandle("PNG", "images/ship_standard_32x32.png"));
		resSrv.declareResource("LifeSpr", groupId, new SpriteHandle("LifeTex", 0.023, 0.023));
		resSrv.declareResource("Cannon1Tex", groupId, new TextureHandle("PNG", "images/cannon_powerup1_32x32.png"));
		resSrv.declareResource("Cannon1Spr", groupId, new SpriteHandle("Cannon1Tex", 0.023, 0.023));
		resSrv.declareResource("Cannon2Tex", groupId, new TextureHandle("PNG", "images/cannon_powerup2_32x32.png"));
		resSrv.declareResource("Cannon2Spr", groupId, new SpriteHandle("Cannon2Tex", 0.023, 0.023));
		resSrv.declareResource("CoolerTex", groupId, new TextureHandle("PNG", "images/cooler_powerup_32x32.png"));
		resSrv.declareResource("CoolerSpr", groupId, new SpriteHandle("CoolerTex", 0.023, 0.023));
		resSrv.declareResource("ScoreFont", groupId, new FontHandle("DisplayOTF", FontHandle.PLAIN, 50));		
		resSrv.declareResource("RocketReloadFont", groupId, new FontHandle("DisplayOTF", FontHandle.PLAIN, 32));		
	}

	@Override
	public void engage() {
		super.engage();
		EventService evtSrv = EventService.getInstance(getCore());
		evtSrv.addListener(this, ScoreUpdateEvent.TYPE_ID);
		evtSrv.addListener(this, ShieldUpdateEvent.TYPE_ID);
		evtSrv.addListener(this, LivesUpdateEvent.TYPE_ID);
		evtSrv.addListener(this, HeatUpdateEvent.TYPE_ID);
		evtSrv.addListener(this, CannonPowerUpEvent.TYPE_ID);
		evtSrv.addListener(this, CoolerPowerUpEvent.TYPE_ID);
		evtSrv.addListener(this, RocketReloadUpdateEvent.TYPE_ID);
		evtSrv.addListener(this, EnimyDestroyedEvent.TYPE_ID);
		evtSrv.addListener(this, DestroyedEvent.TYPE_ID);
		
		double referenceResolution = PropertyService.getInstance(getCore()).getDoubleProperty(SpaceSweeper.REFERENCE_RESOLUTION_PROP);
		SceneService scnSrv = SceneService.getInstance(getCore());
		this.baseNode = scnSrv.createNode();
		this.baseNode.setPose(0.5, 1.0 / scnSrv.getAspectRatio() - 0.045 / 2, 0);
		
//		SpriteVisual visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("HudFrameSpr");
//		this.baseNode.addVisual(visual);
//		scnSrv.getOverlayRoot().addNode(this.baseNode);

		// init top
		double ar = scnSrv.getAspectRatio();
		SpriteVisual visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("HudTopSpr");
		this.baseNode = scnSrv.createNode();
		this.baseNode.addVisual(visual);
		this.baseNode.setPose(0.5, (1 - 0.091 / 2) / ar, 0);
		scnSrv.getOverlayRoot().addNode(this.baseNode);
		
		// init bottom
		visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("HudBottomSpr");
		SceneNode node = scnSrv.createNode();
		node.addVisual(visual);
		node.setPose(0.5, 0.029 / ar / 2, 0);
		scnSrv.getOverlayRoot().addNode(node);

		// init left
		visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("HudLeftSpr");
		node = scnSrv.createNode();
		node.addVisual(visual);
		node.setPose(0.03 / 2, (0.029 / ar + (0.44 / ar)) , 0);
		scnSrv.getOverlayRoot().addNode(node);

		// init right
		visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("HudRightSpr");
		node = scnSrv.createNode();
		node.addVisual(visual);
		node.setPose(1 - 0.03 / 2, (0.029 / ar + (0.44 / ar)) , 0);
		scnSrv.getOverlayRoot().addNode(node);
		
		double yPos = ((0.091 / ar) * 0.05);
		
		// initialize score
		this.score = new TextVisual(getCore(), "ScoreFont");
		this.score.setColor(Color.WHITE);
		this.score.setScale(1.0 / referenceResolution);
		this.score.setText("00000000");
		node = scnSrv.createNode();
		node.addVisual(this.score);
		node.setPose(-0.42, yPos , 0);
		this.baseNode.addNode(node);
		
		yPos = ((0.091 / ar) * 0.05);
		
		// initialize lives
		visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("LifeSpr");
		for (int i = 0; i < this.lives.length; i++) {
			node = scnSrv.createNode();
			this.lives[i] = visual.newInstance();
			node.addVisual(this.lives[i]);
			node.setPose(0.369 + 0.025 * i, yPos, 0);
			this.baseNode.addNode(node);
		}
		
		// initialize power up
		node = scnSrv.createNode();
		visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("Cannon1Spr");
		this.powerUps[0] = visual.newInstance();
		node.addVisual(this.powerUps[0]);
		node.setPose(0.212, yPos, 0);
		this.baseNode.addNode(node);

		node = scnSrv.createNode();
		visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("Cannon2Spr");
		this.powerUps[1] = visual.newInstance();
		node.addVisual(this.powerUps[1]);
		node.setPose(0.212, yPos, 0);
		this.baseNode.addNode(node);

		node = scnSrv.createNode();
		visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("CoolerSpr");
		this.powerUps[2] = visual.newInstance();
		node.addVisual(this.powerUps[2]);
		node.setPose(0.212 + 0.025, yPos, 0);
		this.baseNode.addNode(node);
		
		yPos = ((0.091 / ar) * 0.07);
		
		// init shields
		this.shields.engage();
		this.shields.setBarColor(new Color(242.0 / 255.0, 61.0 / 255.0, 32.0 / 255.0));
		this.shields.setBackColor(new Color(95.0 / 255.0, 86.0 / 255.0, 107.0 / 255.0));
		this.shields.setFrameColor(Color.BLACK);
		node = this.shields.getBaseNode();
		node.setPose(-0.145, yPos, 0);
		this.baseNode.addNode(node);
		this.shieldUpdater = new BarUpdateTask(getCore(), this.shields);
		TaskService.getInstance(getCore()).attachTask(this.shieldUpdater);
		
		// init heat
		this.heat.engage();
		this.heat.setBarColor(Color.ORANGE);
		this.heat.setBackColor(new Color(95.0 / 255.0, 86.0 / 255.0, 107.0 / 255.0));
		this.heat.setFrameColor(Color.BLACK);
		this.heat.setPercentage(0);
		node = this.heat.getBaseNode();
		node.setPose(0.038, yPos, 0);
		this.baseNode.addNode(node);
		this.heatUpdater = new BarUpdateTask(getCore(), this.heat);
		this.heatUpdater.reset(0.0);
		TaskService.getInstance(getCore()).attachTask(this.heatUpdater);
		
		// init rocket
		this.rocket.engage();
		this.rocket.setBarColor(new Color(0, 0.7, 0));
		this.rocket.setBackColor(new Color(95.0 / 255.0, 86.0 / 255.0, 107.0 / 255.0));
		this.rocket.setFrameColor(Color.WHITE);
		this.rocket.setPercentage(0);
		node = this.rocket.getBaseNode();
		node.setPose(0.0, -0.055 / ar, 0);
		this.baseNode.addNode(node);
		this.rocketUpdater = new BarUpdateTask(getCore(), this.rocket);
		this.rocketUpdater.reset(1.0);
		this.rocketUpdater.setSpeed(0.02);
		TaskService.getInstance(getCore()).attachTask(this.rocketUpdater);
		
		this.rocketText = new TextVisual(getCore(), "RocketReloadFont");
		this.rocketText.setColor(Color.WHITE);
		this.rocketText.setScale(1.0 / referenceResolution);
		this.rocketText.setText("Missile Ready");
		this.rocketText.setAllignment(Alignment.CENTER);
		node = scnSrv.createNode();
		node.addVisual(this.rocketText);
		
		node.setPose(0.0, -0.09 / ar, 0);
		this.baseNode.addNode(node);
		
		// initialize values
		this.hitCount = 0;
		this.destroyedMessages.add("Ups, what's that?\n\n You can do better!");
		this.destroyedMessages.add("Don't give up.\n\nYou can make it!");
		this.destroyedMessages.add("Asteroids, asteroids!\n\nI don't like them, do you?");
		this.destroyedMessages.add("Well, it seems that today\n\nis not your best day.");
		this.destroyedMessages.add("Today is a good day to die!");
	}

	@Override
	public void disengage() {
		EventService.getInstance(getCore()).removeListener(this);
		TaskService.getInstance(getCore()).destroyTask(this.shieldUpdater);
		this.shieldUpdater = null;
		this.shields.disengage();
		
		TaskService.getInstance(getCore()).destroyTask(this.heatUpdater);
		this.heatUpdater = null;
		this.heat.disengage();

		TaskService.getInstance(getCore()).destroyTask(this.rocketUpdater);
		this.rocketUpdater = null;
		this.rocket.disengage();
		
		EventService.getInstance(getCore()).removeListener(this);
		SceneService.getInstance(getCore()).destroyNode(this.baseNode);
		this.baseNode = null;
		super.disengage();
	}
	
	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(ScoreUpdateEvent.TYPE_ID)) {
			handleScoreUpdate((ScoreUpdateEvent) event);
		} else if (event.isOfType(LivesUpdateEvent.TYPE_ID)) {
			handleLivesUpdate((LivesUpdateEvent) event);
		} else if (event.isOfType(StageUpdateEvent.TYPE_ID)) {
			handleStageUpdate((StageUpdateEvent) event);
		} else if (event.isOfType(ShieldUpdateEvent.TYPE_ID)) {
			handleShieldStatusUpdate((ShieldUpdateEvent) event);
		} else if (event.isOfType(HeatUpdateEvent.TYPE_ID)) {
			handleHeatStatusUpdate((HeatUpdateEvent) event);
		} else if (event.isOfType(CannonPowerUpEvent.TYPE_ID)) {
			handleCannonPowerUpEvent((CannonPowerUpEvent) event);
		} else if (event.isOfType(CoolerPowerUpEvent.TYPE_ID)) {
			handleCoolerPowerUpEvent((CoolerPowerUpEvent) event);
		} else if (event.isOfType(RocketReloadUpdateEvent.TYPE_ID)) {
			handleRocketReloadUpdate((RocketReloadUpdateEvent) event);
		} else if (event.isOfType(EnimyDestroyedEvent.TYPE_ID)) {
			this.hitCount++;
			if (hitCount % CNT_DESTROYED_MESSAGE == 0) {
				EventService.getInstance(getCore()).dispatchEvent(new MessageEvent("Congratulations!\n\nYou have destroyed " + this.hitCount + " asteroids."));
			}
		} else if (event.isOfType(DestroyedEvent.TYPE_ID)) {
			handleDestroyed((DestroyedEvent) event);
		}
	}

	private void handleDestroyed(DestroyedEvent event) {
		if (event.getEntityTypeId().equals(ShipEntity.TYPE)) {
			if (this.destroyedMessages.isEmpty()) {
				this.destroyedMessages.addAll(this.usedDestroyedMessages);
				this.usedDestroyedMessages.clear();
			}
			int n = this.rnd.nextInt(this.destroyedMessages.size());
			EventService.getInstance(getCore()).dispatchEvent(new MessageEvent(this.destroyedMessages.get(n)));
			this.usedDestroyedMessages.add(this.destroyedMessages.remove(n));
		}
	}

	private void handleRocketReloadUpdate(RocketReloadUpdateEvent event) {
		if (event.getReloadState() <= 0.01) {
			this.rocketUpdater.reset(event.getReloadState());
		} else {
			this.rocketUpdater.setPercentage(event.getReloadState());
		}
		
		if (event.getReloadState() < 0.995) {
			this.rocketText.setText("Missile Reloading");
		} else {
			this.rocketText.setText("Missile Ready");			
		}
	}

	private void handleCoolerPowerUpEvent(CoolerPowerUpEvent event) {
		switch (event.getLevel()) {
		case 0:
			this.powerUps[2].setMask(0x0000);
			break;
			
		case 1:
			this.powerUps[2].setMask(0xFFFF);
			break;
		}
	}

	private void handleCannonPowerUpEvent(CannonPowerUpEvent event) {
		switch (event.getLevel()) {
		case 0:
			this.powerUps[0].setMask(0x0000);
			this.powerUps[1].setMask(0x0000);
			break;
			
		case 1:
			this.powerUps[0].setMask(0xFFFF);
			this.powerUps[1].setMask(0x0000);
			break;
			
		case 2:
			this.powerUps[0].setMask(0x0000);
			this.powerUps[1].setMask(0xFFFF);
			break;
		}
	}

	private void handleLivesUpdate(LivesUpdateEvent event) {
		for (int i = 0; i < this.lives.length; ++i) {
			if (i < event.getLives() - 1) {
				this.lives[i].setMask(0xFFFF);
			} else {
				this.lives[i].setMask(0x0000);
			}
		}
	}

	private void handleStageUpdate(StageUpdateEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void handleShieldStatusUpdate(ShieldUpdateEvent event) {
		if (event.getEntityType() == ShipEntity.TYPE) {
			this.shieldUpdater.setPercentage(event.getShieldState());
		}
	}

	private void handleHeatStatusUpdate(HeatUpdateEvent event) {
		if (event.getEntityType() == ShipEntity.TYPE) {
			this.heatUpdater.setPercentage(event.getHeatState());
		}
	}
	
	private void handleScoreUpdate(ScoreUpdateEvent event) {
		this.score.setText(String.format("%08d", event.getScore()));
	}
}
