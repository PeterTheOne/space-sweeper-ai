package org.cogaen.spacesweeper.physics;

public class CollisionTester {
	public static boolean isColliding(Circle c1, Circle c2) {
		double dx = c1.getPositionX() - c2.getPositionX();
		double dy = c1.getPositionY() - c2.getPositionY();
		double radii = c1.getRadius() + c2.getRadius();
		
		return dx * dx + dy * dy < radii * radii;
	}
	
	public static boolean isColliding(Rectangle r1, Rectangle r2) {
		throw new UnsupportedOperationException("not implemented yet");
	}
	
	public static boolean isColliding(Circle c, Rectangle r) {
		throw new UnsupportedOperationException("not implemented yet");
	}
}
