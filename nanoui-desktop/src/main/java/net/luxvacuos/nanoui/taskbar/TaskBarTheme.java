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

import static net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme.colorA;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_MIDDLE;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgFontFace;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.nanovg.NanoVG.nvgRect;
import static org.lwjgl.nanovg.NanoVG.nvgRestore;
import static org.lwjgl.nanovg.NanoVG.nvgSave;
import static org.lwjgl.nanovg.NanoVG.nvgScissor;
import static org.lwjgl.nanovg.NanoVG.nvgText;
import static org.lwjgl.nanovg.NanoVG.nvgTextAlign;
import static org.lwjgl.nanovg.NanoVG.nvgTextBounds;

import java.nio.FloatBuffer;

import net.luxvacuos.nanoui.rendering.api.nanovg.themes.NanoTheme;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;

public class TaskBarTheme extends NanoTheme {

	public TaskBarTheme() {
		buttonColor = Theme.rgba(255, 255, 255, 0);
		buttonHighlight = Theme.rgba(255, 255, 255, 40);
		buttonTextColor = Theme.rgba(255, 255, 255, 255);
	}

	@Override
	public void renderButton(long vg, String preicon, String text, String font, String entypo, float x, float y,
			float w, float h, boolean highlight, float fontSize, float preiconSize) {
		float tw, iw = 0;
		nvgSave(vg);

		nvgBeginPath(vg);
		nvgRect(vg, x, y, w, h);
		if (highlight)
			nvgFillColor(vg, buttonHighlight);
		else
			nvgFillColor(vg, buttonColor);
		nvgFill(vg);

		nvgFontSize(vg, fontSize);
		nvgFontFace(vg, font);
		tw = nvgTextBounds(vg, 0, 0, text, (FloatBuffer) null);
		if (preicon != null) {
			nvgFontSize(vg, preiconSize);
			nvgFontFace(vg, entypo);
			iw = nvgTextBounds(vg, 0, 0, preicon, (FloatBuffer) null);
			iw += h * 0.15f;
		}

		if (preicon != null) {
			nvgFontSize(vg, preiconSize);
			nvgFontFace(vg, entypo);
			nvgFillColor(vg, buttonTextColor);
			if (text.isEmpty()) {
				nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
				nvgText(vg, x + w * 0.5f, y + h * 0.5f, preicon);
			} else {
				nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE);
				nvgText(vg, x + h * 0.5f, y + h * 0.5f, preicon);
			}
		}

		nvgSave(vg);
		nvgScissor(vg, x, y, w, h);
		nvgFontSize(vg, fontSize);
		nvgFontFace(vg, font);
		nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE);
		nvgFillColor(vg, buttonTextColor);
		nvgText(vg, x + w * 0.5f - tw * 0.5f + iw * 0.25f, y + h * 0.5f, text);
		nvgRestore(vg);
		nvgRestore(vg);
	}

	@Override
	public void renderTaskbarWindowButton(long vg, String preicon, String text, String font, String entypo, float x,
			float y, float w, float h, boolean highlight, boolean active, float fontSize) {
		float tw, iw = 0;
		nvgSave(vg);

		nvgBeginPath(vg);
		nvgRect(vg, x, y, w, h);
		if (highlight || active)
			nvgFillColor(vg, buttonHighlight);
		else
			nvgFillColor(vg, buttonColor);
		nvgFill(vg);
		
		if (active) {
			nvgBeginPath(vg);
			nvgRect(vg, x, y + h - 3, w, 3);
			nvgFillColor(vg, Theme.rgba(255, 255, 255, 255, colorA));
			nvgFill(vg);
		}

		nvgFontSize(vg, fontSize);
		nvgFontFace(vg, font);
		tw = nvgTextBounds(vg, 0, 0, text, (FloatBuffer) null);
		if (preicon != null) {
			nvgFontSize(vg, h * 0.5f);
			nvgFontFace(vg, entypo);
			iw = nvgTextBounds(vg, 0, 0, preicon, (FloatBuffer) null);
			iw += h * 0.15f;
		}

		if (preicon != null) {
			nvgFontSize(vg, h * 0.5f);
			nvgFontFace(vg, entypo);
			nvgFillColor(vg, buttonTextColor);
			if (text.isEmpty()) {
				nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
				nvgText(vg, x + w * 0.5f, y + h * 0.5f, preicon);
			} else {
				nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE);
				nvgText(vg, x + w * 0.5f - tw * 0.5f - iw * 0.75f, y + h * 0.5f, preicon);
			}
		}

		nvgSave(vg);
		nvgScissor(vg, x + 5, y + 5, w - 10, h - 10);
		nvgFontSize(vg, fontSize);
		nvgFontFace(vg, font);
		nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE);
		nvgFillColor(vg, buttonTextColor);
		nvgText(vg, x + w * 0.5f - tw * 0.5f + iw * 0.25f, y + h * 0.5f, text);
		nvgRestore(vg);
		nvgRestore(vg);
	}

}
