/*
 * This file is part of Light Engine
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

import java.util.List;

import org.lwjgl.nanovg.NVGColor;

import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;

public class ComponentWindow {

	protected RootComponent rootComponent;
	private NVGColor backgroundColor = Theme.rgba(0, 0, 0, 255);

	public ComponentWindow(Window window) {
		rootComponent = new RootComponent(0, window.getHeight(), window.getWidth(), window.getHeight());
	}

	public void init(Window window) {
	}

	public void render(Window window) {
		window.beingNVGFrame();
		Theme.renderBox(window.getNVGID(), 0, 0, window.getWidth(), window.getHeight(),
				backgroundColor, 0, 0, 0, 0);
		rootComponent.render(window);
		window.endNVGFrame();
	}

	public void update(float delta, Window window) {
		rootComponent.update(delta, window);
		rootComponent.alwaysUpdate(delta, window, 0, window.getHeight(), window.getWidth(), window.getHeight());
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

}
