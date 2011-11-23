package org.cogaen.spacesweeper.state;

import org.cogaen.core.Core;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.view.InfoView;
import org.cogaen.state.BasicState;

public class InfoState extends BasicState {

	public static final CogaenId ID = new CogaenId("Info");
	public static final CogaenId END_OF_INFO = new CogaenId("EndOfInfo");

	private InfoView view;
	
	public InfoState(Core core) {
		super(core);
		this.view = new InfoView(core);

		ResourceService.getInstance(core).createGroup(ID);
		this.view.registerResources(ID);
	}
	
	@Override
	public void onEnter() {
		ResourceService.getInstance(getCore()).loadGroup(ID);
		view.engage();
	}

	@Override
	public void onExit() {
		view.disengage();
		ResourceService.getInstance(getCore()).unloadGroup(ID);
	}

}
