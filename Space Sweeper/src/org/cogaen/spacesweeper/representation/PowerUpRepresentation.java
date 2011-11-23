package org.cogaen.spacesweeper.representation;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.lwjgl.scene.BlinkTask;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SpriteVisual;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.event.PowerUpWarningEvent;
import org.cogaen.spacesweeper.event.SoundEvent;
import org.cogaen.task.TaskService;

public class PowerUpRepresentation extends BaseRepresentation implements EventListener {

	public enum Type {SHIELD, CANNON1, CANNON2, COOLER}
	private static final double SHADOW_DISTANCE = 0.1;
	private static final double BLINK_INTERVAL = 0.25;
	private static final double SOUND_DELAY = 0.25;	
	
	private BlinkTask blinkMiniMap;
	private BlinkTask blinkWarning;
	private BlinkTask blinkWarningShadow;
	private Type powerUpType;
	private Visual powerUpVsl;
	private Visual shadowVsl;
	
	public PowerUpRepresentation(Core core, CogaenId entityId, Type type) {
		super(core, entityId, 1);
		this.powerUpType = type;
	}

	@Override
	public void engage() {
		super.engage();
		EventService.getInstance(getCore()).addListener(this, PowerUpWarningEvent.TYPE_ID);
		SpriteVisual powerUpMapVsl = null;
		
		switch (this.powerUpType) {
		case SHIELD:
			this.powerUpVsl = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("ShieldPowerUpSpr");
			powerUpMapVsl = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("ShieldPowerUpMiniMapSpr");
			break;
			
		case CANNON1:
			this.powerUpVsl = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("CannonPowerUp1Spr");
			powerUpMapVsl = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("CannonPowerUp1MiniMapSpr");
			break;

		case CANNON2:
			this.powerUpVsl = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("CannonPowerUp2Spr");
			powerUpMapVsl = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("CannonPowerUp2MiniMapSpr");
			break;
			
		case COOLER:
			this.powerUpVsl = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("CoolerPowerUpSpr");
			powerUpMapVsl = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("CoolerPowerUpMiniMapSpr");
			break;
		}
		
		SceneNode node1 = getSceneService().createNode();
		SceneNode node2 = getSceneService().createNode();
		
		// don't display in mini-map
		powerUpVsl.setMask(0x0001);
		node1.addVisual(powerUpVsl);
				
		this.shadowVsl = powerUpVsl.newInstance();
		shadowVsl.setColor(new Color(0, 0, 0, 0.5));
		// don't display in mini-map
		shadowVsl.setMask(0x0001);
		node2.addVisual(shadowVsl);
		node2.setPose(SHADOW_DISTANCE, -SHADOW_DISTANCE, 0);
		
		getNode().addNode(node2);
		getNode().addNode(node1);

		// mini-map visual
		powerUpMapVsl.setMask(0x0002);
		node1.addVisual(powerUpMapVsl);
		this.blinkMiniMap = new BlinkTask(getCore(), powerUpMapVsl, BLINK_INTERVAL);
		TaskService.getInstance(getCore()).attachTask(this.blinkMiniMap);
		
		EventService.getInstance(getCore()).dispatchEvent(new SoundEvent("PowerUpEngageSnd"), SOUND_DELAY);
	}

	@Override
	public void disengage() {
		EventService.getInstance(getCore()).removeListener(this);
		TaskService taskSrv = TaskService.getInstance(getCore());
		taskSrv.destroyTask(this.blinkMiniMap);
		if (this.blinkWarning != null) {
			taskSrv.destroyTask(this.blinkWarning);
		}
		if (this.blinkWarningShadow != null) {
			taskSrv.destroyTask(this.blinkWarningShadow);
		}
		super.disengage();
	}

	@Override
	public void updatePosition(double x, double y, double angle) {
		super.updatePosition(x, y, 0);
	}

	@Override
	public void setPose(double x, double y, double angle) {
		super.setPose(x, y, 0);
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(PowerUpWarningEvent.TYPE_ID)) {
			handlePowerUpWarning((PowerUpWarningEvent) event);
		}
	}

	private void handlePowerUpWarning(PowerUpWarningEvent event) {
		if (event.getEntityId().equals(getEntityId())) {
			this.blinkWarning = new BlinkTask(getCore(), this.powerUpVsl, BLINK_INTERVAL);
			this.blinkWarningShadow = new BlinkTask(getCore(), this.shadowVsl, BLINK_INTERVAL);
			
			TaskService taskSrv = TaskService.getInstance(getCore());
			taskSrv.attachTask(this.blinkWarning);
			taskSrv.attachTask(this.blinkWarningShadow);
		}
	}
	
}
