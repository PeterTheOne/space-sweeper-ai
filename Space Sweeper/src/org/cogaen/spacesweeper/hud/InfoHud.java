package org.cogaen.spacesweeper.hud;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.lwjgl.scene.Alignment;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.FadeInTask;
import org.cogaen.lwjgl.scene.FadeOutTask;
import org.cogaen.lwjgl.scene.FontHandle;
import org.cogaen.lwjgl.scene.MultiLineLabelVisual;
import org.cogaen.lwjgl.scene.ReadableColor;
import org.cogaen.lwjgl.scene.RectangleVisual;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.task.TaskService;
import org.cogaen.view.AbstractHud;

public class InfoHud extends AbstractHud implements EventListener {

	public static final CogaenId READY_EVENT_ID = new CogaenId("Ready");
	private static final String WIDTH_PROP = "infoWidth";
	private static final String HEIGHT_PROP = "infoHeight";
	private static final double DEFAULT_WIDTH = 0.7;
	private static final double DEFAULT_HEIGHT = 0.45;
	private static final double MARGIN = 0.01;
	private static final double FADE_TIME = 0.5;
	
	private MultiLineLabelVisual mll;
	private String newText;
	private boolean busy;
	private boolean fading;
	private Visual backFrame;
	private Visual frame;
	
	public InfoHud(Core core) {
		super(core);
	}
	
	@Override
	public void registerResources(CogaenId groupId) {
		ResourceService resSrv = ResourceService.getInstance(getCore());
		if (!resSrv.isDeclared("InfoFnt")) {
			resSrv.declareResource("InfoFnt", groupId, new FontHandle("SanSerif", 24));
		}
	}

	@Override
	public void engage() {
		super.engage();
		EventService evtSrv = EventService.getInstance(getCore());
		evtSrv.addListener(this, FadeInTask.FADE_IN_FINISHED_EVENT_ID);
		evtSrv.addListener(this, FadeOutTask.FADE_OUT_FINISHED_EVENT_ID);
		
		SceneService scnSrv = SceneService.getInstance(getCore());
		SceneNode node = scnSrv.createNode();
		
		createFrame(node);
		this.mll = createMultiLineLaben(node);
		this.mll.setColor(Color.WHITE);
		node.addVisual(mll);
		
		node.setPose(0.5, 0.5 / scnSrv.getAspectRatio(), 0);
		scnSrv.getOverlayRoot().addNode(node);
		this.busy = false;
	}

	@Override
	public void disengage() {
		EventService.getInstance(getCore()).removeListener(this);
		SceneService.getInstance(getCore()).destroyAll();
		super.disengage();
	}
	
	private MultiLineLabelVisual createMultiLineLaben(SceneNode node) {
		PropertyService propSrv = PropertyService.getInstance(getCore());
		double refRes = propSrv.getDoubleProperty(SpaceSweeper.REFERENCE_RESOLUTION_PROP);
		double width = (propSrv.getDoubleProperty(WIDTH_PROP, DEFAULT_WIDTH) - MARGIN) * refRes;
		double height = (propSrv.getDoubleProperty(HEIGHT_PROP, DEFAULT_HEIGHT) - MARGIN) * refRes;
		
		MultiLineLabelVisual mll = new MultiLineLabelVisual(getCore(), "InfoFnt", width, height);
		mll.setScale(1.0 / refRes);
		node.addVisual(mll);
		
		return mll;
	}

	private void createFrame(SceneNode node) {
		PropertyService propSrv = PropertyService.getInstance(getCore());
		
		double width = propSrv.getDoubleProperty(WIDTH_PROP, DEFAULT_WIDTH);
		double height = propSrv.getDoubleProperty(HEIGHT_PROP, DEFAULT_HEIGHT);
		
		RectangleVisual rec = new RectangleVisual(width, height);
		rec.setFilled(true);
		rec.setColor(new Color(0.5, 0.5, 0.5));
		this.backFrame = rec;
		node.addVisual(rec);
		
		rec = new RectangleVisual(width, height);
		rec.setFilled(false);
		rec.setColor(Color.WHITE);
		this.frame = rec;
		node.addVisual(rec);
	}
	
	public void setText(String text) {
		if (this.busy) {
			return;
		} else if (!this.fading) {
			this.mll.setText(text);		
			return;
		}
		
		this.newText = text;
		
		FadeOutTask fadeOut = new FadeOutTask(getCore(), this.mll, FADE_TIME);
		TaskService.getInstance(getCore()).attachTask(fadeOut);
		this.busy = true;
	}
	
	public void setCenterAllignment(boolean center) {
		if (center) {
			this.mll.setAlignment(Alignment.CENTER);
		} else {
			this.mll.setAlignment(Alignment.LEFT);			
		}
	}
	
	public boolean isFading() {
		return fading;
	}

	public void setFading(boolean fading) {
		this.fading = fading;
	}
	
	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(FadeInTask.FADE_IN_FINISHED_EVENT_ID)) {
			this.busy = false;
			EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(READY_EVENT_ID));
		} else if (event.isOfType(FadeOutTask.FADE_OUT_FINISHED_EVENT_ID)) {
			this.mll.setText(this.newText);
			FadeInTask fadeIn = new FadeInTask(getCore(), this.mll, FADE_TIME);
			TaskService.getInstance(getCore()).attachTask(fadeIn);
		}
	}

	public void setBackColor(ReadableColor color) {
		this.backFrame.setColor(color);
	}
	
	public Color getBackColor() {
		return this.backFrame.getColor();
	}
	
	public void setFrameColor(ReadableColor color) {
		this.frame.setColor(color);
	}
	
	public Color getFrameColor() {
		return this.frame.getColor();
	}
	
	public boolean isBusy() {
		return this.busy;
	}

}
