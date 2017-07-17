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

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Timer;

import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.nanoui.ui.Button;

public class ClockButton extends Button {

	private String clock = "", date = "";

	public ClockButton(float x, float y, float w, float h) {
		super(x, y, w, h, "");
		final DateFormat clockFormat = new SimpleDateFormat("h:mm a");
		final DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");
		int interval = 1000; // 1000

		new Timer(interval, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Date now = Calendar.getInstance().getTime();
				clock = clockFormat.format(now);
				date = dateFormat.format(now);
			}
		}).start();
		Date now = Calendar.getInstance().getTime();
		clock = clockFormat.format(now);
		date = dateFormat.format(now);
	}

	@Override
	public void render(Window window) {
		if (!enabled)
			return;
		super.render(window);
		Theme.renderText(window.getNVGID(), clock, font, NVG_ALIGN_CENTER | NVG_ALIGN_BOTTOM,
				rootComponent.rootX + alignedX + w / 2f,
				window.getHeight() - rootComponent.rootY - alignedY - h / 2f + 2f, fontSize,
				Theme.rgba(255, 255, 255, 255, Theme.colorA));
		Theme.renderText(window.getNVGID(), date, font, NVG_ALIGN_CENTER | NVG_ALIGN_TOP,
				rootComponent.rootX + alignedX + w / 2f,
				window.getHeight() - rootComponent.rootY - alignedY - h / 2f - 2f, fontSize,
				Theme.rgba(255, 255, 255, 255, Theme.colorA));
	}

}
