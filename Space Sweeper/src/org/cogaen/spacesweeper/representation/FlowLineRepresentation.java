package org.cogaen.spacesweeper.representation;

import org.cogaen.core.Core;
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.scene.Visual;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.LineVisual;

public class FlowLineRepresentation extends BaseRepresentation {
	
	private double dx;
	private double dy;

	public FlowLineRepresentation(Core core, CogaenId entityId, double dx, 
			double dy) {
		super(core, entityId);
		this.dx = dx;
		this.dy = dy;
	}
	
	public FlowLineRepresentation(Core core, CogaenId entityId, int layer, 
			double dx, double dy) {
		super(core, entityId, layer);
		this.dx = dx;
		this.dy = dy;
	}
	
	@Override
	public void engage() {
		super.engage();		
		setVisual();
	}

	private void setVisual() {
		getNode().removeAllVisuals();
		Visual visual = (Visual) new LineVisual((float) this.dx, (float) this.dy, 0.2f);
		getNode().addVisual(visual);
	}

	@Override
	public void disengage() {
		
		super.disengage();
	}
	
	public void setVector(double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
		setVisual();
	}

}
