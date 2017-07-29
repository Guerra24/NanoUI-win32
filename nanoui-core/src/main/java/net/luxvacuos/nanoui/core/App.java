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

package net.luxvacuos.nanoui.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import net.luxvacuos.nanoui.core.states.IState;
import net.luxvacuos.nanoui.core.states.StateMachine;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;

public class App {

	private AppUI appUI;

	public App(IState initialState) {
		GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
		init(initialState);
	}

	public void init(IState initialState) {
		appUI = new AppUI();
		appUI.init();
		StateMachine.registerState(initialState);
		StateMachine.setCurrentState(initialState.getName());
		try {
			StateMachine.run();
			update();
			dispose();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			handleError(t);
		}
	}

	public void update() {
		float delta = 0;
		float accumulator = 0f;
		float interval = 1f / 30;
		float alpha = 0;
		int fps = 30;
		Window window = AppUI.getMainWindow();
		while (StateMachine.isRunning() && !(window.isCloseRequested())) {
			TaskManager.update();
			delta = window.getDelta();
			accumulator += delta;
			while (accumulator >= interval) {
				appUI.update(interval);
				StateMachine.update(interval);
				accumulator -= interval;
			}
			alpha = accumulator / interval;
			StateMachine.render(alpha);
			window.updateDisplay(fps);
		}
	}

	public void handleError(Throwable e) {
		e.printStackTrace();
		update();
		dispose();
	}

	public void dispose() {
		StateMachine.dispose();
		appUI.dispose();
	}

}
