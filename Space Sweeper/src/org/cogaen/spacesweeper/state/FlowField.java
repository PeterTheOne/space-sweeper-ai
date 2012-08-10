package org.cogaen.spacesweeper.state;

import org.cogaen.core.Core;
import org.cogaen.core.Engageable;
import org.cogaen.entity.Entity;
import org.cogaen.entity.EntityService;
import org.cogaen.event.Event;
import org.cogaen.event.EventService;
import org.cogaen.event.SimpleEvent;
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.name.CogaenId;
import org.cogaen.spacesweeper.PositionHelper;
import org.cogaen.spacesweeper.entity.BigAsteroid;
import org.cogaen.spacesweeper.entity.MediumAsteroid;
import org.cogaen.spacesweeper.entity.Pose2D;
import org.cogaen.spacesweeper.entity.SmallAsteroid;
import org.cogaen.spacesweeper.event.FlowFieldEngagedEvent;
import org.cogaen.spacesweeper.event.FlowFieldUpdatedEvent;
import org.cogaen.task.TaskService;

public class FlowField implements Engageable {

	public static final CogaenId FF_DISENGAGED = new CogaenId("FF Disengaged");
	
	private Core core;
	private EventService evntSrv;
	private LoggingService logSrv;
	private TaskService taskSrv;
	
	private boolean engaged;

	private double worldWidth;
	private double worldHeight;
	private double worldWidthHalf;
	private double worldHeightHalf;	
	private double[][][] field;
	private UpdateFlowFieldTask updateTask;

	private double resultX;
	private double resultY;

	public FlowField(Core core) {
		this.engaged = false;
		this.core = core;
	}

	@Override
	public void engage() {
		this.evntSrv = EventService.getInstance(this.core);
		this.logSrv = LoggingService.getInstance(this.core);
		this.taskSrv = TaskService.getInstance(this.core);
		
		this.updateTask = new UpdateFlowFieldTask(this);
		this.taskSrv.attachTask(this.updateTask);
		
		this.worldWidth = PlayState.DEFAULT_WORLD_WIDTH;
		double ar = SceneService.getInstance(this.core).getAspectRatio();
		this.worldHeight = worldWidth / ar;
		
		this.worldWidthHalf = this.worldWidth / 2d;
		this.worldHeightHalf = this.worldHeight / 2d;

		this.field = new double[(int) Math.floor(this.worldWidth)]
		                        [(int) Math.floor(this.worldHeight)][2];
		clearField();

		this.engaged = true;
		Event event = new FlowFieldEngagedEvent(field.length, field[0].length);
		this.evntSrv.dispatchEvent(event);
	}

	private void clearField() {
		for (int x = 0; x < (int) this.worldWidth; x++) {
			for (int y = 0; y < (int) this.worldHeight; y++) {
				this.field[x][y][0] = 0;
				this.field[x][y][1] = 0;
			}
		}
	}
	
	@Override
	public void disengage() {
		this.evntSrv.dispatchEvent(new SimpleEvent(FF_DISENGAGED));
		this.engaged = false;
		this.taskSrv.destroyTask(this.updateTask);
	}

	@Override
	public boolean isEngaged() {
		return this.engaged;
	}

