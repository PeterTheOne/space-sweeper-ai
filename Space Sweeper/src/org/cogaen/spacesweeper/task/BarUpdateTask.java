package org.cogaen.spacesweeper.task;

import org.cogaen.core.Core;
import org.cogaen.math.EaseInOut;
import org.cogaen.spacesweeper.hud.BarHud;
import org.cogaen.task.AbstractTask;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class BarUpdateTask extends AbstractTask {
	
	private BarHud bar;
	private EaseInOut percentage = new EaseInOut(1.0, 0.2);
	private Timer timer;
	
	public BarUpdateTask(Core core, BarHud bar) {
		super(core, "Bar Updater");
		this.bar = bar;
		this.timer = TimeService.getInstance(getCore()).getTimer();
	}

	public void reset(double p) {
		this.percentage.reset(p);
	}

	@Override
	public void update() {
		this.percentage.update(this.timer.getDeltaTime());
		this.bar.setPercentage(this.percentage.getCurrentValue());
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void setPercentage(double p) {
		this.percentage.setTargetValue(p);
	}

	public void setSpeed(double s) {
		this.percentage.setEaseTime(s);
	}

}
