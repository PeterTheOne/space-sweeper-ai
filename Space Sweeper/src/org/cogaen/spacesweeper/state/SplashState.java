package org.cogaen.spacesweeper.state;

import org.cogaen.core.Core;
import org.cogaen.event.EventService;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.view.SplashView;
import org.cogaen.state.BasicState;
import org.cogaen.view.View;

public class SplashState extends BasicState {
	
	public static final CogaenId ID = new CogaenId("Splash");
	public static final CogaenId END_OF_SPLASH = new CogaenId("EndOfSplash");
	
	private View view;
	
	public SplashState(Core core) {
		super(core);
		EventService.getInstance(core);
		this.view = new SplashView(core);

		ResourceService resSrv = ResourceService.getInstance(core);
		resSrv.createGroup(ID);
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