	public void update() {
		EntityService entSrv = EntityService.getInstance(this.core);
		clearField();
		for (int i = 0; i < entSrv.numEntities(); i++) {
			Entity ent = entSrv.getEntity(i);
			if (ent == null) {
				continue;
			}
			Pose2D pose2D = null;
			if (ent.getType().equals(BigAsteroid.TYPE)) {
				BigAsteroid ast = (BigAsteroid) ent;
				pose2D = (Pose2D) ast.getAttribute(Pose2D.ATTR_ID);
			} else if (ent.getType().equals(MediumAsteroid.TYPE)) {
				MediumAsteroid ast = (MediumAsteroid) ent;
				pose2D = (Pose2D) ast.getAttribute(Pose2D.ATTR_ID);
			} else if (ent.getType().equals(SmallAsteroid.TYPE)) {
				SmallAsteroid ast = (SmallAsteroid) ent;
				pose2D = (Pose2D) ast.getAttribute(Pose2D.ATTR_ID);
			}
			
			if (pose2D == null) {
				continue;
			}
			
			double sigma = 4;
			double gaussRadius = (int) Math.ceil(sigma * 3.0f);

			int startX = (int) Math.floor((pose2D.getPosX() + this.worldWidthHalf  - gaussRadius + worldWidth) % Math.floor(worldWidth));
			int startY = (int) Math.floor((pose2D.getPosY() + this.worldHeightHalf  - gaussRadius + worldHeight) % Math.floor(worldHeight));
			for (int u = 0, x = startX; u < 2 * gaussRadius; u++, x++, x %= Math.floor(worldWidth)) {
				for (int v = 0, y = startY; v < 2 * gaussRadius; v++, y++, y %= Math.floor(worldHeight)) {
					// find shortest path with PositionHelper
					PositionHelper posHelper = new PositionHelper(this.worldWidth, this.worldHeight);
					posHelper.setTarget(
							pose2D.getPosX(), 
							pose2D.getPosY(), 
							x - this.worldWidthHalf, 
							y - this.worldHeightHalf
					);
					double newX = posHelper.getTargetX();
					double newY = posHelper.getTargetY();
					
					double deltaX = newX - pose2D.getPosX();
					double deltaY = newY - pose2D.getPosY();
					double deltaLength = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
					
					// gauss
					double dividend =  Math.pow(deltaLength, 2);
					double divisor = (1 * Math.pow(sigma, 2));
					double gauss = Math.exp(- dividend / divisor);
					
					// set angle
					this.field[x][y][0] += deltaX * gauss;
					this.field[x][y][1] += deltaY * gauss;
				}
			}
		}
		// normalize when to strong
		for (int x = 0; x < (int) this.worldWidth; x++) {
			for (int y = 0; y < (int) this.worldHeight; y++) {
				double x2 = this.field[x][y][0] * this.field[x][y][0];
				double y2 = this.field[x][y][1] * this.field[x][y][1];
				double dl = Math.sqrt(x2 + y2);
				if (dl > 1) {
					this.field[x][y][0] /= dl;
					this.field[x][y][1] /= dl;
				}
			}
		}
		
		this.evntSrv.dispatchEvent(new FlowFieldUpdatedEvent(this));
	}
	
	public void calculateFlow(double x, double y) {
		x += worldWidthHalf;
		y += worldHeightHalf;
		
		double tx = x - Math.floor(x);
		double ty = y - Math.floor(y);

		int x0 = (int) (Math.floor(x + worldWidth)			% Math.floor(worldWidth));
		int x1 = (int) (Math.floor(x + worldWidth + 1)		% Math.floor(worldWidth));
		int y0 = (int) (Math.floor(y + worldHeight)			% Math.floor(worldHeight));
		int y1 = (int) (Math.floor(y + worldHeight + 1)		% Math.floor(worldHeight));

		double P0x = this.field[x0][y0][0];
		double P0y = this.field[x0][y0][1];
		double P1x = this.field[x1][y0][0];
		double P1y = this.field[x1][y0][1];
		double P2x = this.field[x0][y1][0];
		double P2y = this.field[x0][y1][1];
		double P3x = this.field[x1][y1][0];
		double P3y = this.field[x1][y1][1];

		double term1X = (1 - tx) * P0x + tx * P1x;
		double term2X = (1 - tx) * P2x + tx * P3x;
		this.resultX = (1 - ty) * term1X + ty * term2X;
		
		double term1Y = (1 - tx) * P0y + tx * P1y;
		double term2Y = (1 - tx) * P2y + tx * P3y;
		this.resultY = (1 - ty) * term1Y + ty * term2Y;
	}
	
	// todo: test this function
	public double getFlowStrength(double x, double y) {
		x += worldWidthHalf;
		y += worldHeightHalf;
		
		double tx = x - Math.floor(x);
		double ty = y - Math.floor(y);

		int x0 = (int) (Math.floor(x + worldWidth)			% Math.floor(worldWidth));
		int x1 = (int) (Math.floor(x + worldWidth + 1)		% Math.floor(worldWidth));
		int y0 = (int) (Math.floor(y + worldHeight)			% Math.floor(worldHeight));
		int y1 = (int) (Math.floor(y + worldHeight + 1)		% Math.floor(worldHeight));

		double P0 = this.field[x0][y0][2];
		double P1 = this.field[x1][y0][2];
		double P2 = this.field[x0][y1][2];
		double P3 = this.field[x1][y1][2];

		double term1 = (1 - tx) * P0 + tx * P1;
		double term2 = (1 - tx) * P2 + tx * P3;
		double result = (1 - ty) * term1 + ty * term2;
		
		return result;
	}

	public double getFlowX() {
		return this.resultX;
	}
	
	public double getFlowY() {
		return this.resultY;
	}

	public double[][][] getField() {
		return this.field;
	}

}
