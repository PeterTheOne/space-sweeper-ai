package org.cogaen.spacesweeper.task;

import org.cogaen.core.Core;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.event.MessageEvent;
import org.cogaen.task.AbstractTask;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class GuideTask extends AbstractTask {

	public static final CogaenId END_OF_GUIDE_EVENT_ID = new CogaenId("EndOfGuide");
	private static final char PAGE_BREAK = '~';
	private static final double DEFAULT_DELAY = 8.0;
	private static final String GUIDE_DELAY_PROP = "guideDelay";
	private String guideText;
	private Timer timer;
	private double timeStamp;
	private int idx;
	private double msgDelay;
	
	public GuideTask(Core core, String guideText) {
		super(core, "Guide");
		this.guideText = guideText;
		this.timer = TimeService.getInstance(core).getTimer();
		this.timeStamp = this.timer.getTime();
		this.idx = 0;
		this.msgDelay = PropertyService.getInstance(getCore()).getDoubleProperty(GUIDE_DELAY_PROP, DEFAULT_DELAY);
	}

	@Override
	public void update() {
		if (this.timeStamp <= this.timer.getTime()) {
			String page = searchPage();
			if (page == null) {
				EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(END_OF_GUIDE_EVENT_ID));
			} else {
				EventService.getInstance(getCore()).dispatchEvent(new MessageEvent(page));
				if (isEndOfText()) {
					EventService.getInstance(getCore()).dispatchEvent(new SimpleEvent(END_OF_GUIDE_EVENT_ID));
				}
			}
			this.timeStamp = this.timer.getTime() + this.msgDelay;
		}
	}

	private boolean isEndOfText() {
		return this.idx == -1;
	}
	
	private String searchPage() {
		if (this.idx == -1) {
			return null;
		}
		
		int i = this.guideText.indexOf(PAGE_BREAK, this.idx);
		String page = null;
		if (i == -1) {
			page = this.guideText.substring(this.idx);
			this.idx = -1;
		} else {
			page = this.guideText.substring(this.idx, i);
			this.idx = i + 1;
			eatWhiteSpace();
		}
		
		return page;
	}
	
	private void eatWhiteSpace() {
		char ch = this.guideText.charAt(this.idx);
		while (this.idx < this.guideText.length() && (ch == ' ' || ch == '\n' || ch == '\t' || ch =='\r')) {
			ch = this.guideText.charAt(++this.idx);
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
