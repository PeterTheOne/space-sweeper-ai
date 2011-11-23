package org.cogaen.spacesweeper.representation;

import org.cogaen.core.Core;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.ParticleSystem;
import org.cogaen.lwjgl.scene.ParticleSystemService;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SpriteVisual;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.lwjgl.sound.SoundService;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.view.PlayView;

public class MediumAsteroidRepresentation extends BaseRepresentation {

	private static final double SHADOW_DISTANCE = 0.15;
	private SceneNode node1;
	private SceneNode node2;
	private Visual mark;
	
	public MediumAsteroidRepresentation(Core core, CogaenId entityId) {
		super(core, entityId);
	}

	@Override
	public void engage() {
		super.engage();
		
		this.node1 = getSceneService().createNode();
		this.node2 = getSceneService().createNode();
		
		SpriteVisual visual = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("MediumAsteroidSpr");
		this.node1.addVisual(visual);
		this.mark = visual;

		visual = visual.newInstance();
		visual.setColor(new Color(0, 0, 0, 0.5));
		// don't display in mini map
		visual.setMask(0x0001);
		this.node2.addVisual(visual);
		this.node2.setPose(SHADOW_DISTANCE, -SHADOW_DISTANCE, 0);
		
		getNode().addNode(this.node2);
		getNode().addNode(this.node1);
//		CircleVisual debugVisual = new CircleVisual(BigAsteroid.RADIUS);
//		debugVisual.setColor(Color.GREEN);
//		getNode().addVisual(debugVisual);
	}

	@Override
	public void updatePosition(double x, double y, double angle) {
		super.updatePosition(x, y, 0);
		this.node1.setPose(0, 0, angle);
		this.node2.setPose(SHADOW_DISTANCE, -SHADOW_DISTANCE, angle);
	}
	
	@Override
	public void destroy() {		
		ParticleSystem ps = ParticleSystemService.getInstance(getCore()).getFromPool(PlayView.SMALl_FIRE_POOL);
		ps.getEmitter().setPose(getNode().getPosX(), getNode().getPosY(), 0);
		ps.setActive(0.15);
		
		SoundService sndSrv = SoundService.getInstance(getCore());
		sndSrv.playFromPool(PlayView.SOUND_MEDIUM_EXPLOSION_POOL);
		super.destroy();
	}
	
	@Override
	public void setMark(boolean value) {
		super.setMark(value);
		this.mark.setColor(value ? Color.RED : Color.WHITE);
	}
	
}
