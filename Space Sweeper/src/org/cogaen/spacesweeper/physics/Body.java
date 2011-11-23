package org.cogaen.spacesweeper.physics;

import org.cogaen.name.CogaenId;

public abstract class Body {
	
	private double[] position = new double[3];
	private double[] velocity = new double[3];
	private double[] acceleration = new double[3];
	private double[] damping = new double[3];
	private int collisionFlag = 0x0001;
	private int collisionMask = 0xFFFF;
	private CogaenId id;
			
	public Body(CogaenId id) {
		this.id = id;
	}
	
	public void setCollisionFlag(int flag) {
		this.collisionFlag = flag;
	}
	
	public int getCollisionFlag() {
		return this.collisionFlag;
	}

	public void setCollisionMask(int mask) {
		this.collisionMask = mask;
	}
	
	public int getCollisionMask() {
		return this.collisionMask;
	}
	
	public void setLinearDamping(double damping) {
		this.damping[0] = this.damping[1] = -Math.abs(damping);
	}
	
	public void setAngularDamping(double damping) {
		this.damping[2] = -Math.abs(damping);
	}

	public double getLinearDamping() {
		return this.damping[0];
	}
	
	public double getAngularDamping() {
		return this.damping[2];
	}
	
	public void setPosition(double x, double y) {
		this.position[0] = x;
		this.position[1] = y;
	}
	
	public void setAngularPosition(double phi) {
		this.position[2] = phi;
	}
	
	public double getAngularPosition() {
		return this.position[2];
	}
	
	public double getPositionX() {
		return this.position[0];
	}
	
	public double getPositionY() {
		return this.position[1];
	}

	public void setVelocity(double vx, double vy) {
		this.velocity[0] = vx;
		this.velocity[1] = vy;
	}
	
	public void setAngularVelocity(double omega) {
		this.velocity[2] = omega;
	}
	
	public void setSpeed(double speed) {
		setVelocity(-speed * Math.sin(getAngularPosition()), speed * Math.cos(getAngularPosition()));		
	}
	
	public double getSpeed() {
		return Math.sqrt(getSpeedSquared());
	}
	
	public double getSpeedSquared() {
		return this.velocity[0] * this.velocity[0] + this.velocity[1] * this.velocity[1];
	}
	
	public double getAngularVelocity() {
		return this.velocity[2];
	}
	
	public double getVelocityX() {
		return this.velocity[0];
	}
	
	public double getVelocityY() {
		return this.velocity[1];
	}
	
	public void setAcceleration(double ax, double ay) {
		this.acceleration[0] = ax;
		this.acceleration[1] = ay;
	}
	
	public void setAcceleration(double acceleration) {
		setAcceleration(-acceleration * Math.sin(getAngularPosition()), acceleration * Math.cos(getAngularPosition()));		
	}
	
	public double getAccelerationSquared() {
		return this.acceleration[0] * this.acceleration[0] + this.acceleration[1] * this.acceleration[1];		
	}
	
	public double getAcceleration() {
		return Math.sqrt(getAccelerationSquared());
	}
	
	public void setAngularAcceleration(double alpha) {
		this.acceleration[2] = alpha;
	}
	
	public double getAngularAcceleration() {
		return this.acceleration[2];
	}
	
	public double getAccelerationX() {
		return this.acceleration[0];
	}
	
	public double getAccelerationY() {
		return this.acceleration[1];
	}
			
	public CogaenId getId() {
		return this.id;
	}
				
	void update(double dt) {
						
		for (int i = 0; i < position.length; ++i) {
			this.velocity[i] += (this.acceleration[i] + getDamping(i)) * dt;
			this.position[i] += this.velocity[i] * dt;
		}
	}
	
	private double getDamping(int i) {
		return this.velocity[i] * this.damping[i];
	}

	public boolean isCollisionAllowed(Body body) {
		return (this.collisionMask & body.collisionFlag) != 0 && (this.collisionFlag & body.collisionMask) != 0;
	}

	public abstract boolean isColliding(Body body);
	public abstract boolean isColliding(Circle circle);
	public abstract boolean isColliding(Rectangle rectangle);
}


