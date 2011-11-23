package org.cogaen.spacesweeper.view;

import java.util.Properties;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.lwjgl.sound.Sound;
import org.cogaen.lwjgl.sound.SoundService;
import org.cogaen.lwjgl.sound.Source;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.spacesweeper.event.MessageEvent;
import org.cogaen.spacesweeper.event.ShieldUpdateEvent;
import org.cogaen.spacesweeper.event.SoundEvent;
import org.cogaen.spacesweeper.event.StageUpdateEvent;
import org.cogaen.spacesweeper.state.GameLogic;
import org.cogaen.spacesweeper.task.GuideTask;
import org.cogaen.spacesweeper.task.RandomTextTask;
import org.cogaen.spacesweeper.util.Logic;
import org.cogaen.task.TaskService;

public class PlayViewLogic extends Logic implements EventListener {

	private static final CogaenId SOUND_GENERAL_POOL = new CogaenId("General");
	private static final CogaenId START_GUIDE = new CogaenId("StartGuide");
	private static final double MESSAGE_DELAY = 1.0;
	private static final int NUM_OF_SOURCES = 5;
	private static final double SHIELD_WARNING_DELAY = 0.25;
	private static final String USE_GUIDE_PROP = "useGuide";
	private EventService evtSrv;
	private boolean shieldWarning;
	private SoundService sndSrv;
	private GuideTask guide;
	private RandomTextTask rndText;
	private boolean useGuide = false;
	
	public PlayViewLogic(Core core) {
		super(core);
		this.evtSrv = EventService.getInstance(core);
		this.sndSrv = SoundService.getInstance(core);
	}

	@Override
	public void engage() {
		super.engage();
		this.evtSrv.addListener(this, StageUpdateEvent.TYPE_ID);
		this.evtSrv.addListener(this, GameLogic.GAME_OVER);
		this.evtSrv.addListener(this, ShieldUpdateEvent.TYPE_ID);
		this.evtSrv.addListener(this, GameLogic.TRIPLE_KILL);
		this.evtSrv.addListener(this, SoundEvent.TYPE_ID);
		this.evtSrv.addListener(this, GuideTask.END_OF_GUIDE_EVENT_ID);
		this.evtSrv.addListener(this, START_GUIDE);
		this.shieldWarning = false;

		this.useGuide = PropertyService.getInstance(getCore()).getBoolProperty(USE_GUIDE_PROP, true);
		if (!this.useGuide) {
			startRandomText();
		}
		
		// general sound pool
		sndSrv.createPool(SOUND_GENERAL_POOL);
		for (int i = 0; i < NUM_OF_SOURCES; ++i) {
			Source src = sndSrv.createSource();
			sndSrv.addToPool(SOUND_GENERAL_POOL, src);
		}
	}

	@Override
	public void disengage() {
		if (this.guide != null) {
			TaskService.getInstance(getCore()).destroyTask(this.guide);
			this.guide = null;
		}
		if (this.rndText != null) {
			TaskService.getInstance(getCore()).destroyTask(this.rndText);
			this.rndText = null;
		}
		this.sndSrv.destroyPool(SOUND_GENERAL_POOL);
		this.evtSrv.removeListener(this);
		super.disengage();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(StageUpdateEvent.TYPE_ID)) {
			handleStageUpdate((StageUpdateEvent) event);
		} else if (event.isOfType(GameLogic.GAME_OVER)) {
			this.evtSrv.dispatchEvent(new MessageEvent("Game Over"), MESSAGE_DELAY);			
		} else if (event.isOfType(ShieldUpdateEvent.TYPE_ID)) {
			handleShieldUpdate((ShieldUpdateEvent) event);
		} else if (event.isOfType(GameLogic.TRIPLE_KILL)) {
			this.evtSrv.dispatchEvent(new MessageEvent("Triple Kill"));			
		} else if (event.isOfType(SoundEvent.TYPE_ID)) {
			playSound(((SoundEvent) event).getSoundResource());
		} else if (event.isOfType(GuideTask.END_OF_GUIDE_EVENT_ID)) {
			TaskService.getInstance(getCore()).destroyTask(this.guide);
			this.guide = null;
			startRandomText();
		} else if (event.isOfType(START_GUIDE)) {
			this.guide = new GuideTask(getCore(), (String) ResourceService.getInstance(getCore()).getResource("GuideTxt"));
			TaskService.getInstance(getCore()).attachTask(this.guide);						
		}
	}

	private void startRandomText() {
		this.rndText = new RandomTextTask(getCore());
		this.rndText.addPages((String) ResourceService.getInstance(getCore()).getResource("RandomTxt"));
		this.rndText.addPage("Space Sweeper V" + SpaceSweeper.VERSION);
		this.rndText.addPage("Cogaen V" + getCore().getVersion());
		Properties properties = System.getProperties();
		this.rndText.addPage("Operating System\n\n" + properties.getProperty("os.name") + ", V" + properties.getProperty("os.version"));
		this.rndText.addPage("Java Version " + properties.getProperty("java.version"));
		TaskService.getInstance(getCore()).attachTask(this.rndText);
	}

	private void playSound(String soundResource) {
		Source source = this.sndSrv.getSource(SOUND_GENERAL_POOL);
		source.assignSound((Sound) ResourceService.getInstance(getCore()).getResource(soundResource));
		source.playSound();		
	}

	private void handleShieldUpdate(ShieldUpdateEvent event) {
		if (this.shieldWarning && event.getShieldState() <= 0.05) {
			this.evtSrv.dispatchEvent(new MessageEvent("Warning, Shields Low!"), SHIELD_WARNING_DELAY);						
			this.shieldWarning = false;
			playSound("AlarmSnd");
		} else if (event.getShieldState() >= 0.15) {
			this.shieldWarning = true;
		}
	}

	private void handleStageUpdate(StageUpdateEvent event) {
		if (event.getStage() > 1 || !this.useGuide) {
			this.evtSrv.dispatchEvent(new MessageEvent("Prepare for Stage " + event.getStage()), MESSAGE_DELAY);
		} else {
			this.evtSrv.dispatchEvent(new SimpleEvent(START_GUIDE), MESSAGE_DELAY);
		}
	}

}
