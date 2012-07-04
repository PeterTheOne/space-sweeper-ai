package org.cogaen.spacesweeper.state;

import org.cogaen.task.Task;

public class UpdateFlowFieldTask implements Task {

	private static final String DEFAULT_NAME = "UpdateFlowFieldTask";

	private FlowField flowField;
	
	public UpdateFlowFieldTask(FlowField flowField) {
		this.flowField = flowField;
	}

	@Override
	public void update() {
		this.flowField.update();
	}

	@Override
	public void destroy() {
		// empty
	}

	@Override
	public String getName() {
		return DEFAULT_NAME;
	}

}
