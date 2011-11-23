package org.cogaen.spacesweeper.util;

import org.cogaen.core.Core;
import org.cogaen.core.Engageable;

public class Logic implements Engageable {

	
	private Core core;
	private boolean engaged;
	
	public Logic(Core core) {
		this.core = core;
	}

	@Override
	public void engage() {
		this.engaged = true;
	}

	@Override
	public void disengage() {
		this.engaged = false;
	}

	@Override
	public final boolean isEngaged() {
		return this.engaged;
	}

	public final Core getCore() {
		return core;
	}
}
