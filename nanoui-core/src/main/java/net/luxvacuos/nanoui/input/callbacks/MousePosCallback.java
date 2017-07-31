/*
 * This file is part of NanoUI
 * 
 * Copyright (C) 2016-2017 Lux Vacuos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.luxvacuos.nanoui.input.callbacks;

import org.lwjgl.glfw.GLFWCursorPosCallback;

public class MousePosCallback extends GLFWCursorPosCallback {

	private double lastX, lastY, x, y, dx, dy;

	private final long windowID;

	public MousePosCallback(long windowID) {
		this.windowID = windowID;
	}

	@Override
	public void invoke(long window, double xpos, double ypos) {
		if (this.windowID != window)
			return;
		this.lastX = this.x;
		this.lastY = this.y;
		this.x = xpos;
		this.y = ypos;
		this.dx = this.x - this.lastX;
		this.dy = this.y - this.lastY;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getDX() {
		return dx;
	}

	public double getDY() {
		return dy;
	}

}
