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

package org.cogaen.spacesweeper;

import org.cogaen.lwjgl.scene.Visual;
import org.lwjgl.opengl.GL11;

public class LineVisual extends Visual {
	
	private float dx;
	private float dy;
	private float width;
	
	public LineVisual(float dx, float dy, float width) {
		this.dx = dx;
		this.dy = dy;
		this.width = width;
	}

	@Override
	public void prolog() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
    	GL11.glEnable(GL11.GL_BLEND);
	}

	@Override
	public void render() {
		GL11.glLineWidth(this.width); 
		getColor().apply();
    	GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(0f, 0f);
		GL11.glVertex2f(this.dx, this.dy);
		GL11.glEnd();
	}

	@Override
	public void epilog() {
		// intentionally left empty
	}

	@Override
	public LineVisual newInstance() {
		LineVisual instance = new LineVisual(this.dx, this.dy, this.width);
		super.copyFields(instance);
		
		return instance;
	}

}
