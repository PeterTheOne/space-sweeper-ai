package org.cogaen.spacesweeper.hud;

import org.cogaen.core.Core;
import org.cogaen.core.Engageable;
import org.cogaen.entity.EntityService;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.logging.LogListener;
import org.cogaen.logging.LoggingService;
import org.cogaen.logging.LoggingService.Priority;
import org.cogaen.lwjgl.input.KeyCode;
import org.cogaen.lwjgl.input.KeyPressedEvent;
import org.cogaen.lwjgl.input.KeyReleasedEvent;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.CommandLineVisual;
import org.cogaen.lwjgl.scene.RectangleVisual;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.spacesweeper.state.GameLogic;
import org.cogaen.spacesweeper.task.MoveTask;
import org.cogaen.task.TaskService;
import org.cogaen.time.TimeService;

public class ConsoleHud implements Engageable, EventListener, LogListener {

	private static final double REFERENCE_RESOLUTION = 1280.0;
	private static final double REFERENCE_SCALE = 1.0 / REFERENCE_RESOLUTION;

	private static final String PROMPT = "> ";
	private static final double MARGIN = 0.01;
	private static final double GAP = 0.06;
	private static final CogaenId TIMER_ID = new CogaenId("GUI_TIMER");
	private static final String CONSOLE_LINES_PROP = "consoleLines";
	
	private boolean engaged;
	private Core core;
	private SceneNode node;
	private boolean shift;
	private CommandLineVisual cml;
	private StringBuffer command = new StringBuffer();
	private MoveTask moveTask;
	private boolean active;
	private boolean logging;
	
	public ConsoleHud(Core core) {
		this.core = core;
		TimeService timeSrv = TimeService.getInstance(this.core);
		if (!timeSrv.hasTimer(TIMER_ID)) {
			timeSrv.createTimer(TIMER_ID);
		}
	}
	
	@Override
	public void engage() {
		SceneService scnSrv = SceneService.getInstance(this.core);
		EventService evtSrv = EventService.getInstance(this.core);
		evtSrv.addListener(this, KeyPressedEvent.TYPE_ID);
		evtSrv.addListener(this, KeyReleasedEvent.TYPE_ID);
		
		this.cml = initCommandLineVisual();
		this.node = scnSrv.createNode();

		createFrame(node, this.cml.getWidth() * REFERENCE_SCALE + MARGIN, this.cml.getHeight() * REFERENCE_SCALE + MARGIN);
		this.node.addVisual(cml);
		
		scnSrv.getOverlayRoot().addNode(this.node);
		
		this.moveTask = new MoveTask(this.core, "console mover", this.node);
		this.moveTask.setTimer(TimeService.getInstance(this.core).getTimer(TIMER_ID));
		TaskService.getInstance(this.core).attachTask(this.moveTask);

		setActive(false, false);
		this.command.setLength(0);
		this.logging = false;
		this.engaged = true;
	}
	
	@Override
	public void disengage() {
		EventService.getInstance(this.core).removeListener(this);
		SceneService scnSrv = SceneService.getInstance(this.core);
		scnSrv.destroyNode(node);
		
		TaskService.getInstance(this.core).destroyTask(this.moveTask);
		this.moveTask = null;

		LoggingService.getInstance(this.core).removeListener(this);
		
		this.engaged = false;
	}

	private CommandLineVisual initCommandLineVisual() {
		PropertyService propSrv = PropertyService.getInstance(this.core);
		
		CommandLineVisual cml = new CommandLineVisual(this.core, "CmlFont", propSrv.getIntProperty(CONSOLE_LINES_PROP, 8));
		cml.setTimer(TimeService.getInstance(this.core).getTimer(TIMER_ID));
		cml.addString("Command Line Interface");
		cml.newLine();
		cml.addString(PROMPT);
		cml.setWidth(REFERENCE_RESOLUTION * 0.95);
		cml.setScale(REFERENCE_SCALE);
		
		return cml;
	}

	private void createFrame(SceneNode node, double width, double height) {
		RectangleVisual rec = new RectangleVisual(width, height);
		rec.setColor(new Color(1, 1, 1, 0.25));
		this.node.addVisual(rec);
		
		rec = new RectangleVisual(width, height);
		rec.setFilled(false);
		rec.setColor(new Color(1, 1, 1, 1));		
		this.node.addVisual(rec);
	}

