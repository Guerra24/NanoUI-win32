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

import net.luxvacuos.nanoui.input.MouseHandler;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.nanoui.util.Maths;

public class Slider extends Component {

	private float pos, precision;
	private OnAction onPress;
	private boolean customPrecision, move;

	public Slider(float x, float y, float w, float h, float position) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.pos = position;
	}

	@Override
	public void update(float delta, Window window) {
		MouseHandler mh = window.getMouseHandler();
		if ((mh.isButtonPressed(0) && insideSlider(mh)) || move) {
			move = mh.isButtonPressed(0);
			pos = (mh.getX() - rootComponent.rootX - alignedX) / w;
			if (customPrecision)
				pos = (float) (Math.floor(pos * precision) / precision);
			pos = Maths.clamp(pos, 0, 1);
			if (onPress != null)
				onPress.onAction();
		}
		super.update(delta, window);
	}

	@Override
	public void render(Window window) {
		Theme.renderSlider(window.getNVGID(), pos, rootComponent.rootX + alignedX,
				window.getHeight() - rootComponent.rootY - alignedY - h, w, h);
	}

	public boolean insideSlider(MouseHandler mh) {
		return mh.getX() > rootComponent.rootX + alignedX - 6 && mh.getY() > rootComponent.rootY + alignedY
				&& mh.getX() < rootComponent.rootX + alignedX + w + 6
				&& mh.getY() < rootComponent.rootY + alignedY + h;
	}

	public void setOnPress(OnAction onPress) {
		this.onPress = onPress;
	}

	public void useCustomPrecision(boolean customPrecision) {
		this.customPrecision = customPrecision;
	}

	public void setPrecision(float precision) {
		this.precision = precision;
	}

	public float getPosition() {
		return pos;
	}

}
