package org.cogaen.spacesweeper.state;

import org.cogaen.core.Core;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.view.MenuView;
import org.cogaen.state.BasicState;

public class MenuState extends BasicState {

	public static final CogaenId ID = new CogaenId("Menu");
	public static final CogaenId END_OF_MENU = new CogaenId("EndOfMenu");

	private MenuView view;
	
	public MenuState(Core core) {
		super(core);
		this.view = new MenuView(core);
		
		ResourceService.getInstance(getCore()).createGroup(ID);
		this.view.registerResources(ID);
	}
	
	@Override
	public void onEnter() {
		ResourceService.getInstance(getCore()).loadGroup(ID);
		this.view.engage();
	}

	@Override
	public void onExit() {
		this.view.disengage();
		ResourceService.getInstance(getCore()).unloadGroup(ID);
	}

}
