package org.cogaen.spacesweeper.view;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.lwjgl.scene.Alignment;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.FontHandle;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.TextVisual;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.resource.ResourceService;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.spacesweeper.event.GroupLoadUpdateEvent;
import org.cogaen.spacesweeper.hud.BarHud;
import org.cogaen.view.View;

public class LoadingView extends View implements EventListener {

	private BarHud progress;
	private TextVisual percentageText;
	
	public LoadingView(Core core) {
		super(core);
		this.progress = new BarHud(core, 0.75, 0.025);
	}

	@Override
	public void registerResources(CogaenId groupId) {
		super.registerResources(groupId);
		
		ResourceService resSrv = ResourceService.getInstance(getCore());
		if (System.getProperty("os.name").startsWith("Mac")) {
			resSrv.declareResource("LoadingFnt", groupId, new FontHandle("D3-Euronism", FontHandle.PLAIN, 25));		
		} else {
			resSrv.declareResource("LoadingFnt", groupId, new FontHandle("D3 Euronism", FontHandle.PLAIN, 25));					
		}
	}

	@Override
	public void engage() {
		super.engage();
		EventService.getInstance(getCore()).addListener(this, GroupLoadUpdateEvent.TYPE_ID);
		
		SceneService scnSrv = SceneService.getInstance(getCore());
		this.progress.engage();
		this.progress.setBarColor(new Color(242.0 / 255.0, 61.0 / 255.0, 32.0 / 255.0));
		this.progress.setBackColor(new Color(95.0 / 255.0, 86.0 / 255.0, 107.0 / 255.0));
		this.progress.setFrameColor(Color.WHITE);
		SceneNode node = this.progress.getBaseNode();
		node.setPose(0.5, 0.35 / scnSrv.getAspectRatio(), 0);
		scnSrv.getOverlayRoot().addNode(node);

		double referenceResolution = PropertyService.getInstance(getCore()).getDoubleProperty(SpaceSweeper.REFERENCE_RESOLUTION_PROP);
		node = scnSrv.createNode();
		node.setPose(0.5, 0.45 / scnSrv.getAspectRatio(), 0);
		TextVisual text = new TextVisual(getCore(), "LoadingFnt");
		text.setColor(Color.WHITE);
		text.setAllignment(Alignment.CENTER);
		text.setScale(1.0 / referenceResolution);
		text.setText("Loading, please wait...");
		node.addVisual(text);
		node.setPose(0.5, 0.5 / scnSrv.getAspectRatio(), 0);
		
		scnSrv.getOverlayRoot().addNode(node);
		
		node = scnSrv.createNode();
		node.setPose(0.47, 0.35 / scnSrv.getAspectRatio(), 0.0);
		this.percentageText = new TextVisual(getCore(), "LoadingFnt");
		this.percentageText.setColor(Color.WHITE);
		this.percentageText.setScale(1.0 / referenceResolution);
		node.addVisual(this.percentageText);
		
		scnSrv.getOverlayRoot().addNode(node);
	}

	@Override
	public void disengage() {
		EventService.getInstance(getCore()).removeListener(this);
		SceneService.getInstance(getCore()).destroyAll();
		super.disengage();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(GroupLoadUpdateEvent.TYPE_ID)) {
			handleGroupLoadUpdate((GroupLoadUpdateEvent) event);
		}
	}

	private void handleGroupLoadUpdate(GroupLoadUpdateEvent event) {
		this.progress.setPercentage(event.getPercentage());
		this.percentageText.setText(String.format("%3.0f%%", event.getPercentage() * 100));
	}

}
