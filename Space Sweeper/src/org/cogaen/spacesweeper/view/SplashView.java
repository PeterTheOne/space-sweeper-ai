package org.cogaen.spacesweeper.view;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.lwjgl.input.KeyPressedEvent;
import org.cogaen.lwjgl.scene.FadeInTask;
import org.cogaen.lwjgl.scene.FadeOutTask;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.SpriteHandle;
import org.cogaen.lwjgl.scene.SpriteVisual;
import org.cogaen.lwjgl.scene.TextureHandle;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.state.SplashState;
import org.cogaen.task.TaskService;
import org.cogaen.view.View;

public class SplashView extends View implements EventListener {

	public static final CogaenId DISPLAYING = new CogaenId("Displaying");
	private static final double FADE_TIME = 1.0;
	private static final CogaenId FADE_OUT = new CogaenId("FadeOut");
	private static final double DISPLAY_TIME = 5;
	private FadeInTask fadeIn;
	private SpriteVisual splash;
	private boolean busy;
	private boolean userAbort;
	
	public SplashView(Core core) {
		super(core);
	}
	
	@Override
	public void registerResources(CogaenId groupId) {
		super.registerResources(groupId);
		ResourceService resSrv = ResourceService.getInstance(getCore());
		
		resSrv.declareResource("splashTex", groupId, new TextureHandle("JPG", "images/cogaen_mit_800x600.jpg"));
		double height = 1.0 / SceneService.getInstance(getCore()).getAspectRatio();
		resSrv.declareResource("SplashSpr", groupId, new SpriteHandle("splashTex", height * 4.0 / 3.0, height));
	}

	@Override
	public void engage() {
		super.engage();
		EventService evtSrv = EventService.getInstance(getCore());
		evtSrv.addListener(this, KeyPressedEvent.TYPE_ID);
		evtSrv.addListener(this, FadeInTask.FADE_IN_FINISHED_EVENT_ID);
		evtSrv.addListener(this, FadeOutTask.FADE_OUT_FINISHED_EVENT_ID);
		evtSrv.addListener(this, FADE_OUT);

		SceneService scnSrv = SceneService.getInstance(getCore());
		
		SceneNode node = scnSrv.createNode();
		node.setPose(0.5, 0.5 / scnSrv.getAspectRatio(), 0);
		this.splash = (SpriteVisual) ResourceService.getInstance(getCore()).getResource("SplashSpr");
		node.addVisual(splash);
		scnSrv.getOverlayRoot().addNode(node);
		
		this.fadeIn = new FadeInTask(getCore(), splash, FADE_TIME);
		TaskService.getInstance(getCore()).attachTask(fadeIn);
		this.busy = true;
		this.userAbort = false;
	}

	@Override
	public void disengage() {
		EventService.getInstance(getCore()).removeListener(this);
		SceneService.getInstance(getCore()).destroyAll();
		super.disengage();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(KeyPressedEvent.TYPE_ID)) {
			if (this.busy) {
				this.userAbort = true;
			} else {
				EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(FADE_OUT));
			}
		} else if (event.isOfType(FadeInTask.FADE_IN_FINISHED_EVENT_ID)) {
			if (this.userAbort) {
				EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(FADE_OUT));				
			} else {
				EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(FADE_OUT), DISPLAY_TIME);
			}
			this.busy = false;
		} else if (event.isOfType(FADE_OUT)) {
			FadeOutTask fadeOut = new FadeOutTask(getCore(), this.splash, FADE_TIME);
			TaskService.getInstance(getCore()).attachTask(fadeOut);
			this.busy = true;
		} else if (event.isOfType(FadeOutTask.FADE_OUT_FINISHED_EVENT_ID)) {
			EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(SplashState.END_OF_SPLASH));			
		}
	}
	
}
