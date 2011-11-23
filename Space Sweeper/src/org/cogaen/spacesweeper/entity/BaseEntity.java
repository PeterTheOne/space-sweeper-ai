package org.cogaen.spacesweeper.entity;

import org.cogaen.action.LoggingAction;
import org.cogaen.core.Core;
import org.cogaen.entity.ActionComponent;
import org.cogaen.entity.ComponentEntity;
import org.cogaen.logging.LoggingService;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.component.LiveCycleComponent;

public class BaseEntity extends ComponentEntity {

	private static final String LIVE_CYCLE_LOGGING_PROP = "liveCycleLogging";

	public BaseEntity(Core core, CogaenId id, CogaenId typeId) {
		super(core, id, typeId);

		if (PropertyService.getInstance(core).getBoolProperty(LIVE_CYCLE_LOGGING_PROP, false)) {
			addLogging();
		}
		
		addComponent(new LiveCycleComponent());
	}

	private void addLogging() {
		LoggingAction engageAction = new LoggingAction(getCore(), LoggingService.Priority.DEBUG, "GAME", "entity " + getId() + " engaged");
		LoggingAction disengageAction = new LoggingAction(getCore(), LoggingService.Priority.DEBUG, "GAME", "entity " + getId() + " disengaged");
		addComponent(new ActionComponent(engageAction, disengageAction));
	}	
}
