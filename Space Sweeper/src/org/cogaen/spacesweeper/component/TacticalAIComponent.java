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

		// add flowfield avoidance to target position
		double ffX = 0;
		double ffY = 0;
		double ffFactor = 30.0;
		// get flow field vector
		if (this.flowfield != null) {
			this.flowfield.calculateFlow(
					this.body.getPositionX(), 
					this.body.getPositionY());
			ffX = this.flowfield.getFlowX() * ffFactor;
			ffY = this.flowfield.getFlowY() * ffFactor;
		}
		targetPosX += ffX;
		targetPosY += ffY;
		
		// calculate speed
		double dx = targetPosX - this.body.getPositionX();
		double dy = targetPosY - this.body.getPositionY();
		double dl = Math.sqrt(dx * dx + dy * dy);
		double speed = dl / 1.5d;
		
		LoggingService log = LoggingService.getInstance(getCore());
		log.logInfo("TacAIComp", "speed: " + speed);
		
		// set move command
		moveCommand(speed, targetPosX, targetPosY);
	}
	
	private void moveCommand(double speed, double targetPosX, double targetPosY) {
		this.opAI.setTargetSpeed(speed);
		this.opAI.setTarget(targetPosX, targetPosY);
	}
	
	
}
