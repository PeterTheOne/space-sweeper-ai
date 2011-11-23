package org.cogaen.spacesweeper.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cogaen.core.Core;
import org.cogaen.event.EventService;
import org.cogaen.logging.LoggingService;
import org.cogaen.spacesweeper.event.MessageEvent;
import org.cogaen.task.AbstractTask;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class RandomTextTask extends AbstractTask {

	private static final char PAGE_BREAK = '~';
	private static final double MAX_TIME = 120;
	private static final double MIN_TIME = 30;
	private List<String> pages = new ArrayList<String>();
	private List<String> displayedPages = new ArrayList<String>();
	private Timer timer;
	private double timeStamp;
	private Random rnd = new Random();
	
	public RandomTextTask(Core core) {
		super(core, "Random Text");
		this.timer = TimeService.getInstance(core).getTimer();
		this.timeStamp = this.timer.getTime() + MIN_TIME + (MAX_TIME - MIN_TIME) * this.rnd.nextDouble();
		LoggingService.getInstance(getCore()).logDebug("GAME", String.format("next random text in about %3.0f seconds", this.timeStamp - this.timer.getTime()));
	}

	@Override
	public void update() {
		if (this.timeStamp <= this.timer.getTime()) {
			if (this.pages.size() <= 0) {
				this.pages.addAll(this.displayedPages);
				this.displayedPages.clear();
			}
			
			if (this.pages.size() > 0) {
				int n = this.rnd.nextInt(this.pages.size());
				String page = pages.remove(n);
				EventService.getInstance(getCore()).dispatchEvent(new MessageEvent(page));
				this.displayedPages.add(page);
			}
			this.timeStamp = this.timer.getTime() + MIN_TIME + (MAX_TIME - MIN_TIME) * this.rnd.nextDouble();
			LoggingService.getInstance(getCore()).logDebug("GAME", String.format("next random text in about %3.0f seconds", this.timeStamp - this.timer.getTime()));
		}
	}

	public void addPage(String str) {
		if (str.length() > 0) {
			this.pages.add(str);
		}
	}
	
	public void addPages(String str) {
		int idx1 = 0;
		while (true) {
			int idx2 = str.indexOf(PAGE_BREAK, idx1);
			if (idx2 == -1) {
				addPage(str.substring(idx1));
				break;
			} else {
				addPage(str.substring(idx1, idx2));
				idx1 = eatWhiteSpace(str, idx2 + 1);
			}
		}
	}

	private int eatWhiteSpace(String text, int idx) {
		if (idx >= text.length()) {
			return idx;
		}
		
		char ch = text.charAt(idx);
		while (idx < text.length() - 1 && (ch == ' ' || ch == '\n' || ch == '\t' || ch =='\r')) {
			ch = text.charAt(++idx);
		}
		
		return idx;
	}
	
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
