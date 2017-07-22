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

package net.luxvacuos.nanoui.ui;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_MIDDLE;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;

import net.luxvacuos.nanoui.core.Variables;
import net.luxvacuos.nanoui.core.states.StateMachine;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme.ButtonStyle;

public class ComponentWindow {

	protected RootComponent rootComponent;
	protected ITitleBar titlebar;
	private NVGColor backgroundColor = Theme.rgba(0, 0, 0, 255);

	public ComponentWindow(Window window) {
		rootComponent = new RootComponent(0, window.getHeight(), window.getWidth(), window.getHeight());
		titlebar = new TitleBar(this);
	}

	public void init(Window window) {
		titlebar.getLeft().setLayout(new FlowLayout(Direction.RIGHT, 0, 0));
		titlebar.getRight().setLayout(new FlowLayout(Direction.LEFT, 0, 0));
		TitleBarButton closeBtn = new TitleBarButton(0, 0, 46, 32);
		closeBtn.setOnButtonPress(() -> {
			StateMachine.stop();
		});
		closeBtn.setWindowAlignment(Alignment.RIGHT_TOP);
		closeBtn.setAlignment(Alignment.LEFT_BOTTOM);
		closeBtn.setStyle(ButtonStyle.CLOSE);

		TitleBarButton maximizeBtn = new TitleBarButton(0, 0, 46, 32);
		maximizeBtn.setOnButtonPress(() -> {
			if (window.isMaximized()) {
				GLFW.glfwRestoreWindow(window.getID());
				maximizeBtn.setStyle(ButtonStyle.MAXIMIZE);
			} else {
				GLFW.glfwMaximizeWindow(window.getID());
				maximizeBtn.setStyle(ButtonStyle.RESTORE);
			}
		});
		maximizeBtn.setWindowAlignment(Alignment.RIGHT_TOP);
		maximizeBtn.setAlignment(Alignment.LEFT_BOTTOM);
		maximizeBtn.setStyle(ButtonStyle.MAXIMIZE);

		TitleBarButton minimizeBtn = new TitleBarButton(0, 0, 46, 32);
		minimizeBtn.setOnButtonPress(() -> {
			GLFW.glfwIconifyWindow(window.getID());
		});
		minimizeBtn.setWindowAlignment(Alignment.RIGHT_TOP);
		minimizeBtn.setAlignment(Alignment.LEFT_BOTTOM);
		minimizeBtn.setStyle(ButtonStyle.MINIMIZE);

		TitleBarText titleText = new TitleBarText(Variables.TITLE, 0, 0);
		titleText.setWindowAlignment(Alignment.LEFT);
		titleText.setAlign(NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE);
		titlebar.getRight().addComponent(closeBtn);
		titlebar.getRight().addComponent(maximizeBtn);
		titlebar.getRight().addComponent(minimizeBtn);
		TitleBarButton backBtn = new TitleBarButton(0, 0, 48, 32);
		backBtn.setStyle(ButtonStyle.LEFT_ARROW);
		TitleBarButton forwardBtn = new TitleBarButton(0, 0, 48, 32);
		forwardBtn.setStyle(ButtonStyle.RIGHT_ARROW);
		titlebar.getLeft().addComponent(backBtn);
		titlebar.getLeft().addComponent(forwardBtn);
		titlebar.getLeft().addComponent(titleText);
	}

	public void render(Window window) {
		window.beingNVGFrame();
		if (titlebar.isEnabled())
			Theme.renderBox(window.getNVGID(), 0, 1, window.getWidth(), window.getHeight() - 1, backgroundColor, 0, 0,
					0, 0);
		else
			Theme.renderBox(window.getNVGID(), 0, 0, window.getWidth(), window.getHeight(), backgroundColor, 0, 0, 0,
					0);
		rootComponent.render(window);
		titlebar.render(window);
		window.endNVGFrame();
	}

	public void update(float delta, Window window) {
		titlebar.update(delta, window);
		titlebar.alwaysUpdate(delta, window);
		rootComponent.update(delta, window);
		if (titlebar.isEnabled())
			rootComponent.alwaysUpdate(delta, window, 0, window.getHeight() - Variables.TITLEBAR_HEIGHT - 1,
					window.getWidth(), window.getHeight() - Variables.TITLEBAR_HEIGHT - 1);
		else
			rootComponent.alwaysUpdate(delta, window, 0, window.getHeight() - Variables.TITLEBAR_HEIGHT,
					window.getWidth(), window.getHeight() - Variables.TITLEBAR_HEIGHT);
	}

	public void dispose(Window window) {
		rootComponent.dispose();
	}

	public void addComponent(Component component) {
		rootComponent.addComponent(component);
	}

	public void addAllComponents(List<Component> components) {
		rootComponent.addAllComponents(components);
	}

	public void setLayout(ILayout layout) {
		rootComponent.setLayout(layout);
	}

	public void setBackgroundColor(float r, float g, float b, float a) {
		backgroundColor.r(r);
		backgroundColor.g(g);
		backgroundColor.b(b);
		backgroundColor.a(a);
	}

	public void setBackgroundColor(String hex) {
		backgroundColor.r(Integer.valueOf(hex.substring(1, 3), 16) / 255f);
		backgroundColor.g(Integer.valueOf(hex.substring(3, 5), 16) / 255f);
		backgroundColor.b(Integer.valueOf(hex.substring(5, 7), 16) / 255f);
		backgroundColor.a(Integer.valueOf(hex.substring(7, 9), 16) / 255f);
	}

	public ITitleBar getTitlebar() {
		return titlebar;
	}

}
