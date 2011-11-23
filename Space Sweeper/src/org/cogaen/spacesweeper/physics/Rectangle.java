package org.cogaen.spacesweeper.physics;

import org.cogaen.name.CogaenId;

public class Rectangle extends Body {

	private double width;
	private double height;

	public Rectangle(CogaenId id, double width, double height) {
		super(id);
		this.width = width;
		this.height = height;
	}
	
	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
	
	double getLeft() {
		return getPositionX() - this.width / 2.0;
	}
	
	double getRight() {
		return getPositionX() + this.width / 2.0;
	}
	
	double getTop() {
		return getPositionY() + this.height / 2.0;
	}
	
	double getBottom() {
		return getPositionY() - this.height / 2.0;
	}

	@Override
	public boolean isColliding(Body body) {
		return body.isColliding(this);
	}

	@Override
	public boolean isColliding(Circle circle) {
		return CollisionTester.isColliding(circle, this);
	}

	@Override
	public boolean isColliding(Rectangle rectangle) {
		return CollisionTester.isColliding(this, rectangle);
	}
}
