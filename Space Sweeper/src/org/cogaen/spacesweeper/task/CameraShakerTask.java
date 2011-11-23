package org.cogaen.spacesweeper.task;

import org.cogaen.core.Core;
import org.cogaen.lwjgl.scene.Camera;
import org.cogaen.task.AbstractTask;
import org.cogaen.task.TaskService;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class CameraShakerTask extends AbstractTask {

	private static final double SHAKE_TIME = 1.0;
	private static final double SPEED = Math.PI * 16;
	private static final double START_SCALE = 0.25;
	private Camera camera;
	private Timer timer;
	private double timeStamp;
	private double origPosX;
	private double origPosY;
	private double value;
	private double scale;
	
	public CameraShakerTask(Core core, Camera camera) {
		super(core, "Camera Shaker");		
		this.camera = camera;
		this.timer = TimeService.getInstance(core).getTimer();
		this.timeStamp = this.timer.getTime() + SHAKE_TIME;
		
		this.origPosX = this.camera.getPosX();
		this.origPosY = this.camera.getPosY();
		this.value = 0;
		this.scale = START_SCALE;
	}

	@Override
	public void update() {
		if (this.timeStamp < this.timer.getTime()) {
			TaskService.getInstance(getCore()).destroyTask(this);
			this.camera.setPosition(this.origPosX, this.origPosY);
			return;
		}

		this.value += SPEED * this.timer.getDeltaTime();
		double x = this.origPosX + Math.sin(this.value) * this.scale;
		this.camera.setPosition(x, this.origPosY);
		this.scale = START_SCALE * (this.timeStamp - this.timer.getTime()) / SHAKE_TIME;
	}

	@Override
	public void destroy() {
		// intentionally left empty
	}

}
