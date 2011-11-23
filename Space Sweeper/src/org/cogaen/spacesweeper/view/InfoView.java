package org.cogaen.spacesweeper.view;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.lwjgl.input.KeyCode;
import org.cogaen.lwjgl.input.KeyPressedEvent;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.FadeInTask;
import org.cogaen.lwjgl.scene.FadeOutTask;
import org.cogaen.lwjgl.scene.RectangleVisual;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.resource.TextHandle;
import org.cogaen.spacesweeper.hud.InfoHud;
import org.cogaen.spacesweeper.state.InfoState;
import org.cogaen.task.TaskService;
import org.cogaen.view.View;

public class InfoView extends View implements EventListener {

	private static final double FADE_TIME = 1.0;
	private static final CogaenId COVER_IS_GONE = new CogaenId("CoverIsGone");
	private static final CogaenId COVER_IS_HERE = new CogaenId("CoverIsHere");
	private InfoHud infoHud;
	private int curText;
	private int nPages;
	private Visual cover;
	private boolean busy;
	private int key;
	
	public InfoView(Core core) {
		super(core);
		this.infoHud = new InfoHud(core);
	}

	@Override
	public void registerResources(CogaenId groupId) {
		ResourceService resSrv = ResourceService.getInstance(getCore());
		
		this.infoHud.registerResources(groupId);
		this.nPages = 0;
		resSrv.declareResource("infoTxt" + (++nPages), groupId, new TextHandle("text/info.txt"));
		resSrv.declareResource("infoTxt" + (++nPages), groupId, new TextHandle("text/log1.txt"));
		resSrv.declareResource("infoTxt" + (++nPages), groupId, new TextHandle("text/log2.txt"));
		resSrv.declareResource("infoTxt" + (++nPages), groupId, new TextHandle("text/log3.txt"));
		resSrv.declareResource("infoTxt" + (++nPages), groupId, new TextHandle("text/controls.txt"));
		resSrv.declareResource("infoTxt" + (++nPages), groupId, new TextHandle("text/credits.txt"));
	}

	@Override
	public void engage() {
		super.engage();
		EventService evtSrv = EventService.getInstance(getCore());
		evtSrv.addListener(this, KeyPressedEvent.TYPE_ID);
		evtSrv.addListener(this, COVER_IS_GONE);
		evtSrv.addListener(this, COVER_IS_HERE);
		evtSrv.addListener(this, InfoHud.READY_EVENT_ID);
		
		this.infoHud.engage();
		this.infoHud.setCenterAllignment(true);
		this.infoHud.setFading(false);
		this.infoHud.setText((String) ResourceService.getInstance(getCore()).getResource("infoTxt1"));
		this.infoHud.setBackColor(new Color(95.0 / 255.0, 86.0 / 255.0, 107.0 / 255.0));
		this.infoHud.setFrameColor(Color.WHITE);
		this.curText = 1;

		SceneService scnSrv = SceneService.getInstance(getCore());
		this.cover = new RectangleVisual(1.0, 1.0 / scnSrv.getAspectRatio());
		this.cover.setColor(Color.BLACK);
		SceneNode node = scnSrv.createNode();
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
		this.infoHud.disengage();
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
			EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(InfoState.END_OF_INFO));			
		} else if (event.isOfType(InfoHud.READY_EVENT_ID)) {
			if (this.key != 0) {
				handleUserInput(this.key);
				this.key = 0;
			}
		}
	}
	
	private void handleUserInput(int key) {
		if (key == KeyCode.KEY_ESC) {
			fadeOut();
			return;
		}		
				
		if (this.curText++ >= this.nPages) {
			fadeOut();
		} else {
			this.infoHud.setFading(true);
			String text = (String) ResourceService.getInstance(getCore()).getResource("infoTxt" + this.curText);
			this.infoHud.setText(text);
		}		
	}

	private void handleKeyPressed(KeyPressedEvent event) {
		
		if (this.busy || this.infoHud.isBusy()) {
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
