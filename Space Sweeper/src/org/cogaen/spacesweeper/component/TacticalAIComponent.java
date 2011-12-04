package org.cogaen.spacesweeper.component;

import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.EntityService;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.entity.OperationalAIInterface;
import org.cogaen.spacesweeper.entity.ShipEntity;
import org.cogaen.spacesweeper.physics.Body;
import org.cogaen.spacesweeper.state.GameLogic;
import org.cogaen.spacesweeper.tactic.HuntState;
import org.cogaen.state.DeterministicStateMachine;

public class TacticalAIComponent extends UpdateableComponent {
	
	private CogaenId bodyAttrId;
	private Body body;
	private Body enemyBody;
	private OperationalAIInterface opAI;
	private DeterministicStateMachine stateMachine;
	
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
	}
	
	@Override
	public void disengage() {
		this.stateMachine.disengage();
		super.disengage();
	}

	@Override
	public void update() {		
		//TODO: check state, create updateMethod for every State, avoid Objects	
		
		if (this.stateMachine.getCurrentState().equals(HuntState.ID)) {
			//TODO:
			//if (no obsticle in the way)
			setShipAsTarget(this.enemyBody);
			// else 
			// avoid
		}
	}
	
	private void setShipAsTarget(Body targetBody) {
		double targetPosX = targetBody.getPositionX();
		double targetPosY = targetBody.getPositionY();
		this.opAI.setTarget(targetPosX, targetPosY);
	}
	
	
}
