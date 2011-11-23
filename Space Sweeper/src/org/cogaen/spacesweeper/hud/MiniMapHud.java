package org.cogaen.spacesweeper.hud;

import org.cogaen.core.Core;
import org.cogaen.core.Engageable;
import org.cogaen.lwjgl.scene.Camera;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.RectangleVisual;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.state.PlayState;

public class MiniMapHud implements Engageable {

	private static final String MINI_MAP_WIDTH_PROP = "miniMapWidth";
	private static final double DEFAULT_MINI_MAP_WIDTH = 0.15;
	private static final double GAP = 0.03;
	
	private boolean engaged;
	private Core core;
	private Camera camera;
	private Visual frame;
	
	public MiniMapHud(Core core) {
		this.core = core;
	}
	
	@Override
	public void engage() {
		initCamera();
		initBackground();
		initFrame();
		
		setActive(true);
		this.engaged = true;
	}

	private void initFrame() {
		SceneService scnSrv = SceneService.getInstance(this.core);
		PropertyService propSrv = PropertyService.getInstance(this.core);

		SceneNode node = scnSrv.createNode();
		double mapWidth = propSrv.getDoubleProperty(MINI_MAP_WIDTH_PROP, DEFAULT_MINI_MAP_WIDTH);
		RectangleVisual rec = new RectangleVisual(mapWidth, mapWidth / scnSrv.getAspectRatio());
		rec.setColor(Color.WHITE);
		rec.setFilled(false);
		node.addVisual(rec);
		
		double ar = scnSrv.getAspectRatio();
		node.setPose(1.0 - mapWidth / 2 - GAP, mapWidth / ar / 2 + GAP, 0);
		scnSrv.getOverlayRoot().addNode(node);
		this.frame = rec;
	}

	private void initBackground() {
		SceneService scnSrv = SceneService.getInstance(this.core);
		PropertyService propSrv = PropertyService.getInstance(this.core);
		double worldWidth = propSrv.getDoubleProperty(PlayState.WORLD_WIDTH_PROP);
		
		scnSrv.ensuerNumOfLayers(2);
		SceneNode node = scnSrv.createNode();
		RectangleVisual rec = new RectangleVisual(worldWidth, worldWidth * scnSrv.getAspectRatio());
		rec.setMask(0x0002);
		rec.setColor(Color.BLACK);
		rec.getColor().setAlpha(0.6);
		node.addVisual(rec);
		scnSrv.getLayer(2).addNode(node);
	}

	private void initCamera() {
		SceneService scnSrv = SceneService.getInstance(this.core);
		PropertyService propSrv = PropertyService.getInstance(this.core);

		this.camera = scnSrv.createCamera();
		int mapWidth = (int) (scnSrv.getScreenWidth() * propSrv.getDoubleProperty(MINI_MAP_WIDTH_PROP, DEFAULT_MINI_MAP_WIDTH));
		int mapHeight = (int) (mapWidth / scnSrv.getAspectRatio());
		int gap = (int) (scnSrv.getScreenWidth() * GAP);
		double worldWidth = propSrv.getDoubleProperty(PlayState.WORLD_WIDTH_PROP);
		
		this.camera.setViewPort(scnSrv.getScreenWidth() - mapWidth - gap, gap, mapWidth, mapHeight);
		this.camera.setZoom(scnSrv.getScreenWidth() / worldWidth);
		this.camera.setMask(0x0002); 
	}

	@Override
	public void disengage() {
		SceneService scnSrv = SceneService.getInstance(this.core);
		scnSrv.destroyCamera(this.camera);
		this.engaged = false;
	}

	@Override
	public boolean isEngaged() {
		return this.engaged;
	}

	public void setActive(boolean value) {
		this.camera.setActive(value);
		this.frame.setMask(value ? 0xFFFF : 0x0000);
	}
	
	public boolean isActive() {
		return this.camera.isActive();
	}

}
