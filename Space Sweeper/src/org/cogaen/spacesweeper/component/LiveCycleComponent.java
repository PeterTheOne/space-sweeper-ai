package org.cogaen.spacesweeper.component;

import org.cogaen.entity.Component;
import org.cogaen.entity.ComponentEntity;
import org.cogaen.event.EventService;
import org.cogaen.spacesweeper.entity.Pose2D;
import org.cogaen.spacesweeper.event.DestroyedEvent;
import org.cogaen.spacesweeper.event.SpawnEvent;

public class LiveCycleComponent extends Component {

	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
	}

	@Override
	public void engage() {
		super.engage();
		Pose2D pose = (Pose2D) getParent().getAttribute(Pose2D.ATTR_ID);
		SpawnEvent spawn = new SpawnEvent(getParent().getId(), getParent().getType(), pose);
		EventService.getInstance(getCore()).dispatchEvent(spawn);
	}

	@Override
	public void disengage() {
		Pose2D pose = (Pose2D) getParent().getAttribute(Pose2D.ATTR_ID);
		DestroyedEvent destroyed = new DestroyedEvent(getParent().getId(), getParent().getType(), pose);
		EventService.getInstance(getCore()).dispatchEvent(destroyed);
		super.disengage();
	}

}
