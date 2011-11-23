package org.cogaen.spacesweeper.state;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.entity.BigAsteroid;
import org.cogaen.spacesweeper.event.GroupLoadUpdateEvent;
import org.cogaen.spacesweeper.physics.PhysicsService;
import org.cogaen.spacesweeper.view.LoadingView;
import org.cogaen.spacesweeper.view.PlayView;
import org.cogaen.state.BasicState;

public class PlayState extends BasicState implements EventListener {

	public static final CogaenId ID = new CogaenId("Play");
	public static final CogaenId END_OF_PLAY = new CogaenId("EndOfMenu");
	public static final double DEFAULT_WORLD_WIDTH = 45;
	public static final String WORLD_WIDTH_PROP = "worldWidth";
	private static final double DEFERRED_DELAY = 0.01;

	private PlayView playView;
	private LoadingView loadingView;
	private GameLogic logic;
	private ResourceService resSrv;
	private boolean loaded;
	
	public PlayState(Core core) {
		super(core);
		this.playView = new PlayView(core);
		this.loadingView = new LoadingView(core);
		this.logic = new GameLogic(core);
		
		this.resSrv = ResourceService.getInstance(core);
		this.resSrv.createGroup(ID);
		this.playView.registerResources(ID);
		this.loadingView.registerResources(ID);
	}
	
	@Override
	public void onEnter() {
		EventService.getInstance(getCore()).addListener(this, GroupLoadUpdateEvent.TYPE_ID);
		this.loadingView.engage();
		
		this.resSrv.loadGroupDeferred(ID);
		EventService.getInstance(getCore()).dispatchEvent(new GroupLoadUpdateEvent(ID, 0.0));
		this.loaded = false;
	}
	
	private void onEnter2() {
		this.loadingView.disengage();
		this.playView.engage();
		this.logic.engage();
		
		PropertyService propSrv = PropertyService.getInstance(getCore());
		double worldWidth = propSrv.getDoubleProperty(WORLD_WIDTH_PROP);

		double ar = SceneService.getInstance(getCore()).getAspectRatio();
		double physicsWorldWidth = worldWidth + BigAsteroid.RADIUS * 2;
		double physicsWorldHeight = (worldWidth / ar) + BigAsteroid.RADIUS * 2;
		PhysicsService.getInstance(getCore()).setWorldSize(physicsWorldWidth, physicsWorldHeight);				
	}

	@Override
	public void onExit() {
		EventService.getInstance(getCore()).removeListener(this);

		if (this.loadingView.isEngaged()) {
			this.loadingView.disengage();
		}
		
		if (this.logic.isEngaged()) {
			this.logic.disengage();
		}

		if (this.playView.isEngaged()) {
			this.playView.disengage();
		}
		ResourceService.getInstance(getCore()).unloadGroup(ID);
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(GroupLoadUpdateEvent.TYPE_ID)) {
			handleGroupLoadUpdate((GroupLoadUpdateEvent) event);
		}
	}

	private void handleGroupLoadUpdate(GroupLoadUpdateEvent event) {
		if (!this.loaded && this.resSrv.hasNextDeferredResource()) {
			double percentage = this.resSrv.loadNextDeferredResource();
			EventService.getInstance(getCore()).dispatchEvent(new GroupLoadUpdateEvent(ID, percentage), DEFERRED_DELAY);
		} else if (!this.loaded){
			EventService.getInstance(getCore()).dispatchEvent(new GroupLoadUpdateEvent(ID, 1.0), DEFERRED_DELAY);
			this.loaded = true;
		} else {
			onEnter2();
		}
	}	
}
