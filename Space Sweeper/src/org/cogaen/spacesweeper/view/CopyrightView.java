package org.cogaen.spacesweeper.view;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.lwjgl.input.KeyPressedEvent;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.SpriteHandle;
import org.cogaen.lwjgl.scene.TextureHandle;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.name.CogaenId;
import org.cogaen.resource.ResourceService;
import org.cogaen.resource.TextHandle;
import org.cogaen.spacesweeper.hud.InfoHud;
import org.cogaen.spacesweeper.state.CopyrightState;
import org.cogaen.view.View;

public class CopyrightView extends View implements EventListener {

	private InfoHud infoHud;
	
	public CopyrightView(Core core) {
		super(core);
		this.infoHud = new InfoHud(core);
	}

	@Override
	public void registerResources(CogaenId groupId) {
		ResourceService resSrv = ResourceService.getInstance(getCore());
		resSrv.declareResource("CopyrightBackgroundTex", groupId, new TextureHandle("JPG", "images/space_1024x768.jpg", TextureHandle.NEAREST_FILTER));
		resSrv.declareResource("CopyrightBackgroundSpr", groupId, new SpriteHandle("BackgroundTex", 1.0, 1.0 / SceneService.getInstance(getCore()).getAspectRatio()));
		resSrv.declareResource("CopyrightTxt", groupId, new TextHandle("text/license.txt"));
		
		this.infoHud.registerResources(groupId);
	}

	@Override
	public void engage() {
		super.engage();
		EventService.getInstance(getCore()).addListener(this, KeyPressedEvent.TYPE_ID);
		
		SceneService scnSrv = SceneService.getInstance(getCore());
		SceneNode node = scnSrv.createNode();
		Visual visual = (Visual) ResourceService.getInstance(getCore()).getResource("CopyrightBackgroundSpr");
		node.addVisual(visual);		
		node.setPose(0.5, 0.5 / scnSrv.getAspectRatio(), 0);
		scnSrv.getOverlayRoot().addNode(node);
		
		this.infoHud.engage();
		this.infoHud.setCenterAllignment(true);
		this.infoHud.setFading(false);
		this.infoHud.setText((String) ResourceService.getInstance(getCore()).getResource("CopyrightTxt"));
		this.infoHud.setBackColor(new Color(95.0 / 255.0, 86.0 / 255.0, 107.0 / 255.0, 0.5));
		this.infoHud.setFrameColor(Color.WHITE);		
		this.infoHud.setText((String) ResourceService.getInstance(getCore()).getResource("CopyrightTxt"));
	}

	@Override
	public void disengage() {
		EventService.getInstance(getCore()).removeListener(this);
		this.infoHud.disengage();
		SceneService.getInstance(getCore()).destroyAll();
		super.disengage();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(KeyPressedEvent.TYPE_ID)) {
			EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(CopyrightState.END_OF_COPYRIGHT));
		}
	}
}
