package org.cogaen.spacesweeper.hud;

import java.util.ArrayList;
import java.util.List;

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
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.TypeWriterTask;
import org.cogaen.lwjgl.sound.Sound;
import org.cogaen.lwjgl.sound.SoundHandle;
import org.cogaen.lwjgl.sound.SoundService;
import org.cogaen.lwjgl.sound.Source;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.spacesweeper.event.MessageEvent;
import org.cogaen.task.TaskService;
import org.cogaen.view.AbstractHud;

public class MessageHud extends AbstractHud implements EventListener {

	private static final double WIDTH = 0.35;
	private static final double HEIGHT = 0.25;
	private static final double FADE_TIME = 0.5;
	private static final double DISPLAY_TIME = 5.0;
	private static final CogaenId REMOVE_MESSAGE = new CogaenId("RemoveMessage");
	private static final CogaenId SOUND_MESSAGE_POOL = new CogaenId("Message");
	private MultiLineLabelVisual messageText;
	private SceneNode baseNode;
	private boolean busy;
	private List<String> messageQueue = new ArrayList<String>();
	private boolean typeWriter = false;
	private int numSounds;
	private SoundService soundSrv;
	
	public MessageHud(Core core) {
		this(core, false);
	}
	
	public MessageHud(Core core, boolean typeWriter) {
		super(core);
		this.typeWriter = typeWriter;
		this.soundSrv = SoundService.getInstance(core);
	}

	@Override
	public void registerResources(CogaenId groupId) {
		super.registerResources(groupId);
		
		ResourceService resSrv = ResourceService.getInstance(getCore());
		this.numSounds = 0;
		resSrv.declareResource("MessageFnt", groupId, new FontHandle("DisplayOTF", FontHandle.PLAIN, 32));		
		resSrv.declareResource("MessageSnd" + (++this.numSounds), groupId, new SoundHandle("sound/computer1.wav"));		
		resSrv.declareResource("MessageSnd" + (++this.numSounds), groupId, new SoundHandle("sound/computer2.wav"));		
		resSrv.declareResource("MessageSnd" + (++this.numSounds), groupId, new SoundHandle("sound/computer3.wav"));		
	}

	@Override
	public void engage() {
		super.engage();
		EventService evtSrv = EventService.getInstance(getCore());
		evtSrv.addListener(this, MessageEvent.TYPE_ID);
		evtSrv.addListener(this, FadeInTask.FADE_IN_FINISHED_EVENT_ID);
		evtSrv.addListener(this, FadeOutTask.FADE_OUT_FINISHED_EVENT_ID);
		evtSrv.addListener(this, TypeWriterTask.TYPING_FINISHED_EVENT_ID);
		evtSrv.addListener(this, REMOVE_MESSAGE);
		
		double referenceResolution = PropertyService.getInstance(getCore()).getDoubleProperty(SpaceSweeper.REFERENCE_RESOLUTION_PROP);
		
		SceneService scnSrv = SceneService.getInstance(getCore());
		this.baseNode = scnSrv.createNode();
		this.baseNode.setPose(0.5, 0.5 / scnSrv.getAspectRatio(), 0);
		
		this.messageText = new MultiLineLabelVisual(getCore(), "MessageFnt", WIDTH * referenceResolution, HEIGHT * referenceResolution);
		this.messageText.setScale(1.0 / referenceResolution);
		this.messageText.setColor(Color.WHITE);
		this.messageText.setAlignment(Alignment.CENTER);
		this.baseNode.addVisual(this.messageText);
		
		scnSrv.getOverlayRoot().addNode(this.baseNode);
		this.busy = false;
		
		// init sound pool
		this.soundSrv.createPool(SOUND_MESSAGE_POOL);
		for (int i = 0; i < this.numSounds; ++i) {
			Source source = soundSrv.createSource();
			source.assignSound((Sound) ResourceService.getInstance(getCore()).getResource("MessageSnd" + (i + 1)));
			this.soundSrv.addToPool(SOUND_MESSAGE_POOL, source);
		}

//		EventService.getInstance(getCore()).dispatchEvent(new MessageEvent("Hello pilot!\n\nGet ready for the challenge\nof your life."));
//		EventService.getInstance(getCore()).dispatchEvent(new MessageEvent("Use arrow keys to steer ship.\n\nUse left control key to fire bullet."));
//		EventService.getInstance(getCore()).dispatchEvent(new MessageEvent("Destroy all asteroids to get to the next stage."));
//		EventService.getInstance(getCore()).dispatchEvent(new MessageEvent("Use right control key to launch cruise missile."), 30);
//		EventService.getInstance(getCore()).dispatchEvent(new MessageEvent("Space Sweeper V" + SpaceSweeper.VERSION), 60);
//		EventService.getInstance(getCore()).dispatchEvent(new MessageEvent("Cogaen V" + getCore().getVersion()), 90);
	}

	@Override
	public void disengage() {
		EventService.getInstance(getCore()).removeListener(this);
		SceneService.getInstance(getCore()).destroyNode(this.baseNode);
		this.soundSrv.destroyPool(SOUND_MESSAGE_POOL);
		super.disengage();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(MessageEvent.TYPE_ID)) {
			handleMessage((MessageEvent) event);
		} else if (event.isOfType(FadeInTask.FADE_IN_FINISHED_EVENT_ID)) {
			EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(REMOVE_MESSAGE), DISPLAY_TIME);
		} else if (event.isOfType(TypeWriterTask.TYPING_FINISHED_EVENT_ID)) {
			EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(REMOVE_MESSAGE), DISPLAY_TIME);
		} else if (event.isOfType(REMOVE_MESSAGE)) {
			FadeOutTask fadeOut = new FadeOutTask(getCore(), this.messageText, FADE_TIME);
			TaskService.getInstance(getCore()).attachTask(fadeOut);
		} else if (event.isOfType(FadeOutTask.FADE_OUT_FINISHED_EVENT_ID)) {
			if (!this.messageQueue.isEmpty()) {
				displayMessage(this.messageQueue.remove(0));
				this.soundSrv.playFromPool(SOUND_MESSAGE_POOL);
			} else {
				this.busy = false;
			}
		} else if (event.isOfType(TypeWriterTask.TYPING_FINISHED_EVENT_ID)) {
			
		}
	}
	
	private void displayMessage(String text) {
		if (this.typeWriter) {
			TypeWriterTask typeWriter = new TypeWriterTask(getCore(), this.messageText, text);
			TaskService.getInstance(getCore()).attachTask(typeWriter);
			this.messageText.setText("");
			this.messageText.getColor().setAlpha(1.0);
			this.busy = true;			
		} else {
			this.messageText.setText(text);
			FadeInTask fadeIn = new FadeInTask(getCore(), this.messageText, FADE_TIME);
			TaskService.getInstance(getCore()).attachTask(fadeIn);
			this.busy = true;
		}
	}
	
	private void handleMessage(MessageEvent event) {
		if (this.busy) {
			this.messageQueue.add(event.getMessage());
		} else {
			displayMessage(event.getMessage());
			this.soundSrv.playFromPool(SOUND_MESSAGE_POOL);
		}
	}

	public boolean isTypeWriter() {
		return typeWriter;
	}

	public void setTypeWriter(boolean typeWriter) {
		this.typeWriter = typeWriter;
	}
}
