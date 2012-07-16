package org.cogaen.spacesweeper.representation;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.ParticleSystem;
import org.cogaen.lwjgl.scene.ParticleSystemService;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SpriteVisual;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.lwjgl.sound.Sound;
import org.cogaen.lwjgl.sound.SoundService;
import org.cogaen.lwjgl.sound.Source;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.component.CannonComponent;
import org.cogaen.spacesweeper.component.OperationalAIComponent;
import org.cogaen.spacesweeper.component.RocketLauncherComponent;
import org.cogaen.spacesweeper.component.ShieldComponent;
import org.cogaen.spacesweeper.entity.ShipEntity;
import org.cogaen.spacesweeper.event.CannonPowerUpEvent;
import org.cogaen.spacesweeper.event.CoolerPowerUpEvent;
import org.cogaen.spacesweeper.event.InvulerableEvent;
import org.cogaen.spacesweeper.event.SoundEvent;
import org.cogaen.spacesweeper.event.TargetChangeEvent;
import org.cogaen.spacesweeper.event.ThrustEvent;
import org.cogaen.spacesweeper.view.PlayView;
import org.cogaen.task.AbstractTask;
import org.cogaen.task.TaskService;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class AIShipRepresentation extends BaseRepresentation implements EventListener {

	private static final double SHADOW_DISTANCE = 0.1;
	
	private PlayView playView;
	
	private SceneNode node1;
	private SceneNode node2;

	private ParticleSystem ps1;
	private ParticleSystem ps2;
	private Visual visual;
	private Source source;
	
	private CogaenId targetLineId;
	private FlowLineRepresentation targetLine;
	
	public AIShipRepresentation(Core core, CogaenId entityId, PlayView playView) {
		super(core, entityId);
		this.playView = playView;
	}
	
	@Override
	public void engage() {
		super.engage();

		EventService evtSrv = EventService.getInstance(getCore());
		evtSrv.addListener(this, ThrustEvent.TYPE_ID);
		evtSrv.addListener(this, InvulerableEvent.TYPE_ID);
		evtSrv.addListener(this, CannonComponent.SHOT_FIRED_EVENT);
		evtSrv.addListener(this, RocketLauncherComponent.ROCKET_FIRED_EVENT);
		evtSrv.addListener(this, CannonPowerUpEvent.TYPE_ID);
		evtSrv.addListener(this, CoolerPowerUpEvent.TYPE_ID);
		evtSrv.addListener(this, CannonComponent.OVERHEAT_EVENT);
		evtSrv.addListener(this, ShieldComponent.SHIELD_POWERUP_EVENT);
		evtSrv.addListener(this, TargetChangeEvent.TYPE_ID);
		
		this.node1 = getSceneService().createNode();
		this.node2 = getSceneService().createNode();
		
		this.visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("ShipSpr");
		this.node1.addVisual(this.visual);

		Visual shadow = this.visual.newInstance();
		shadow.setColor(new Color(0, 0, 0, 0.5));
		// don't display in mini map
		shadow.setMask(0x001);
		this.node2.addVisual(shadow);
		getNode().addNode(this.node2);
		getNode().addNode(this.node1);
		
		this.ps1 = getParticleSystem();
		this.ps2 = getParticleSystem();
		this.source = SoundService.getInstance(getCore()).createSource();
		this.source.assignSound((Sound) ResourceService.getInstance(getCore()).getResource("ThrustSnd"));
		this.source.setLooping(true);
		this.source.setGain(0.4);
		
		this.targetLineId = new CogaenId("AIShip Target Line");
		this.targetLine = new FlowLineRepresentation(getCore(), this.targetLineId, 
				0, 0f, 0f);
		this.playView.addRepresentation(this.targetLineId, this.targetLine);
		this.targetLine.setPose(0, 0, 0);
	}
	
	@Override
	public void disengage() {
		super.disengage();
		this.playView.removeRepresentation(this.targetLineId);
		
		EventService.getInstance(getCore()).removeListener(this);
		SoundService.getInstance(getCore()).destroySource(this.source);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		this.ps1.setActive(false);
		this.ps2.setActive(false);

		ParticleSystem ps = ParticleSystemService.getInstance(getCore()).getFromPool(PlayView.SMALl_FIRE_POOL);
		ps.getEmitter().setPose(getNode().getPosX(), getNode().getPosY(), 0);
		ps.setActive(0.1);

		ps = ParticleSystemService.getInstance(getCore()).getFromPool(PlayView.SMOKE_POOL);
		ps.getEmitter().setPose(getNode().getPosX(), getNode().getPosY(), 0);
		ps.setActive(0.35);
		
		this.source.stopSound();
		SoundService sndSrv = SoundService.getInstance(getCore());
		Source src = sndSrv.getSource(PlayView.SOUND_SHIP_DESTROYED_POOL);
		src.playSound();			
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(ThrustEvent.TYPE_ID)) {
			handleThrust((ThrustEvent) event);
		} else if (event.isOfType(InvulerableEvent.TYPE_ID)) {
			handleInvulnerable((InvulerableEvent) event);
		} else if (event.isOfType(CannonComponent.SHOT_FIRED_EVENT)) {
			SoundService sndSrv = SoundService.getInstance(getCore());
			sndSrv.playFromPool(PlayView.SOUND_SHOT_POOL);
		} else if (event.isOfType(CannonPowerUpEvent.TYPE_ID)) {
			CannonPowerUpEvent powerUp = (CannonPowerUpEvent) event;
			if (powerUp.getLevel() > 0) {
				EventService.getInstance(getCore()).dispatchEvent(new SoundEvent("CannonPowerUpSnd"));
			}
		} else if (event.isOfType(ShieldComponent.SHIELD_POWERUP_EVENT)) {
			EventService.getInstance(getCore()).dispatchEvent(new SoundEvent("ShieldPowerUpSnd"));
		} else if (event.isOfType(CoolerPowerUpEvent.TYPE_ID)) {
			CoolerPowerUpEvent coolerPowerUp = (CoolerPowerUpEvent) event;
			if (coolerPowerUp.getLevel() > 0) {
				EventService.getInstance(getCore()).dispatchEvent(new SoundEvent("CoolerPowerUpSnd"));
			}
		} else if (event.isOfType(CannonComponent.OVERHEAT_EVENT)) {
			EventService.getInstance(getCore()).dispatchEvent(new SoundEvent("OverheatSnd"));
		} else if (event.isOfType(RocketLauncherComponent.ROCKET_FIRED_EVENT)) {
			SoundService.getInstance(getCore()).playFromPool(PlayView.ROCKET_LAUNCH_POOL);
		} else if (event.isOfType(TargetChangeEvent.TYPE_ID)) {
			handleTargetChangeEvent((TargetChangeEvent) event);
		}
	}
	
	private void handleTargetChangeEvent(TargetChangeEvent event) {
		double dX = event.getTargetPosX() - event.getPosX();
		double dY = event.getTargetPosY() - event.getPosY();
		this.targetLine.setVector(dX, dY);
		targetLine.setPose(event.getPosX(), event.getPosY(), 0);
	}

	private void handleInvulnerable(InvulerableEvent event) {
		if (!event.getEntityid().equals(getEntityId())) {
			return;
		}
		
		InvulnerableTask task = new InvulnerableTask(getCore(), this.visual, event.getTimeFrame());
		TaskService.getInstance(getCore()).attachTask(task);
	}

	private void handleThrust(ThrustEvent event) {
		if (!event.getEntityId().equals(getEntityId())) {
			return;
		}
		
		if (event.getThrust() > 0) {
			this.ps1.setActive(true);
			this.ps2.setActive(true);
			this.source.playSound();
		} else {
			this.ps1.setActive(false);
			this.ps2.setActive(false);
			this.source.stopSound();
		}
		
	}

	@Override
	public void updatePosition(double x, double y, double angle) {
		super.updatePosition(x, y, 0);
		
		this.node1.setPose(0, 0, angle);
		this.node2.setPose(SHADOW_DISTANCE, -SHADOW_DISTANCE, angle);
		
		double d = -ShipEntity.RADIUS;
		double ex = -d * Math.sin(angle + 0.15);
		double ey = d * Math.cos(angle + 0.15);
		this.ps1.getEmitter().setPose(x + ex, y + ey, Math.PI + angle);
		
		d = -ShipEntity.RADIUS;
		ex = -d * Math.sin(angle - 0.2);
		ey = d * Math.cos(angle - 0.2);
		this.ps2.getEmitter().setPose(x + ex, y + ey, Math.PI + angle);
		
		this.targetLine.setPose(x, y, 0);
	}

	private ParticleSystem getParticleSystem() {
		return ParticleSystemService.getInstance(getCore()).getFromPool(new CogaenId("JetStream"));
	}
	
	private static class InvulnerableTask extends AbstractTask {

		private static final double BLINK_TIME = 0.1;
		private static final double ALPHA_VALUE = 0.25;
		private Visual visual;
		private Timer timer;
		private double timeStamp1;
		private double timeStamp2;
		
		public InvulnerableTask(Core core, Visual visual, double timeFrame) {
			super(core, "invulnerable task");
			this.visual = visual;
			this.timer = TimeService.getInstance(getCore()).getTimer();
			this.timeStamp1 = this.timer.getTime() + timeFrame;
			this.timeStamp2 = 0;
		}
		
		@Override
		public void update() {
			if (this.timer.getTime() >= this.timeStamp1) {
				TaskService.getInstance(getCore()).destroyTask(this);
				return;
			}
			
			if (this.timer.getTime() >= this.timeStamp2) {
				if (this.visual.getColor().getAlpha() == 1.0) {
					this.visual.getColor().setAlpha(ALPHA_VALUE);
				} else {
					this.visual.getColor().setAlpha(1.0);					
				}
				this.timeStamp2 = this.timer.getTime() + BLINK_TIME;
			}
		}

		@Override
		public void destroy() {
			this.visual.getColor().setAlpha(1.0);
		}
		
	}

}
