/*
 * Copyright (c) 2011 Roman Divotkey
 * 
 * This file is subject to the terms and conditions defined in 
 * file 'LICENSE.txt', which is part of this source code package.
 */

package org.cogaen.spacesweeper;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;

import org.cogaen.core.Core;
import org.cogaen.core.ServiceException;
import org.cogaen.core.Version;
import org.cogaen.entity.EntityService;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.logging.ConsoleLogger;
import org.cogaen.logging.LogFilter;
import org.cogaen.logging.LoggingService;
import org.cogaen.logging.PassThroughFilter;
import org.cogaen.logging.SourceFilter;
import org.cogaen.lwjgl.input.KeyboardService;
import org.cogaen.lwjgl.input.KeyCode;
import org.cogaen.lwjgl.input.KeyPressedEvent;
import org.cogaen.lwjgl.input.MouseService;
import org.cogaen.lwjgl.scene.ParticleSystemService;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.sound.SoundService;
import org.cogaen.name.CogaenId;
import org.cogaen.name.IdService;
import org.cogaen.property.PropertyService;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.physics.PhysicsService;
import org.cogaen.spacesweeper.state.CopyrightState;
import org.cogaen.spacesweeper.state.InfoState;
import org.cogaen.spacesweeper.state.MenuState;
import org.cogaen.spacesweeper.state.PlayState;
import org.cogaen.spacesweeper.state.SplashState;
import org.cogaen.state.GameStateService;
import org.cogaen.task.TaskService;
import org.cogaen.time.Clock;
import org.cogaen.time.TimeService;

public class SpaceSweeper extends Applet implements EventListener {

	public static final Version VERSION = new Version(2, 3, 8);
	public static final String APP_TITLE_PROP = "appTitle";
	public static final String REFERENCE_RESOLUTION_PROP = "referenceResolution";
	public static final CogaenId PAUSED = new CogaenId("Paused");
	public static final CogaenId STARTED = new CogaenId("Started");
	private static final long serialVersionUID = 6724709111886674987L;
	private static final double REFERENCE_RESOLUTION = 1680;
	private static final String DEFAULT_CONFIG_FILE = "space_sweeper.cfg";
	private static final LogFilter STANDARD_FILTER = LoggingService.WARNING_LEVEL_FILTER;
	private static final LogFilter DEBUG_FILTER = new PassThroughFilter();
	private static final String DEFAULT_APP_TITLE = "Space Sweeper";
	private static final String START_STATE_PROP = "startState";
	private static final String FULLSCREEN_APPLET_PARAM = "fullscreen";
	
	private Core core;
	private Canvas parent;
	private Thread gameThread;
	private String appletText;
	
	public static void main(String[] args) throws ServiceException {
		SpaceSweeper game = new SpaceSweeper();
		
		if (args.length > 0) {
			game.createAndInitialize(null, 0, 0, true, args[0]);
		} else {
			game.createAndInitialize(null, 0, 0, true, DEFAULT_CONFIG_FILE);
		}
		game.runApplication();
	}
	
	@Override
	public void init() {
		super.init();
		
		setLayout(new BorderLayout());
		this.parent = new Canvas() {

			private static final long serialVersionUID = 3909314825024252442L;

			@Override
			public void addNotify() {
				super.addNotify();
				try {
					startGame();
					setIgnoreRepaint(true);
				} catch (ServiceException e) {
					String msg = "unable to initialize game: " + e.getMessage();
					SpaceSweeper.this.appletText = msg;
					setIgnoreRepaint(false);
					repaint();
					e.printStackTrace();
				}
			}

			@Override
			public void removeNotify() {
				stopGame();
				super.removeNotify();
			}

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				FontMetrics fm = g.getFontMetrics();
				Rectangle2D rec = fm.getStringBounds(SpaceSweeper.this.appletText, g);
				double x = getWidth() / 2 - rec.getWidth() / 2;
				double y = getHeight() / 2 - rec.getHeight() / 2;
				g.drawString(SpaceSweeper.this.appletText, (int) x, (int) y);
			}
		};
		