	@Override
	public boolean isEngaged() {
		return this.engaged;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(KeyPressedEvent.TYPE_ID)) {
			handleKeyPressed((KeyPressedEvent) event);
		} else if (event.isOfType(KeyReleasedEvent.TYPE_ID)) {
			handleKeyReleased((KeyReleasedEvent) event);
		}
	}
	
	public void setActive(boolean value, boolean fade) {
		SceneService scnSrv = SceneService.getInstance(this.core);
		if (value) {
			double x = 0.5;
			double y = 1.0 / scnSrv.getAspectRatio() - this.cml.getHeight()  * REFERENCE_SCALE / 2 - GAP;
			
			if (fade) {
				this.moveTask.setTargetPosition(x, y);			
			} else {
				this.moveTask.setCurrentPosition(x, y);
			}
			
			this.active = true;
		} else {
			double x = 0.5;
			double y = 1.0 / scnSrv.getAspectRatio() + this.cml.getHeight()  * REFERENCE_SCALE / 2 + GAP;

			if (fade) {
				this.moveTask.setTargetPosition(x, y);
			} else {
				this.moveTask.setCurrentPosition(x, y);
			}
			this.active = false;			
		}
	}
	
	public boolean isActive() {
		return this.active;
	}

	private void handleKeyReleased(KeyReleasedEvent event) {
		switch (event.getKeyCode()) {
		case KeyCode.KEY_LSHIFT:
		case KeyCode.KEY_RSHIFT:
			this.shift = false;
			break;
		}
	}

	private void handleKeyPressed(KeyPressedEvent event) {
		if (!isActive()) {
			return;
		}
		
//		System.out.println("KC: " + event.getKeyCode());
		
		if (KeyCode.isPrintable(event.getKeyCode())) {
			char ch = KeyCode.getChar(event.getKeyCode(), this.shift);
			this.cml.addChar(ch);
			this.command.append(ch);
			return;
		}
		
		switch (event.getKeyCode()) {
		case KeyCode.KEY_BACK:
			if (this.cml.curLineLengh() > PROMPT.length()) {
				this.cml.deleteLastChar();
				this.command.deleteCharAt(this.command.length() - 1);
			}
			break;
			
		case KeyCode.KEY_LSHIFT:
		case KeyCode.KEY_RSHIFT:
			this.shift = true;
			break;
			
		case KeyCode.KEY_RETURN:
			makeNewLine();
			interprete(this.command.toString().trim());
			this.command.setLength(0);
			break;
		}
	}
	
	private void makeNewLine() {
		this.cml.newLine();
		this.cml.addString(PROMPT);
	}
	
	private void interprete(String cmd) {
		if (cmd.equals("slow")) {
			TimeService.getInstance(this.core).getTimer().setScale(0.5);
			this.cml.addString("speed set to 0.5");
			makeNewLine();
		} else if (cmd.equals("help")) {
			this.cml.addString("commands: version, slow, normal, fast, info, log, newstage, newship, powerup, resume, pause, listtasks, listservices, pauseservice [idx] resumeservice [idx], getproperty [key], setproperty [key] [value]");
			makeNewLine();
		} else if (cmd.equals("normal")) {
			TimeService.getInstance(this.core).getTimer().setScale(1.0);			
			this.cml.addString("speed set to 1.0");
			makeNewLine();
		} else if (cmd.equals("fast")) {
			TimeService.getInstance(this.core).getTimer().setScale(1.5);			
			this.cml.addString("speed set to 1.5");
			makeNewLine();
		} else if (cmd.equals("info")) {
			EntityService entSrv = EntityService.getInstance(this.core);
			this.cml.addString("active entities: " + entSrv.numEntities());
			makeNewLine();
		} else if (cmd.equals("resume")) {
			TimeService.getInstance(this.core).getTimer().setPaused(false);
			this.cml.addString("game resumed");
			makeNewLine();			
		} else if (cmd.equals("pause")) {
			TimeService.getInstance(this.core).getTimer().setPaused(true);
			this.cml.addString("game paused");
			makeNewLine();			
		} else if (cmd.equals("listtasks")) {
			TaskService taskSrv = TaskService.getInstance(this.core);
			StringBuffer str = new StringBuffer();
			for (int i = 0; i < taskSrv.numOfTasks(); ++i) {
				str.append("(" + i + ") " + taskSrv.getTask(i).getName());
				if (i < taskSrv.numOfTasks() - 1) {
					str.append(", ");
				}
			}
			this.cml.addString(str.toString());
			makeNewLine();			
		} else if (cmd.startsWith("setproperty")) {
			if (cmd.length() <= "setproperty".length()) {
				this.cml.addString("missing property key");
				makeNewLine();		
				return;
			}
			String params = cmd.substring("setproperty".length() + 1);
			StringBuffer key = new StringBuffer();
			int i = 0;
			while (i < params.length() && params.charAt(i) != ' ') {
				key.append(params.charAt(i++));
			}
			if (params.length() <= i) {
				this.cml.addString("missing property value");
				makeNewLine();		
				return;
			}
			String value = params.substring(i + 1);
			PropertyService.getInstance(this.core).setProperty(key.toString(), value);
			this.cml.addString("property " + key + " set to " + value);
			makeNewLine();
		} else if (cmd.startsWith("getproperty")) {
			if (cmd.length() <= "getproperty".length()) {
				this.cml.addString("missing property key");
				makeNewLine();		
				return;
			}
			String params = cmd.substring("getproperty".length() + 1);
			StringBuffer key = new StringBuffer();
			int i = 0;
			while (i < params.length() && params.charAt(i) != ' ') {
				key.append(params.charAt(i++));
			}
			this.cml.addString("property " + key + " is  " + PropertyService.getInstance(this.core).getProperty(key.toString()));
			makeNewLine();
		} else if (cmd.equals("log")) {
			if (!logging) {
				LoggingService.getInstance(this.core).addListener(this);
				this.cml.addString("started printing log messages");
				makeNewLine();
				this.logging = true;
			} else {
				LoggingService.getInstance(this.core).removeListener(this);				
				this.cml.addString("stoppped printing log messages");
				makeNewLine();
				this.logging = false;
			}
		} else if (cmd.equals("newstage")) {
			EventService.getInstance(this.core).dispatchEvent(new SimpleEvent(GameLogic.NEW_STAGE));
			this.cml.addString("new stage command sent");
			makeNewLine();
		} else if (cmd.equals("newship")) {
			EventService.getInstance(this.core).dispatchEvent(new SimpleEvent(GameLogic.NEW_SHIP));
			this.cml.addString("new ship command sent");
			makeNewLine();
		} else if (cmd.equals("powerup")) {
			EventService.getInstance(this.core).dispatchEvent(new SimpleEvent(GameLogic.POWER_UP));
			this.cml.addString("power up command sent");
			makeNewLine();
		} else if (cmd.equals("listservices")) {
			StringBuffer str = new StringBuffer();
			for (int i = 0; i < this.core.numServices(); ++i) {
				str.append("(" + i + ") ");
				str.append(this.core.getService(i).getName());
				if (i < this.core.numServices() - 1) {
					str.append(", ");
				}
			}
			this.cml.addString(str.toString());
			makeNewLine();
		} else if (cmd.startsWith("pauseservice")) {
			int idx = Integer.parseInt(cmd.substring("pauseservice".length() + 1));
			if (idx >= core.numServices()) {
				this.cml.addString("illegal service number: " + idx);
				makeNewLine();					
			} else {
				this.core.getService(idx).pause();
				this.cml.addString("service " + this.core.getService(idx).getName() + " paused");
				makeNewLine();
			}
		} else if (cmd.startsWith("resumeservice")) {
			int idx = Integer.parseInt(cmd.substring("resumeservice".length() + 1));
			if (idx >= core.numServices()) {
				this.cml.addString("illegal service number: " + idx);
				makeNewLine();					
			} else {
				this.core.getService(idx).resume();
				this.cml.addString("service " + this.core.getService(idx).getName() + " resumed");
				makeNewLine();
			}
		} else if (cmd.equals("version")) {
			PropertyService propSrv = PropertyService.getInstance(this.core);
			this.cml.addString(propSrv.getProperty(SpaceSweeper.APP_TITLE_PROP) + " Version " + SpaceSweeper.VERSION);
			makeNewLine();
		} else {
			this.cml.addString("unknown command '" + cmd + "'");
			makeNewLine();			
		}
	}

	@Override
	public void handleLogMessage(Priority priority, String source, String message) {
		this.cml.clearLine();
		this.cml.addString(PROMPT + "[" + source + "]: " + message);
		makeNewLine();
	}

}
