package org.cogaen.spacesweeper.view;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.lwjgl.input.KeyCode;
import org.cogaen.lwjgl.input.KeyPressedEvent;
import org.cogaen.lwjgl.scene.Alignment;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.FadeInTask;
import org.cogaen.lwjgl.scene.FadeOutTask;
import org.cogaen.lwjgl.scene.FontHandle;
import org.cogaen.lwjgl.scene.MultiLineLabelVisual;
import org.cogaen.lwjgl.scene.RectangleVisual;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.spacesweeper.state.InfoState;
import org.cogaen.spacesweeper.state.MenuState;
import org.cogaen.spacesweeper.state.PlayState;
import org.cogaen.task.TaskService;
import org.cogaen.view.View;

public class MenuView extends View implements EventListener {

	private static final CogaenId COVER_IS_GONE = new CogaenId("CoverIsGone");
	private static final CogaenId COVER_IS_HERE = new CogaenId("CoverIsHere");
	private static final double FADE_TIME = 1.0;
	private Visual cover;
	private boolean busy;
	private Event eventToDispatch;
	private int key;
	
	public MenuView(Core core) {
		super(core);
	}
	
	@Override
	public void registerResources(CogaenId groupId) {
		super.registerResources(groupId);
		
		ResourceService resSrv = ResourceService.getInstance(getCore());
		if (System.getProperty("os.name").startsWith("Mac")) {
			resSrv.declareResource("MenuFont", groupId, new FontHandle("D3-Euronism", FontHandle.PLAIN, 25));		
		} else {
			resSrv.declareResource("MenuFont", groupId, new FontHandle("D3 Euronism", FontHandle.PLAIN, 25));					
		}
	}



	@Override
	public void engage() {
		super.engage();
		
		EventService evtSrv = EventService.getInstance(getCore());
		evtSrv.addListener(this, KeyPressedEvent.TYPE_ID);
		evtSrv.addListener(this, COVER_IS_GONE);
		evtSrv.addListener(this, COVER_IS_HERE);
		
		SceneService scnSrv = SceneService.getInstance(getCore());
		
		SceneNode node = scnSrv.createNode();
		node.setPose(0.5, 0.5 / scnSrv.getAspectRatio(), 0);
		
		double referenceResolution = PropertyService.getInstance(getCore()).getDoubleProperty(SpaceSweeper.REFERENCE_RESOLUTION_PROP);
		MultiLineLabelVisual mll = new MultiLineLabelVisual(getCore(), "MenuFont", 0.5 * referenceResolution, 0.5 * referenceResolution);
		mll.setScale(1.0 / referenceResolution);
		mll.setAlignment(Alignment.CENTER);
		mll.setColor(Color.WHITE);
		
		mll.setText("Space Sweeper Version " + SpaceSweeper.VERSION + "\n\n1 ... Play Game\n2 ... Info\n3 ... Quit Application");
		node.addVisual(mll);
		scnSrv.getOverlayRoot().addNode(node);
		
		this.cover = new RectangleVisual(1.0, 1.0 / scnSrv.getAspectRatio());
		this.cover.setColor(Color.BLACK);
		node = scnSrv.createNode();
		node.addVisual(this.cover);
		node.setPose(0.5, 0.5 / scnSrv.getAspectRatio(), 0);
		scnSrv.getOverlayRoot().addNode(node);
		
		FadeOutTask fadeOut = new FadeOutTask(getCore(), this.cover, FADE_TIME);
		fadeOut.setFinishedEventId(COVER_IS_GONE);
		TaskService.getInstance(getCore()).attachTask(fadeOut);
		this.busy = true;
		this.key = 0;
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
			handleKeyPressed((KeyPressedEvent) event);
		} else if (event.isOfType(COVER_IS_GONE)) {
			this.busy = false;
			if (this.key != 0) {
				handleUserInput(this.key);
				this.key = 0;
			}
		} else if (event.isOfType(COVER_IS_HERE)) {
			this.busy = false;
			EventService.getInstance(getCore()).dispatchEvent(this.eventToDispatch);
		}
	}

	private void handleUserInput(int key) {
		
		switch (key) {
		case KeyCode.KEY_1:
			this.eventToDispatch = new SimpleEvent(PlayState.ID);
			fadeOut();
			break;
			
		case KeyCode.KEY_2:
			this.eventToDispatch = new SimpleEvent(InfoState.ID);
			fadeOut();
			break;
			
		case KeyCode.KEY_3:
			this.eventToDispatch = new SimpleEvent(MenuState.END_OF_MENU);
			fadeOut();
			break;
		}
		
	}
	
	private void handleKeyPressed(KeyPressedEvent event) {
		if (this.busy) {
			if (this.key == 0) {
				this.key = event.getKeyCode();
			}
			return;
		}

		handleUserInput(event.getKeyCode());
	}
	
	private void fadeOut() {
		this.busy = true;
		FadeInTask fadeIn = new FadeInTask(getCore(), this.cover, FADE_TIME);
		fadeIn.setFinishedEventId(COVER_IS_HERE);
		TaskService.getInstance(getCore()).attachTask(fadeIn);
	}
	
}
