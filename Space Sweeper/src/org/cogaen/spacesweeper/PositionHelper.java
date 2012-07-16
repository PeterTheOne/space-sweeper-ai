package org.cogaen.spacesweeper;

public class PositionHelper {
	
	private double worldWidth;
	private double worldHeight;
	private double shortTargetX;
	private double shortTargetY;
	
	public PositionHelper(double worldWidth, double worldHeight) {
		this.worldWidth = worldWidth;
		this.worldHeight = worldHeight;
		this.shortTargetX = 0;
		this.shortTargetY = 0;
	}
	
	/**
	 * Sets the target for the shortest way to the target position
	 * in an infinite repeating world.
	 * 
	 * First it calculates if start and target are in different 
	 * quadrants and in witch ones. If so, an alternative route 
	 * is measured and compared with the default target distance. 
	 * This is done for both axis.
	 * 
	 * @param startX
	 * @param startY
	 * @param targetX
	 * @param targetY
	 */
	public void setTarget(double startX, double startY, double targetX, 
			double targetY) {
		this.shortTargetX = targetX;
		double distX = Math.abs(targetX - startX);
		if (startX > 0 && targetX < 0) {
			double altTargetPosX = targetX + this.worldWidth;
			double altDist = Math.abs(altTargetPosX - startX);
			if (altDist < distX) {
				this.shortTargetX = altTargetPosX;
			}
		} else if (startX < 0 && targetX > 0) {
			double altTargetPosX = targetX - this.worldWidth;
			double altDist = Math.abs(altTargetPosX - startX);
			if (altDist < distX) {
				this.shortTargetX = altTargetPosX;
			}
		}
		
		this.shortTargetY = targetY;
		double distY = Math.abs(targetY - startY);
		if (startY > 0 && targetY < 0) {
			double altTargetPosY = targetY + this.worldHeight;
			double altDist = Math.abs(altTargetPosY - startY);
			if (altDist < distY) {
				this.shortTargetY = altTargetPosY;
			}
		} else if (startY < 0 && targetY > 0) {
			double altTargetPosY = targetY - this.worldHeight;
			double altDist = Math.abs(altTargetPosY - startY);
			if (altDist < distY) {
				this.shortTargetY = altTargetPosY;
			}
		}
	}
	
	public double getTargetX() {
		return this.shortTargetX;
	}
	
	public double getTargetY() {
		return this.shortTargetY;
	}
	
}
