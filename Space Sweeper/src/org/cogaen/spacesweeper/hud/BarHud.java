package org.cogaen.spacesweeper.hud;

import org.cogaen.core.Core;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.ReadableColor;
import org.cogaen.lwjgl.scene.RectangleVisual;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.view.AbstractHud;

public class BarHud extends AbstractHud {

	private double width;
	private double height;
	private SceneNode baseNode;
	private RectangleVisual bar;
	private RectangleVisual frame;
	private RectangleVisual back;
	private SceneNode barNode;
	
	public BarHud(Core core, double width, double height) {
		super(core);
		this.width = width;
		this.height = height;
	}

	public void setBarColor(ReadableColor color) {
		this.bar.setColor(color);
	}

	public void setFrameColor(ReadableColor color) {
		this.frame.setColor(color);
	}

	public void setBackColor(ReadableColor color) {
		this.back.setColor(color);
	}
	
	public void setPercentage(double p) {
		this.bar.setWidth(this.width * p);
		this.barNode.setPose(-(1.0 - p) * this.width / 2, 0, 0);
	}
	
	public SceneNode getBaseNode() {
		return this.baseNode;
	}
	
	@Override
	public void engage() {
		super.engage();
		SceneService scnSrv = SceneService.getInstance(getCore());

		this.baseNode = scnSrv.createNode();
		
		this.back = new RectangleVisual(this.width, this.height);
		this.back.setColor(Color.BLUE);
		this.baseNode.addVisual(this.back);
		
		this.barNode = scnSrv.createNode();
		this.bar = new RectangleVisual(this.width, this.height);
		this.barNode.addVisual(this.bar);
		this.baseNode.addNode(this.barNode);

		SceneNode node = scnSrv.createNode();
		this.frame = new RectangleVisual(this.width, this.height);
		this.frame.setFilled(false);
		this.frame.setColor(Color.WHITE);
		node.addVisual(this.frame);
		this.baseNode.addNode(node);
	}

	@Override
	public void disengage() {
		SceneService scnSrv = SceneService.getInstance(getCore());
		scnSrv.destroyNode(this.baseNode);
		super.disengage();
	}
}
