package org.cogaen.spacesweeper.task;

import org.cogaen.core.Core;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.math.EaseInOut;
import org.cogaen.task.AbstractTask;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class MoveTask extends AbstractTask {

	private SceneNode node;
	private EaseInOut easeX = new EaseInOut(0, 0.25);
	private EaseInOut easeY = new EaseInOut(0, 0.25);
	private Timer timer;
	
	public MoveTask(Core core, String name, SceneNode node) {
		super(core, name);
		this.node = node;
		this.timer = TimeService.getInstance(getCore()).getTimer();
	}

	@Override
	public void update() {
		this.easeX.update(this.timer.getDeltaTime());
		this.easeY.update(this.timer.getDeltaTime());
		this.node.setPose(this.easeX.getCurrentValue(), this.easeY.getCurrentValue(), this.node.getAngle());
	}

	@Override
	public void destroy() {
		// intentionally left empty
	}
	
	public void setTargetPosition(double x, double y) {
		this.easeX.setTargetValue(x);
		this.easeY.setTargetValue(y);
	}

	public void setCurrentPosition(double x, double y) {
		this.easeX.reset(x);
		this.easeY.reset(y);
	}
	
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	public Timer getTimer() {
		return this.timer;
	}

}
