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
import net.luxvacuos.nanoui.ui.OnAction;

public class WindowButton extends Button {

	private HWND hwnd;
	protected boolean active = false;
	private int icon = -1;
	private OnAction onHover;
	private float timer;
	private boolean hover = false;

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
		if (icon != -1)
			Theme.renderImage(window.getNVGID(), rootComponent.rootX + alignedX + h * 0.30f,
					window.getHeight() - rootComponent.rootY - alignedY - h + h * 0.25f, h * 0.50f, h * 0.50f, icon,
					1f);

	}

	@Override
	public void update(float delta, Window window) {
		if (!enabled)
			return;
		super.update(delta, window);
		if(pressed || pressedRight)
			hover = true;
		if (insideButton(window.getMouseHandler()) && !hover) {
			timer += delta * 2f;
			if (timer >= 1) {
				onHover.onAction();
				hover = true;
			}
		} else {
			timer = 0;
		}
		if(!insideButton(window.getMouseHandler())) {
			hover = false;
		}
	}

	public void reDraw(HWND hwnd, Window window) {
		this.hwnd = hwnd;
		if (icon == -1)
			icon = Util.getIcon(hwnd, window);
	}

	public void setOnHover(OnAction onHover) {
		this.onHover = onHover;
	}

	public HWND getHwnd() {
		return hwnd;
	}

	public float getX() {
		return rootComponent.rootX + alignedX;
	}

}
