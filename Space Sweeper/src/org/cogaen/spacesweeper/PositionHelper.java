package org.cogaen.spacesweeper;

import org.cogaen.spacesweeper.entity.BigAsteroid;

public class PositionHelper {
	
	private double worldWidth;
	private double worldHeight;
	private double shortTargetX;
	private double shortTargetY;
	
	public PositionHelper(double worldWidth, double worldHeight) {
		//todo: fix this, found in PlayView
		this.worldWidth = worldWidth + BigAsteroid.RADIUS * 2;
		this.worldHeight = worldHeight + BigAsteroid.RADIUS * 2;
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
		double diffX = targetX - startX;
		double distX = Math.abs(diffX);
		if (distX > this.worldWidth / 2d) {
			if (diffX < 0) {
				this.shortTargetX = targetX + this.worldWidth;
			} else {
				this.shortTargetX = targetX - this.worldWidth;
			}
		}
		
		this.shortTargetY = targetY;
		double diffY = targetY - startY;
		double distY = Math.abs(diffY);
		if (distY > this.worldHeight / 2d) {
			if (diffY < 0) {
				this.shortTargetY = targetY + this.worldHeight;
			} else {
				this.shortTargetY = targetY - this.worldHeight;
			}
		}
	}
	
	public double getTargetX() {
		return this.shortTargetX;
	}
	
	public double getTargetY() {
		return this.shortTargetY;
	}
	
	public double calculateAngle(double bodyAngle, double bodyX, double bodyY, 
			double targetX, double targetY) {
		double dx = targetX - bodyX;
		double dy = targetY - bodyY;
		
		if (dx == 0 && dy == 0) {
			return 0;
		}
		
		double txr = dx * Math.cos(-bodyAngle) - dy * Math.sin(-bodyAngle);
		double tyr = dy * Math.cos(-bodyAngle) + dx * Math.sin(-bodyAngle);

		double l = Math.sqrt(txr * txr + tyr * tyr);
		txr /= l;
		return txr;
	}
	
}
