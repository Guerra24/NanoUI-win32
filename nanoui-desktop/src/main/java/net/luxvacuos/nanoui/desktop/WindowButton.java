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

package net.luxvacuos.nanoui.desktop;

import static org.lwjgl.nanovg.NanoVG.nvgDeleteImage;

import org.lwjgl.glfw.GLFW;

import com.sun.jna.platform.win32.WinDef.HWND;

import net.luxvacuos.nanoui.input.KeyboardHandler;
import net.luxvacuos.nanoui.input.MouseHandler;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.nanoui.ui.Button;
import net.luxvacuos.nanoui.ui.OnAction;

public class WindowButton extends Button {

	private static final float FLASH_TIMER = 0.5f, AGAIN_TIMER = 0.5f;

	private HWND hwnd;
	protected boolean active = false, truefullscreen;
	private int icon = -1;
	private OnAction onHover, onLeft, onRight;
	private float timer;
	private boolean hover = false, flash, tryAgain;
	private float flashTimer, tryAgainTimer;

	public WindowButton(float x, float y, float w, float h, String text, HWND hwnd) {
		super(x, y, w, h, text);
		this.hwnd = hwnd;
	}

	@Override
	public void render(Window window) {
		if (!enabled)
			return;
		Theme.renderTaskbarWindowButton(window.getNVGID(), preicon, text, font, entypo, rootComponent.rootX + alignedX,
				window.getHeight() - rootComponent.rootY - alignedY - h, w, h, inside, active, flash, fontSize);
		if (icon != -1)
			Theme.renderImage(window.getNVGID(), rootComponent.rootX + alignedX + h * 0.20f,
					window.getHeight() - rootComponent.rootY - alignedY - h + h * 0.20f, 24, 24, icon, 1f);
	}

	@Override
	public void update(float delta, Window window) {
		if (!enabled)
			return;
		super.update(delta, window);
		MouseHandler mh = window.getMouseHandler();
		if (pressed || pressedRight)
			hover = true;
		if (insideButton(mh) && !hover) {
			timer += delta * 2f;
			if (timer >= 1) {
				onHover.onAction();
				hover = true;
			}
		} else {
			timer = 0;
		}
		if (!insideButton(mh)) {
			hover = false;
		}
		if (flash) {
			flashTimer += delta;
			if (flashTimer > FLASH_TIMER) {
				flash = false;
				flashTimer = 0;
			}
		}
		if (tryAgain) {
			tryAgainTimer += delta;
			if (tryAgainTimer > AGAIN_TIMER)
				if (icon == -1) {
					icon = Util.getIcon(hwnd, window);
					if (icon != -1)
						tryAgain = false;
					tryAgainTimer = 0;
				} else {
					tryAgain = false;
					tryAgainTimer = 0;
				}
		}
		if (insideButton(mh)) {
			KeyboardHandler kb = window.getKeyboardHandler();
			if (kb.isKeyPressed(GLFW.GLFW_KEY_LEFT)) {
				kb.ignoreKeyUntilRelease(GLFW.GLFW_KEY_LEFT);
				onLeft.onAction();
			} else if (kb.isKeyPressed(GLFW.GLFW_KEY_RIGHT)) {
				kb.ignoreKeyUntilRelease(GLFW.GLFW_KEY_RIGHT);
				onRight.onAction();
			}
		}
	}

	@Override
	public void dispose(Window window) {
		if (icon != -1)
			nvgDeleteImage(window.getNVGID(), icon);
		super.dispose(window);
	}

	public void reDraw(HWND hwnd, Window window) {
		this.hwnd = hwnd;
		if (icon != -1) {
			nvgDeleteImage(window.getNVGID(), icon);
			icon = -1;
		}
		if (icon == -1) {
			icon = Util.getIcon(hwnd, window);
			if (icon == -1)
				tryAgain = true;
		}
	}

	public void flash() {
		flash = true;
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

	public void setOnLeft(OnAction onLeft) {
		this.onLeft = onLeft;
	}

	public void setOnRight(OnAction onRight) {
		this.onRight = onRight;
	}

}
