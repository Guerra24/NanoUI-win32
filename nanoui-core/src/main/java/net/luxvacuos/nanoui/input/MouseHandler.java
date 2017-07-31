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

package net.luxvacuos.nanoui.input;

import org.lwjgl.glfw.GLFW;

import net.luxvacuos.nanoui.input.callbacks.MouseButtonCallback;
import net.luxvacuos.nanoui.input.callbacks.MouseEnterCallback;
import net.luxvacuos.nanoui.input.callbacks.MousePosCallback;
import net.luxvacuos.nanoui.input.callbacks.MouseScrollCallback;
import net.luxvacuos.nanoui.rendering.api.glfw.AbstractWindow;

public class MouseHandler {

	private final MouseEnterCallback enterCallback;
	private final MousePosCallback posCallback;
	private final MouseButtonCallback buttonCallback;
	private final MouseScrollCallback scrollCallback;

	private final long windowID;
	@Deprecated
	private final AbstractWindow window; // Temporary

	public MouseHandler(long windowID, AbstractWindow window) {
		this.enterCallback = new MouseEnterCallback(windowID);
		this.posCallback = new MousePosCallback(windowID);
		this.buttonCallback = new MouseButtonCallback(windowID);
		this.scrollCallback = new MouseScrollCallback(windowID);

		this.windowID = windowID;

		GLFW.glfwSetCursorEnterCallback(windowID, this.enterCallback);
		GLFW.glfwSetCursorPosCallback(windowID, this.posCallback);
		GLFW.glfwSetMouseButtonCallback(windowID, this.buttonCallback);
		GLFW.glfwSetScrollCallback(windowID, this.scrollCallback);

		this.window = window;
	}

	public boolean isInside() {
		return this.enterCallback.isInside();
	}

	public float getX() {
		if (this.isInside())
			return (float) this.posCallback.getX();
		else
			return -1;
	}

	public float getY() {
		if (this.isInside())
			return (float) (window.getHeight() - this.posCallback.getY());
		else
			return -1;
	}

	public float getDX() {
		return (float) this.posCallback.getDX();
	}

	public float getDY() {
		return (float) this.posCallback.getDY();
	}

	public boolean isButtonPressedRaw(int button) {
		return GLFW.glfwGetMouseButton(this.windowID, button) == GLFW.GLFW_PRESS;
	}

	public boolean isButtonPressed(int button) {
		return this.buttonCallback.isButtonPressed(button) && !this.buttonCallback.isButtonIgnored(button);
	}

	public void ignoreKeyUntilRelease(int button) {
		this.buttonCallback.setButtonIgnored(button);
	}

	public float getYWheel() {
		return (float) this.scrollCallback.getYWheel();
	}

	public float getXWheel() {
		return (float) this.scrollCallback.getXWheel();
	}

	public static void setGrabbed(long windowID, boolean grab) {
		GLFW.glfwSetInputMode(windowID, GLFW.GLFW_CURSOR, grab ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
	}

}
