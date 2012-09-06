/* 
-----------------------------------------------------------------------------
                   Cogaen - Component-based Game Engine V3
-----------------------------------------------------------------------------
This software is developed by the Cogaen Development Team. Please have a 
look at our project home page for further details: http://www.cogaen.org
   
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Copyright (c) 2010-2011 Roman Divotkey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
*/

package org.cogaen.spacesweeper.component;

import org.cogaen.entity.ComponentEntity;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.input.ControllerState;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.PositionHelper;
import org.cogaen.spacesweeper.entity.OperationalAIInterface;
import org.cogaen.spacesweeper.event.FlowFieldUpdatedEvent;
import org.cogaen.spacesweeper.event.TargetChangeEvent;
import org.cogaen.spacesweeper.physics.Body;
import org.cogaen.spacesweeper.util.PidController;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

public class OperationalAIComponent extends UpdateableComponent implements 
		ControllerState, OperationalAIInterface, EventListener {

	private double hPos;
	private double vPos;
	private boolean buttons[];

	private CogaenId bodyAttrId;
	private Body body;
	private double targetAngle;
	private double targetSpeed;
	private PidController thrustPid;
	private PidController anglePid;
	private Timer timer;
	
	public OperationalAIComponent(int nButtons, CogaenId bodyAttrId) {
		super();
		this.buttons = new boolean[nButtons];
		this.bodyAttrId = bodyAttrId;
	}
	
	@Override
	public void initialize(ComponentEntity parent) {
		super.initialize(parent);
		parent.addAttribute(ControllerState.ID, this);
		parent.addAttribute(OperationalAIInterface.ATTR_ID, this);
		this.body = (Body) getParent().getAttribute(this.bodyAttrId);
	}

	@Override
	public void engage() {
		super.engage();
		this.targetAngle = 0;
		this.thrustPid = new PidController(2, 0.0, 0.0);
		this.thrustPid.setTarget(0);
		this.anglePid = new PidController(2.50, 0.0, 0.0);
		this.anglePid.setTarget(0);
		this.timer = TimeService.getInstance(getCore()).getTimer();
	}

	@Override
	public void disengage() {
		EventService evntSrv = EventService.getInstance(getCore());
		evntSrv.removeListener(this);
		super.disengage();
	}

	@Override
	public void update() {
		updateThrust();
		updateAngle();
	}

	private void updateThrust() {
		// calculate how much speed to set
		// only speed up if angle is the correct target angle! :)
		double finalSpeed = this.targetSpeed * (1 - Math.abs(this.targetAngle));
		
		// set pid target
		this.thrustPid.setTarget(finalSpeed);
		
		// update tur
		this.thrustPid.update(this.body.getSpeed(), this.timer.getDeltaTime());

		// set hPos
		this.vPos = this.thrustPid.getOutput();
		this.vPos = this.vPos > 1 ? 1 : this.vPos;
		this.vPos = this.vPos < 0 ? 0 : this.vPos;
	}

	private void updateAngle() {
		// target is always zero
		
		// update anglePid
		this.anglePid.update(this.targetAngle, this.timer.getDeltaTime());
		
		// set hPos
		this.hPos = -this.anglePid.getOutput();
		this.hPos = this.hPos > 1 ? 1 : this.hPos;
		this.hPos = this.hPos < -1 ? -1 : this.hPos;
	}

	public double getVerticalPosition() {
		return this.vPos;
	}
	
	public double getHorizontalPosition() {
		return this.hPos;
	}
	
	public boolean getButton(int idx) {
		return this.buttons[idx];
	}

	@Override
	public void setShoot(boolean shoot) {
		this.buttons[0] = true;
	}

	@Override
	public void setTargetAngleAndSpeed(double targetAngle, double targetSpeed) {
		this.targetAngle = targetAngle;
		this.targetSpeed = targetSpeed;
	}
}
