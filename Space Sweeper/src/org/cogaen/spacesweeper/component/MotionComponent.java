package org.cogaen.spacesweeper.component;

import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.event.EventService;
import org.cogaen.lwjgl.input.ControllerState;
import org.cogaen.name.CogaenId;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.event.ThrustEvent;
import org.cogaen.spacesweeper.physics.Body;

public class MotionComponent extends UpdateableComponent {

	private static final double DEFAULT_LINEAR_ACCELERATION = 15;
	private static final double DEFAULT_ANGULAR_ACCELERATION = 50;
	private static final double DEFAULT_LINEAR_DAMPING = 0.9;
	private static final double DEFAULT_ANGULAR_DAMPING = 10.0;
	private static final String ANGULAR_DAMPING_PROP = "angularDamping";
	private static final String LINEAR_DAMPING_PROP = "linearDamping";
	private static final String ANGULAR_ACCELERATION_PROP = "angularAcceleration";
	private static final String LINEAR_ACCELERATION_PROP = "linearAcceleration";
	
	private ControllerState ctrl;
	private Body body;
	private CogaenId bodyAttrId;
	private EventService evtSrv;
	private double oldThrust = 0;
	private double angularAcceleration;
	private double linearAcceleration;
	
	public MotionComponent(CogaenId bodyAttrId) {
		this.bodyAttrId = bodyAttrId;
	}
	
	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
		this.evtSrv = EventService.getInstance(getCore());
	}

	@Override
	public void engage() {
		super.engage();
		
		this.ctrl = (ControllerState) getParent().getAttribute(ControllerState.ID);
		
		PropertyService prpSrv = PropertyService.getInstance(getCore());
		this.angularAcceleration = prpSrv.getDoubleProperty(ANGULAR_ACCELERATION_PROP, DEFAULT_ANGULAR_ACCELERATION);
		this.linearAcceleration = prpSrv.getDoubleProperty(LINEAR_ACCELERATION_PROP, DEFAULT_LINEAR_ACCELERATION);
		this.body = (Body) getParent().getAttribute(bodyAttrId);
		this.body.setLinearDamping(prpSrv.getDoubleProperty(LINEAR_DAMPING_PROP, DEFAULT_LINEAR_DAMPING));
		this.body.setAngularDamping(prpSrv.getDoubleProperty(ANGULAR_DAMPING_PROP, DEFAULT_ANGULAR_DAMPING));
	}

	@Override
	public void disengage() {
		this.ctrl = null;
		this.body = null;
		super.disengage();
	}

	@Override
	public void update() {
		double alpha = -this.ctrl.getHorizontalPosition() * this.angularAcceleration;
		this.body.setAngularAcceleration(alpha);
		double thrust = this.ctrl.getVerticalPosition() * this.linearAcceleration;
		thrust = thrust > 0 ? thrust : 0;
		
		if (thrust != oldThrust) {
			this.evtSrv.dispatchEvent(new ThrustEvent(getParent().getId(), thrust));
			this.oldThrust = thrust;
		}
		this.body.setAcceleration(thrust);
	}
}
