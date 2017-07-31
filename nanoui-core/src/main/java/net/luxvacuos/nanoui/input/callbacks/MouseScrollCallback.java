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

import org.lwjgl.glfw.GLFWScrollCallback;

public class MouseScrollCallback extends GLFWScrollCallback {

	private double x, y;

	private final long windowID;

	public MouseScrollCallback(long windowID) {
		this.windowID = windowID;
	}

	@Override
	public void invoke(long windowID, double x, double y) {
		if (this.windowID != windowID)
			return;
		this.x = x;
		this.y = y;
	}

	public double getYWheel() {
		double yWheel = y;
		y = 0;
		return yWheel;
	}

	public double getXWheel() {
		double xWheel = x;
		x = 0;
		return xWheel;
	}

}
