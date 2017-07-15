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

import org.lwjgl.nanovg.NVGColor;

import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;

public class Box extends Component {

	private NVGColor color = Theme.rgba(255, 255, 255, 255);
	private float lt, rt, lb, rb;

	public Box(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	@Override
	public void render(Window window) {
		Theme.renderBox(window.getNVGID(), rootComponent.rootX + alignedX,
				window.getHeight() - rootComponent.rootY - alignedY - h, w, h, color, rt, lt, rb, lb);
	}

	public void setColor(float r, float g, float b, float a) {
		color.r(r);
		color.g(g);
		color.b(b);
		color.a(a);
	}

	public void setColor(String hex) {
		color.r(Integer.valueOf(hex.substring(1, 3), 16) / 255f);
		color.g(Integer.valueOf(hex.substring(3, 5), 16) / 255f);
		color.b(Integer.valueOf(hex.substring(5, 7), 16) / 255f);
		color.a(Integer.valueOf(hex.substring(7, 9), 16) / 255f);
	}

	public void setLeftBottom(float lb) {
		this.lb = lb;
	}

	public void setLeftTop(float lt) {
		this.lt = lt;
	}

	public void setRightBottom(float rb) {
		this.rb = rb;
	}

	public void setRightTop(float rt) {
		this.rt = rt;
	}

}
