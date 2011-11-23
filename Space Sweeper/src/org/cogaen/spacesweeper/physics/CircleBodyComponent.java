package org.cogaen.spacesweeper.physics;

import org.cogaen.entity.Component;
import org.cogaen.entity.ComponentEntity;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.entity.Pose2D;

public class CircleBodyComponent extends Component implements Pose2D {
	
	private Circle circle;
	private double radius;
	private CogaenId bodyAttrId;
	
	public CircleBodyComponent(CogaenId bodyAttrId, double radius) {
		this.radius = radius;
		this.bodyAttrId = bodyAttrId;
	}
	
	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
		this.circle = new Circle(parent.getId(), this.radius);
		
		parent.addAttribute(Pose2D.ATTR_ID, this);
		parent.addAttribute(this.bodyAttrId, this.circle);
	}

	@Override
	public void engage() {
		super.engage();
		PhysicsService.getInstance(getCore()).addBody(this.circle);
	}

	@Override
	public void disengage() {
		PhysicsService.getInstance(getCore()).removeBody(this.circle);
		super.disengage();
	}

	@Override
	public double getPosX() {
		return this.circle.getPositionX();
	}

	@Override
	public double getPosY() {
		return this.circle.getPositionY();
	}

	@Override
	public double getAngle() {
		return this.circle.getAngularPosition();
	}

	@Override
	public void setPosition(double x, double y) {
		this.circle.setPosition(x, y);
	}

	@Override
	public void setAngle(double phi) {
		this.circle.setAngularPosition(phi);
	}

}
