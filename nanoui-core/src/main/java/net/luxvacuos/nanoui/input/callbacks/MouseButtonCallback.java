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

import java.util.BitSet;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

public class MouseButtonCallback extends GLFWMouseButtonCallback {

	private final long windowID;

	private BitSet button = new BitSet(20);
	private BitSet ignore = new BitSet(20);

	public MouseButtonCallback(long windowID) {
		this.windowID = windowID;
	}

	@Override
	public void invoke(long windowID, int button, int action, int mods) {
		if (this.windowID != windowID)
			return;

		this.button.set(button, (action != GLFW.GLFW_RELEASE));
		if (action == GLFW.GLFW_RELEASE && this.ignore.get(button))
			this.ignore.clear(button);
	}

	public boolean isButtonPressed(int button) {
		return this.button.get(button);
	}
	
	public boolean isButtonIgnored(int button) {
		return this.ignore.get(button);
	}
	
	public void setButtonIgnored(int button) {
		this.ignore.set(button);
	}

}
