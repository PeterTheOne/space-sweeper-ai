package org.cogaen.spacesweeper.state;

import org.cogaen.core.Core;
import org.cogaen.core.Engageable;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.event.FlowFieldEngagedEvent;
import org.cogaen.spacesweeper.event.FlowFieldUpdatedEvent;
import org.cogaen.spacesweeper.physics.PositionUpdateEvent;

public class FlowField implements Engageable, EventListener {

	public static final CogaenId FF_DISENGAGED = new CogaenId("FF Disengaged");
	
	private Core core;
	private EventService evntSrv;
	private LoggingService logSrv;
	
	private boolean engaged;
	private double worldWidth;
	private double worldHeight;
	
	private double[][][] field;

	public FlowField(Core core) {
		this.engaged = false;
		this.core = core;
	}

	@Override
	public void engage() {
		this.evntSrv = EventService.getInstance(this.core);
		this.logSrv = LoggingService.getInstance(this.core);
		
		this.evntSrv.addListener(this, PositionUpdateEvent.TYPE_ID);
		
		this.worldWidth = PlayState.DEFAULT_WORLD_WIDTH;
		double ar = SceneService.getInstance(this.core).getAspectRatio();
		this.worldHeight = worldWidth / ar;
		
		this.field = new double[(int) worldWidth][(int) worldHeight][2];
		for (int i = 0; i < (int) worldWidth; i++) {
			for (int j = 0; j < (int) worldHeight; j++) {
				this.field[i][j][0] = 0.5;
				this.field[i][j][1] = 0.5;
			}
		}

		this.engaged = true;
		Event event = new FlowFieldEngagedEvent(field.length, field[0].length);
		this.evntSrv.dispatchEvent(event);
	}
	
	@Override
	public void disengage() {
		this.evntSrv.dispatchEvent(new SimpleEvent(FF_DISENGAGED));
		this.engaged = false;
		

	}

	@Override
	public boolean isEngaged() {
		return this.engaged;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(PositionUpdateEvent.TYPE_ID)) {
			handlePositionUpdateEvent();
		}
	}

	private void handlePositionUpdateEvent() {
		this.evntSrv.dispatchEvent(new FlowFieldUpdatedEvent(this.field));
	}

}
