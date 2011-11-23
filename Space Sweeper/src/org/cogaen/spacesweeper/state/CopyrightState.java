package org.cogaen.spacesweeper.state;

import org.cogaen.core.Core;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.view.CopyrightView;
import org.cogaen.state.BasicState;

public class CopyrightState extends BasicState {

	public static final CogaenId ID = new CogaenId("Copyright");
	public static final CogaenId END_OF_COPYRIGHT = new CogaenId("EndOfCopyright");
	private static final double DISPLAY_TIME = 7.0;

	private CopyrightView view;
	
	public CopyrightState(Core core) {
		super(core);
		this.view = new CopyrightView(core);

		ResourceService.getInstance(core).createGroup(ID);
		this.view.registerResources(ID);
	}
	
	@Override
	public void onEnter() {
		ResourceService.getInstance(getCore()).loadGroup(ID);
		view.engage();
		EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(END_OF_COPYRIGHT), DISPLAY_TIME);
	}

	@Override
	public void onExit() {
		view.disengage();
		ResourceService.getInstance(getCore()).unloadGroup(ID);
	}

}
