package org.cogaen.spacesweeper.util;

public class PidController {

	private double y;
	private double cp = 1;
	private double ci = 0;
	private double cd = 0;
	private double eSum;
	private double lastError;
	private double target;
	
	
	public PidController(double cp, double ci, double cd) {
		this.cp = cp;
		this.ci = ci;
		this.cd = cd;
		this.eSum = 0;
		this.lastError = 0;
	}

	public void update(double input, double dt) {
		double e = this.target - input;
		this.eSum += e;
		this.y = cp * e + ci * eSum + this.cd * (e - this.lastError) / dt;
		this.lastError = e;
	}
	
	public double getOutput() {
		return this.y;
	}	
	
	public void setTarget(double target) {
		this.target = target;
	}
}
