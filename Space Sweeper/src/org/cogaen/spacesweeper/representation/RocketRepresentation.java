package org.cogaen.spacesweeper.representation;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.ParticleSystem;
import org.cogaen.lwjgl.scene.ParticleSystemService;
import org.cogaen.lwjgl.scene.RectangleVisual;
import org.cogaen.lwjgl.sound.SoundService;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.entity.RocketEntity;
import org.cogaen.spacesweeper.event.BulletHitEvent;
import org.cogaen.spacesweeper.event.SoundEvent;
import org.cogaen.spacesweeper.view.PlayView;

public class RocketRepresentation extends BaseRepresentation implements EventListener {

	private ParticleSystem ps;
	
	public RocketRepresentation(Core core, CogaenId entityId) {
		super(core, entityId);
	}

	@Override
	public void engage() {
		super.engage();
		
		EventService evtSrv = EventService.getInstance(getCore());
		evtSrv.addListener(this, BulletHitEvent.TYPE_ID);
		
		RectangleVisual rec = new RectangleVisual(RocketEntity.RADIUS / 2, RocketEntity.RADIUS * 2);
		rec.setColor(Color.ORANGE);
		rec.setMask(0x0001);
		getNode().addVisual(rec);
		this.ps = ParticleSystemService.getInstance(getCore()).getFromPool(PlayView.ROCKET_JET_STREAM_POOL);
		ps.setActive(true);		
	}
	
	@Override
	public void disengage() {
		EventService.getInstance(getCore()).removeListener(this);
		super.disengage();
	}

	@Override
	public void destroy() {		
		this.ps.setActive(false);
		
		ParticleSystem ps = ParticleSystemService.getInstance(getCore()).getFromPool(PlayView.SMALl_FIRE_POOL);
		ps.getEmitter().setPose(getNode().getPosX(), getNode().getPosY(), 0);
		ps.setActive(0.15);
		
		EventService.getInstance(getCore()).dispatchEvent(new SoundEvent("RocketExplosionSnd"));
		
		super.destroy();
	}

	@Override
	public void setPose(double x, double y, double angle) {
		super.setPose(x, y, angle);
		this.ps.getEmitter().setPose(x, y, angle + Math.PI);
	}

	@Override
	public void updatePosition(double x, double y, double angle) {
		super.updatePosition(x, y, angle);
		this.ps.getEmitter().setPose(x, y, angle + Math.PI);
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(BulletHitEvent.TYPE_ID)) {
			handleBulletHit((BulletHitEvent) event);
		}
	}

	private void handleBulletHit(BulletHitEvent event) {
		if (!event.getEntityId().equals(getEntityId())) {
			return;
		}
		
		ParticleSystem ps = ParticleSystemService.getInstance(getCore()).getFromPool(PlayView.SMALL_EXPLOSION_POOL);
		ps.getEmitter().setPose(getNode().getPosX(), getNode().getPosY(), 0);
		ps.setActive(0.1);		
		
		SoundService sndSrv = SoundService.getInstance(getCore());
		sndSrv.playFromPool(PlayView.SOUND_BULLET_EXPLOSION_POOL);
	}	
}
