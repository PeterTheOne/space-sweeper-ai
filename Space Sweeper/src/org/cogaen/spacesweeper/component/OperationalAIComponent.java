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
import org.cogaen.lwjgl.input.ControllerState;
import org.cogaen.name.CogaenId;
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
	private double targetPosX;
	private double targetPosY;
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
		this.targetPosX = this.body.getPositionX();
		this.targetPosY = this.body.getPositionY();
		this.thrustPid = new PidController(0.03, 0.03, 0.03);
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
		double speed = this.body.getSpeed();
		this.thrustPid.update(speed, this.timer.getDeltaTime());

		double vPos = this.thrustPid.getOutput();

		this.vPos = vPos > 1 ? 1 : vPos;
		this.vPos = this.vPos < 0 ? 0 : this.vPos;
	}

	private void updateAngle() {
		double dx = 0;
		double dy = 0;
		
		dx = this.targetPosX - this.body.getPositionX();
		dy = this.targetPosY - this.body.getPositionY();
		
		if (dx == 0 && dy == 0) {
			this.anglePid.update(0, this.timer.getDeltaTime());
			this.hPos = -this.anglePid.getOutput();
			return;
		}
		
		double txr = dx * Math.cos(-this.body.getAngularPosition()) - dy * Math.sin(-this.body.getAngularPosition());
		double tyr = dy * Math.cos(-this.body.getAngularPosition()) + dx * Math.sin(-this.body.getAngularPosition());

		double l = Math.sqrt(txr * txr + tyr * tyr);
		txr /= l;
		
		this.anglePid.update(txr, this.timer.getDeltaTime());
		
		
		double hPos = -this.anglePid.getOutput();
		
		this.hPos = hPos > 1 ? 1 : hPos;
		this.hPos = hPos < -1 ? -1 : hPos;
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
	public void setTarget(double targetPosX, double targetPosY) {
		this.targetPosX = targetPosX;
		this.targetPosY = targetPosY;
		Event event = new TargetChangeEvent(this.body.getPositionX(), 
				this.body.getPositionY(), this.targetPosX, this.targetPosY);
		EventService.getInstance(getCore()).dispatchEvent(event);
	}
	
	@Override
	public void setTargetSpeed(double targetSpeed) {
		this.thrustPid.setTarget(targetSpeed);
	}

	@Override
	public void setShoot(boolean shoot) {
		this.buttons[0] = true;
	}
}
