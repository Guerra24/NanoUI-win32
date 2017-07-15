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

import java.awt.RenderingHints.Key;
import java.util.List;

import net.luxvacuos.nanoui.input.Mouse;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.nanoui.util.Maths;

public class DropDown<E> extends Button {

	private E value;
	private List<E> objects;
	private OnAction action;

	public DropDown(float x, float y, float w, float h, E val, List<E> objects) {
		super(x, y, w, h, val.toString());
		this.objects = objects;
	}

	@Override
	public void init() {
		super.setOnButtonPress(() -> {/*
			TaskManager.addTask(() -> GraphicalSubsystem.getWindowManager().addWindow(new ComponentWindow(
					rootComponent.rootX + alignedX, rootComponent.rootY - alignedY, w, 300, "test") {

				@Override
				public void initApp(Window window) {
					super.toggleTitleBar();
					super.setDecorations(false);
					super.setBackgroundColor(0, 0, 0, 0);

					ScrollArea area = new ScrollArea(0, 0, w, h, 0, 0);
					area.setLayout(new FlowLayout(Direction.DOWN, 0, 0));
					area.setResizeH(true);
					area.setResizeV(true);
					float hh = 0;
					for (E e : objects) {
						ContextMenuButton btn = new ContextMenuButton(0, 0,
								w - (float) REGISTRY
										.getRegistryItem(new Key("/Light Engine/Settings/WindowManager/scrollBarSize")),
								30, e.toString());
						btn.setWindowAlignment(Alignment.LEFT_TOP);
						btn.setAlignment(Alignment.RIGHT_BOTTOM);
						btn.setOnButtonPress(() -> {
							value = e;
							text = e.toString();
							super.closeWindow();
							if (action != null)
								action.onAction();
						});
						area.addComponent(btn);
						hh += 30;
					}
					h = Maths.min(hh, 200);
					super.addComponent(area);

					super.initApp(window);
				}

				@Override
				public void alwaysUpdateApp(float delta, Window window) {
					if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && !insideWindow())
						super.closeWindow();
					super.alwaysUpdateApp(delta, window);
				}
			}));*/

		});
		super.init();
	}

	@Override
	public void render(Window window) {
		if (!enabled)
			return;
		Theme.renderDropDownButton(window.getNVGID(), rootComponent.rootX + alignedX,
				window.getHeight() - rootComponent.rootY - alignedY - h, w, h, fontSize, font, entypo, text, inside);
	}

	@Override
	public void setOnButtonPress(OnAction onPress) {
		this.action = onPress;
	}

	public E getValue() {
		return value;
	}

}