		this.parent.setSize(getWidth(), getHeight());
		this.appletText = "Space Sweeper V" + VERSION;
		add(this.parent);
		this.parent.setFocusable(true);
		this.parent.requestFocus();
		this.parent.setIgnoreRepaint(false);
		this.parent.setVisible(true);
	}

	@Override
	public void destroy() {
		remove(this.parent);
		super.destroy();
	}
	
	@Override
	public void start() {
		super.start();
		EventService.getInstance(this.core).dispatchEvent(new SimpleEvent(STARTED));
		LoggingService.getInstance(this.core).logDebug("GAME", "applet started");
	}

	@Override
	public void stop() {
		EventService.getInstance(this.core).dispatchEvent(new SimpleEvent(PAUSED));
		LoggingService.getInstance(this.core).logDebug("GAME", "applet stopped");
		super.stop();
	}

	private void startGame() throws ServiceException {
		if (Boolean.parseBoolean(getParameter(FULLSCREEN_APPLET_PARAM))) {
			createAndInitialize(this.parent, 0, 0, true, null);			
		} else {
			createAndInitialize(this.parent, getWidth(), getHeight(), false, null);			
		}
		this.gameThread = new Thread() {

			@Override
			public void run() {
				try {
					runApplication();
				} catch (ServiceException e) {
					String msg = "unable to start game:" + e.getMessage();
					SpaceSweeper.this.appletText = msg;
					SpaceSweeper.this.parent.setIgnoreRepaint(false);
					SpaceSweeper.this.parent.repaint();
					e.printStackTrace();
				}
			}
		};
		
		this.gameThread.start();
	}
	
	private void stopGame() {
		GameStateService.getInstance(this.core).setCurrentState(GameStateService.END_STATE_ID);

		try {
			this.gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void registereFont(String filename) {
		ResourceService resSrv = ResourceService.getInstance(this.core);
		InputStream is = resSrv.getStream(filename);
		try {
			Font font = Font.createFont(Font.TRUETYPE_FONT, is);
			LoggingService.getInstance(this.core).logInfo("GAME", "registered font '" + font.getFontName() + "'");
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
		} catch (FontFormatException e) {
			LoggingService.getInstance(this.core).logInfo("GAME", "infalid font format: " + filename);
		} catch (IOException e) {
			LoggingService.getInstance(this.core).logInfo("GAME", "unable to load fonr: " + filename);
		}
	}
	
	private void createAndInitialize(Canvas parent, int width, int height, boolean fullscreen, String configFile) throws ServiceException {
		this.core = new Core();
		this.core.addService(new ConsoleLogger(STANDARD_FILTER));
		this.core.addService(new EventService());
		this.core.addService(new GameStateService());
		this.core.addService(new IdService());
		this.core.addService(new TimeService());
		this.core.addService(new TaskService());
		this.core.addService(new ResourceService());
		this.core.addService(new EntityService());
		this.core.addService(new PropertyService(configFile));
		this.core.addService(new SceneService(parent, width, height, fullscreen, true));
		this.core.addService(new ParticleSystemService());
		this.core.addService(new KeyboardService());
		this.core.addService(new MouseService());
		this.core.addService(new SoundService());
		this.core.addService(new PhysicsService());
	}

	private void runApplication() throws ServiceException {
		core.startup();
		
		PropertyService prpSrv = PropertyService.getInstance(this.core);
		if (prpSrv.getProperty(REFERENCE_RESOLUTION_PROP) == null) {
			prpSrv.setDoubleProperty(REFERENCE_RESOLUTION_PROP, REFERENCE_RESOLUTION);
		}
		
		SceneService.getInstance(this.core).setTitle(prpSrv.getProperty(APP_TITLE_PROP, DEFAULT_APP_TITLE) + " - Version " + VERSION);
		if (prpSrv.getBoolProperty("debug", true)) {
			LoggingService logSrv = LoggingService.getInstance(this.core);
			logSrv.removeAllFilters();
			
			String logSrc = prpSrv.getProperty("log_src_filter", "null");
			logSrv.addFilter(DEBUG_FILTER);
			if (logSrc != null && !"null".equalsIgnoreCase(logSrc)) {
				logSrv.addFilter(new SourceFilter(logSrc));
			}
		}
		
		registereFont("font/D3Euronism.ttf");
		registereFont("font/displayotf.ttf");
		initGameStates();
		EventService evtSrv = EventService.getInstance(this.core);
		evtSrv.addListener(this, SceneService.WINDOW_CLOSE_REQUEST);
		evtSrv.addListener(this, KeyPressedEvent.TYPE_ID);
		checkMouseCursor();
		runGameLoop();
		EventService.getInstance(this.core).removeListener(this);
		core.shutdown();
	}		

	private void runGameLoop() {
		GameStateService stateSrv = GameStateService.getInstance(this.core);
		SceneService scnSrv = SceneService.getInstance(this.core);
				
		Clock clock = new Clock();
		while (!stateSrv.isEndState()) {
			clock.tick();
			this.core.update(clock.getDelta());
			
			scnSrv.renderScene();
		}
	}

	private void initGameStates() {
		GameStateService stateSrv = GameStateService.getInstance(this.core);
		
		stateSrv.addState(new SplashState(this.core), SplashState.ID);
		stateSrv.addState(new PlayState(this.core), PlayState.ID);
		stateSrv.addState(new CopyrightState(this.core), CopyrightState.ID);
		stateSrv.addState(new MenuState(this.core), MenuState.ID);
		stateSrv.addState(new InfoState(this.core), InfoState.ID);
		
		stateSrv.addTransition(SplashState.ID, InfoState.ID, SplashState.END_OF_SPLASH);
		stateSrv.addTransition(MenuState.ID, PlayState.ID, PlayState.ID);
		stateSrv.addTransition(PlayState.ID, MenuState.ID, PlayState.END_OF_PLAY);
		stateSrv.addTransition(MenuState.ID, CopyrightState.ID, MenuState.END_OF_MENU);
		stateSrv.addTransition(MenuState.ID, InfoState.ID, InfoState.ID);
		stateSrv.addTransition(InfoState.ID, MenuState.ID, InfoState.END_OF_INFO);
		stateSrv.addTransition(CopyrightState.ID, GameStateService.END_STATE_ID, CopyrightState.END_OF_COPYRIGHT);

		CogaenId startId = new CogaenId(PropertyService.getInstance(this.core).getProperty(START_STATE_PROP, SplashState.ID.toString()));
		stateSrv.setCurrentState(PlayState.ID);
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(SceneService.WINDOW_CLOSE_REQUEST)) {
			GameStateService stateSrv = GameStateService.getInstance(this.core);
			stateSrv.setCurrentState(GameStateService.END_STATE_ID);
		} else if(event.isOfType(KeyPressedEvent.TYPE_ID)) {
			handleKeyPressed((KeyPressedEvent) event);
		}
	}

	private void handleKeyPressed(KeyPressedEvent event) {
		switch (event.getKeyCode()) {
		case KeyCode.KEY_F11:
			try {
				toogleFullScreen();
			} catch (ServiceException e) {
				LoggingService.getInstance(this.core).logNotice("GAME", "unable to switch to fullscreen mode: " + e.getMessage());
			}
			break;
			
		case KeyCode.KEY_F10:
			toggleVSync();
		}
	}

	private void toggleVSync() {
		SceneService scnSrv = SceneService.getInstance(this.core);
		scnSrv.setVSync(!scnSrv.isVSync());
		LoggingService.getInstance(this.core).logInfo("GAME", "vsync set to " + scnSrv.isVSync());
	}

	private void toogleFullScreen() throws ServiceException {
		SceneService scnSrv = SceneService.getInstance(this.core);
		scnSrv.setFullscreen(!scnSrv.isFullscreen());

		checkMouseCursor();
		LoggingService.getInstance(this.core).logInfo("GAME", "switch to " + (scnSrv.isFullscreen() ? "fullscreen" : "window") + " mode");
	}
	
	private void checkMouseCursor() {
		if (SceneService.getInstance(this.core).isFullscreen()) {
			KeyboardService.getInstance(this.core).hideMouseCursor();
		} else {
			KeyboardService.getInstance(this.core).showMouseCursor();
		}		
	}
}
