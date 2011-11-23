package org.cogaen.spacesweeper.component;

import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.event.SimpleEvent;
import org.cogaen.name.CogaenId;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class TimeToLiveComponent extends UpdateableComponent {

	public static final CogaenId END_OF_LIFE = new CogaenId("EndOfLife");
	private Timer timer;
	private double timeToLive;
	private double endOfLife;
	
	public TimeToLiveComponent(double ttl) {
		this.timeToLive = ttl;
	}
	
	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
		
		this.timer = TimeService.getInstance(getCore()).getTimer();
	}
	
	@Override
	public void update() {
		if (this.timer.getTime() >= this.endOfLife) {
			getParent().handleEvent(new SimpleEvent(END_OF_LIFE));
		}
	}

	@Override
	public void engage() {
		super.engage();
		this.endOfLife = this.timer.getTime() + this.timeToLive;
	}

	@Override
	public void disengage() {
		super.disengage();
	}

}
