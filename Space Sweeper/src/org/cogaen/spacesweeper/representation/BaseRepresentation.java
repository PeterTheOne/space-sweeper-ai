package org.cogaen.spacesweeper.representation;

import org.cogaen.core.Core;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.name.CogaenId;
import org.cogaen.view.EntityRepresentation;

public class BaseRepresentation extends EntityRepresentation {

	private SceneNode node;
	private SceneService scnSrv;
	private int layer;
	
	public BaseRepresentation(Core core, CogaenId entityId) {
		this(core, entityId, 0);
	}

	public BaseRepresentation(Core core, CogaenId entityId, int layer) {
		super(core, entityId);
		this.scnSrv = SceneService.getInstance(core);
		this.layer = layer;
	}
	
	@Override
	public void engage() {
		super.engage();
		this.node = this.scnSrv.createNode();
		this.scnSrv.getLayer(this.layer).addNode(this.node);
	}

	@Override
	public void disengage() {
		this.scnSrv.destroyNode(this.node);
		this.node = null;
		super.disengage();
	}
	
	public void updatePosition(double x, double y, double angle) {
		this.node.setPose(x, y, angle);
	}
		
	public SceneNode getNode() {
		return this.node;
	}
	
	public void setPose(double x, double y, double angle) {
		this.node.setPose(x, y, angle);
	}
	
	public SceneService getSceneService() {
		return this.scnSrv;
	}
	
	public void destroy() {
		// intentionally left empty
	}
	
	public void setMark(boolean value) {
		// intentionally left empty
	}
}
