package org.cogaen.spacesweeper.component;

import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.EntityService;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.event.Event;
import org.cogaen.event.EventService;
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.PositionHelper;
import org.cogaen.spacesweeper.entity.OperationalAIInterface;
import org.cogaen.spacesweeper.entity.ShipEntity;
import org.cogaen.spacesweeper.event.FlowFieldUpdatedEvent;
import org.cogaen.spacesweeper.physics.Body;
import org.cogaen.spacesweeper.state.FlowField;
import org.cogaen.spacesweeper.state.GameLogic;
import org.cogaen.spacesweeper.state.PlayState;
import org.cogaen.spacesweeper.tactic.HuntState;
import org.cogaen.state.DeterministicStateMachine;

public class TacticalAIComponent extends UpdateableComponent {
	
	private CogaenId bodyAttrId;
	private Body body;
	private Body enemyBody;
	private OperationalAIInterface opAI;
	private DeterministicStateMachine stateMachine;
	
	private PositionHelper positionHelper;
	private FlowField flowfield;
	
	public TacticalAIComponent(CogaenId bodyAttrId) {
		super();
		this.bodyAttrId = bodyAttrId;
	}
	
	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
		this.body = (Body) getParent().getAttribute(this.bodyAttrId);
		
		EntityService entServ = EntityService.getInstance(getCore());
		ShipEntity enemyShip = (ShipEntity) entServ.getEntity(GameLogic.PLAYER_ONE_ID);
		this.enemyBody = (Body) enemyShip.getAttribute(this.bodyAttrId);
	}
	
	@Override
	public void engage() {
		super.engage();
		this.opAI = (OperationalAIInterface) getParent().getAttribute(OperationalAIInterface.ATTR_ID);
		this.stateMachine = new DeterministicStateMachine(getCore());
		this.stateMachine.addState(new HuntState(), HuntState.ID);
		this.stateMachine.setStartState(HuntState.ID);
		this.stateMachine.engage();
		double worldWidth = PlayState.DEFAULT_WORLD_WIDTH;
 		double ar = SceneService.getInstance(getCore()).getAspectRatio();
		double worldHeight = worldWidth / ar;
		this.positionHelper = new PositionHelper(worldWidth, worldHeight);
		EventService evntSrv = EventService.getInstance(getCore());
		evntSrv.addListener(this, FlowFieldUpdatedEvent.TYPE_ID);
	}
	
	@Override
	public void disengage() {
		this.stateMachine.disengage();
		super.disengage();
	}
	
	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(FlowFieldUpdatedEvent.TYPE_ID)) {
			handleFlowFieldUpdatedEvent((FlowFieldUpdatedEvent) event);
		}
	}
	
	private void handleFlowFieldUpdatedEvent(FlowFieldUpdatedEvent event) {
		this.flowfield = event.getFlowField();
	}

	@Override
	public void update() {		
		//TODO: check state, create updateMethod for every State, avoid Objects	
		
		//if (this.stateMachine.getCurrentState().equals(HuntState.ID)) {
			huntUpdate();
		//}
	}
	
	private void huntUpdate() {
		// world Wrap
		this.positionHelper.setTarget(this.body.getPositionX(), this.body.getPositionY(), 
				this.enemyBody.getPositionX(), this.enemyBody.getPositionY());
		double targetPosX = this.positionHelper.getTargetX();
		double targetPosY = this.positionHelper.getTargetY();
		
		// calculate target Angle
		double angle1 = positionHelper.calculateAngle(this.body.getAngularPosition(), 
				this.body.getPositionX(), this.body.getPositionY(), targetPosX, targetPosY);
		
		// calculate speed by distance to target
		double dx = targetPosX - this.body.getPositionX();
		double dy = targetPosY - this.body.getPositionY();
		double distance = Math.sqrt(dx * dx + dy * dy);
		
		double maxSpeed = 8;
		double slowingDistance = 12;
		double stopDistance = 4;
		
		double rampedSpeed = maxSpeed * ((distance - stopDistance) / (slowingDistance - stopDistance));
		double speed1 = Math.min(rampedSpeed, maxSpeed);
		speed1 = Math.max(0, speed1);
		

		// add flowfield avoidance to target position
		double ffX = 0;
		double ffY = 0;
		double angle2 = 0;
		// get flow field vector
		if (this.flowfield == null) {
			this.opAI.setTargetAngleAndSpeed(angle1, speed1);
		} else {
			// calculate ff direction
			this.flowfield.calculateFlow(
					this.body.getPositionX(), 
					this.body.getPositionY());
			ffX = this.flowfield.getFlowX();
			ffY = this.flowfield.getFlowY();
			
			// calculate ff angle
			angle2 = positionHelper.calculateAngle(this.body.getAngularPosition(), 
					this.body.getPositionX(), this.body.getPositionY(), 
					this.body.getPositionX() + ffX, 
					this.body.getPositionY() + ffY);
			
			// calculate speed by distance to ff
			double speed2 = 10;
			
			// blend angle between target and ff by ff strength
			double dl = Math.sqrt(ffX * ffX + ffY * ffY);
			double finalAngle = angle1 + (angle2 - angle1) * dl;
			
			// blend speed between target and ff by ff strength
			double finalSpeed = speed1 + (speed2 - speed1) * dl;
			// set move command
			this.opAI.setTargetAngleAndSpeed(finalAngle, finalSpeed);
		}
		
		// shoot
		this.opAI.setShoot(true);
	}
	
	
}
