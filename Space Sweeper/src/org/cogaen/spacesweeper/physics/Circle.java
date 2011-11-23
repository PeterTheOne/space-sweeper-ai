package org.cogaen.spacesweeper.physics;

import org.cogaen.name.CogaenId;

public class Circle extends Body {
	private double radius;
	
	public Circle(CogaenId id, double radius) {
		super(id);
		this.radius = radius;
	}
	
	public double getRadius() {
		return this.radius;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}

	@Override
	public boolean isColliding(Body body) {
		return body.isColliding(this);
	}

	@Override
	public boolean isColliding(Circle circle) {
		return CollisionTester.isColliding(this, circle);
	}

	@Override
	public boolean isColliding(Rectangle rectangle) {
		return CollisionTester.isColliding(this, rectangle);
	}
}
