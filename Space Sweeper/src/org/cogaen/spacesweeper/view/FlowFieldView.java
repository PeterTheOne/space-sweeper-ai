package org.cogaen.spacesweeper.view;

import javax.swing.text.FlowView;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.LineVisual;
import org.cogaen.spacesweeper.event.FlowFieldEngagedEvent;
import org.cogaen.spacesweeper.event.FlowFieldUpdatedEvent;
import org.cogaen.spacesweeper.representation.FlowLineRepresentation;
import org.cogaen.spacesweeper.state.FlowField;
import org.cogaen.spacesweeper.state.PlayState;
import org.cogaen.spacesweeper.util.Logic;

public class FlowFieldView extends Logic implements EventListener {
	
	private PlayView playView;
	
	private SceneService scnSrv;
	private LoggingService log;
	private EventService evntServ;

	public FlowFieldView(Core core, PlayView playView) {
		super(core);
		this.playView = playView;
	}
	
	@Override
	public void engage() {
		this.scnSrv = SceneService.getInstance(getCore());
		this.log = LoggingService.getInstance(getCore());
		this.evntServ = EventService.getInstance(getCore());
		
		this.evntServ.addListener(this, FlowFieldEngagedEvent.TYPE_ID);
		this.evntServ.addListener(this, FlowField.FF_DISENGAGED);
		this.evntServ.addListener(this, FlowFieldUpdatedEvent.TYPE_ID);

		super.engage();
	}

	@Override
	public void disengage() {
		super.disengage();
		this.evntServ.removeListener(this);
	}

	@Override
	public void handleEvent(Event event) {
		this.log.logInfo("FlowFieldView", "handleEvent");
		if (event.isOfType(FlowFieldEngagedEvent.TYPE_ID)) {
			handleFFEngagedEvent((FlowFieldEngagedEvent) event);
		} else if (event.isOfType(FlowField.FF_DISENGAGED)) {
			handleFFDisengagedEvent();
		} else if (event.isOfType(FlowFieldUpdatedEvent.TYPE_ID)) {
			handleFFUpdatedEvent((FlowFieldUpdatedEvent) event);
		}
	}

	private void handleFFEngagedEvent(FlowFieldEngagedEvent event) {
		double left = - event.getHorizontalCount() / 2d;
		double top = - event.getVerticalCount() / 2d;
		for (int x = 0; x < event.getHorizontalCount(); x++) {
			for (int y = 0; y < event.getVerticalCount(); y++) {
				CogaenId fieldLineId = new CogaenId("FlowLine: x: " + x + ", y: " + y);
				FlowLineRepresentation flr = new FlowLineRepresentation(getCore(), fieldLineId, 0, 0f, 0f);
				playView.addRepresentation(fieldLineId, flr);
				flr.setPose(x + left, y + top , 0);
			}
		}
	}

	private void handleFFDisengagedEvent() {
		
	}

	private void handleFFUpdatedEvent(FlowFieldUpdatedEvent event) {
		double[][][] field = event.getField();
		for (int x = 0; x < field.length; x++) {
			for (int y = 0; y < field[0].length; y++) {
				CogaenId fieldLineId = new CogaenId("FlowLine: x: " + x + ", y: " + y);
				FlowLineRepresentation flr = (FlowLineRepresentation) 
						playView.getRepresentation(fieldLineId);
				flr.setVector(field[x][y][0], field[x][y][1]);
			}
		}
	}

}
