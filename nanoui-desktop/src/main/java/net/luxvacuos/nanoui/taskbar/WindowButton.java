/*
 * This file is part of NanoUI
 * 
 * Copyright (C) 2017 Guerra24
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

package net.luxvacuos.nanoui.taskbar;

import com.sun.jna.platform.win32.WinDef.HWND;

import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.nanoui.ui.Button;

public class WindowButton extends Button {

	private HWND hwnd;
	protected boolean active = false;

	public WindowButton(float x, float y, float w, float h, String text, HWND hwnd) {
		super(x, y, w, h, text);
		this.hwnd = hwnd;
	}

	@Override
	public void render(Window window) {
		if (!enabled)
			return;
		Theme.renderTaskbarWindowButton(window.getNVGID(), preicon, text, font, entypo, rootComponent.rootX + alignedX,
				window.getHeight() - rootComponent.rootY - alignedY - h, w, h, inside, active, fontSize);
	}

	public HWND getHwnd() {
		return hwnd;
	}
	
	public float getX() {
		return rootComponent.rootX + alignedX;
	}

}
