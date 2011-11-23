package org.cogaen.spacesweeper.hud;

import org.cogaen.core.Core;
import org.cogaen.event.Event;
import org.cogaen.event.EventListener;
import org.cogaen.event.EventService;
import org.cogaen.lwjgl.input.KeyCode;
import org.cogaen.lwjgl.input.KeyPressedEvent;
import org.cogaen.lwjgl.scene.Color;
import org.cogaen.lwjgl.scene.MultiLineLabelVisual;
import org.cogaen.lwjgl.scene.RectangleVisual;
import org.cogaen.lwjgl.scene.SceneNode;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.lwjgl.scene.TextBlockVisual;
import org.cogaen.property.PropertyService;
import org.cogaen.spacesweeper.SpaceSweeper;
import org.cogaen.view.AbstractHud;

public class HelpHud extends AbstractHud implements EventListener {

	private static final double WIDTH = 0.5;
	private static final double HEIGHT = 0.32;
	private static final double MARGIN = 0.01;
	private SceneNode node;
	private TextBlockVisual textBlock;
	private MultiLineLabelVisual textLabel;
	
	public HelpHud(Core core) {
		super(core);
	}

	@Override
	public void engage() {
		super.engage();
		double referenceResolution = PropertyService.getInstance(getCore()).getDoubleProperty(SpaceSweeper.REFERENCE_RESOLUTION_PROP);
		EventService.getInstance(getCore()).addListener(this, KeyPressedEvent.TYPE_ID);
		
		SceneService scnSrv = SceneService.getInstance(getCore());
		this.node = scnSrv.createNode();
		this.node.setPose(0.5, 0.5 / scnSrv.getAspectRatio(), 0);
		
		// make frame
		RectangleVisual rec = new RectangleVisual(WIDTH + MARGIN, HEIGHT + MARGIN);
		rec.setColor(new Color(0.2, 0.2, 0.2, 0.7));
		rec.setFilled(true);
		this.node.addVisual(rec);
		
		rec = new RectangleVisual(WIDTH + MARGIN, HEIGHT + MARGIN);
		rec.setColor(new Color(1, 1, 1));
		rec.setFilled(false);
		this.node.addVisual(rec);

		this.textBlock = new TextBlockVisual(getCore(), "StdFont", WIDTH * referenceResolution, HEIGHT * referenceResolution);
//		this.node.addVisual(this.textBlock);
		
		this.textLabel = new MultiLineLabelVisual(getCore(), "InfoFont", WIDTH * referenceResolution, HEIGHT * referenceResolution);
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("Space Sweeper\n\n");
		strBuf.append("Copyright (c) 2011 Roman Divotkey, Wolfgang Hoffelner\n\n");
		strBuf.append("Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, ");
		strBuf.append("including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\n");
		strBuf.append("The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\n");
		strBuf.append("THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, ");
		strBuf.append("DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.");
		
		this.textLabel.setText(strBuf.toString());
		this.textLabel.setColor(Color.WHITE);
		this.textLabel.setScale(1.0 / referenceResolution);
		this.node.addVisual(this.textLabel);
		scnSrv.getOverlayRoot().addNode(this.node);
	}

	@Override
	public void disengage() {
		SceneService scnSrv = SceneService.getInstance(getCore());
		scnSrv.destroyNode(this.node);
		
		super.disengage();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(KeyPressedEvent.TYPE_ID)) {
			handleKeyPressed((KeyPressedEvent) event);
		}
	}

	private void handleKeyPressed(KeyPressedEvent event) {
		if (KeyCode.isPrintable(event.getKeyCode())) {
			char ch = KeyCode.getChar(event.getKeyCode(), false);
			this.textBlock.addChar(ch);
			return;
		}
		
		switch (event.getKeyCode()) {
		
		case KeyCode.KEY_BACK:
			this.textBlock.back();
			break;
			
		case KeyCode.KEY_LEFT:
			this.textBlock.left();
			break;
			
		case KeyCode.KEY_RIGHT:
			this.textBlock.right();
			break;

		case KeyCode.KEY_UP:
			this.textBlock.up();
			break;
			
		case KeyCode.KEY_DOWN:
			this.textBlock.down();
			break;			
		}
	}
	
}
