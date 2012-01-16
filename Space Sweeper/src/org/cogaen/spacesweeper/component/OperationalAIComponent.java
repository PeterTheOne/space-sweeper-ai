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
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.input.ControllerState;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.entity.OperationalAIInterface;
import org.cogaen.spacesweeper.physics.Body;
import org.cogaen.spacesweeper.state.PlayState;
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
	
	//TODO: get these from level..
	private double worldWidth;
	private double worldHeight;
	
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
		//TODO: remove bug: ship disappears  when 0 and 0
		this.targetPosX = 0;//this.body.getPositionX();
		this.targetPosY = 2;//this.body.getPositionY();
		this.thrustPid = new PidController(0.03, 0.03, 0.03);
		this.thrustPid.setTarget(0);
		this.anglePid = new PidController(2.50, 0.20, 0.0);
		this.anglePid.setTarget(0);
		this.timer = TimeService.getInstance(getCore()).getTimer();
		this.worldWidth = PlayState.DEFAULT_WORLD_WIDTH;
		double ar = SceneService.getInstance(getCore()).getAspectRatio();
		this.worldHeight = worldWidth / ar;
	}

	@Override
	public void disengage() {
		super.disengage();
	}

	@Override
	public void update() {
		calculateShortestWay();
		updateThrust();
		updateAngle();

		this.vPos = this.vPos > 1 ? 1 : this.vPos;
		this.vPos = this.vPos < 0 ? 0 : this.vPos;
		this.hPos = this.hPos > 1 ? 1 : this.hPos;
		this.hPos = this.hPos < -1 ? -1 : this.hPos;
	}
	
	private void calculateShortestWay() {
		if (this.body.getPositionX() > 0 && this.targetPosX < 0) {
			double altTargetPosX = this.targetPosX + worldWidth;
			if (Math.abs(altTargetPosX - this.body.getPositionX()) < 
					Math.abs(this.targetPosX - this.body.getPositionX())) {
				this.targetPosX = altTargetPosX;
			}
		} else if (this.body.getPositionX() < 0 && this.targetPosX > 0) {
			double altTargetPosX = this.targetPosX - worldWidth;
			if (Math.abs(altTargetPosX - this.body.getPositionX()) < 
					Math.abs(this.targetPosX - this.body.getPositionX())) {
				this.targetPosX = altTargetPosX;
			}
		}
		
		if (this.body.getPositionY() > 0 && this.targetPosY < 0) {
			double altTargetPosY = this.targetPosY + worldHeight;
			double altDist = Math.abs(altTargetPosY - this.body.getPositionY());
			double dist = Math.abs(this.targetPosY - this.body.getPositionY());
			if (altDist < dist) {
				this.targetPosY = altTargetPosY;
			}
		} else if (this.body.getPositionY() < 0 && this.targetPosY > 0) {
			double altTargetPosY = this.targetPosY - worldHeight;
			double altDist = Math.abs(altTargetPosY - this.body.getPositionY());
			double dist = Math.abs(this.targetPosY - this.body.getPositionY());
			if (altDist < dist) {
				this.targetPosY = altTargetPosY;
			}
		}
	}

	private void updateThrust() {
		
		double speed = this.body.getSpeed();
		this.thrustPid.update(speed, this.timer.getDeltaTime());

		this.vPos = this.thrustPid.getOutput();
		
		LoggingService log = LoggingService.getInstance(getCore());
		
		log.logInfo("AIComp", "speed: " + speed);
		log.logInfo("AIComp", "pidOut: " + this.thrustPid.getOutput());
	}

	private void updateAngle() {
		double dx = this.targetPosX - this.body.getPositionX();
		double dy = this.targetPosY - this.body.getPositionY();
		
		double txr = dx * Math.cos(-this.body.getAngularPosition()) - dy * Math.sin(-this.body.getAngularPosition());
		double tyr = dy * Math.cos(-this.body.getAngularPosition()) + dx * Math.sin(-this.body.getAngularPosition());

		double l = Math.sqrt(txr * txr + tyr * tyr);
		txr /= l;
		tyr /= l;
		
		this.anglePid.update(txr, this.timer.getDeltaTime());
		
		this.hPos = -this.anglePid.getOutput();
	}
	
	@Override
	public void handleEvent(Event event) {
		//empty
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
	}
	
	@Override
	public void setTargetSpeed(double targetSpeed) {
		this.thrustPid.setTarget(targetSpeed);
	}
}
