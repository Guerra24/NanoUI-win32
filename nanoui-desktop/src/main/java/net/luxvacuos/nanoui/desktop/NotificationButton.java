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

package net.luxvacuos.nanoui.desktop;

import net.luxvacuos.nanoui.ui.Button;
import net.luxvacuos.win32.User32Ext.NOTIFYICONDATA;

public class NotificationButton extends Button{

	protected NOTIFYICONDATA iconData;
	
	public NotificationButton(float x, float y, float w, float h, String text, NOTIFYICONDATA iconData) {
		super(x, y, w, h, text);
		this.iconData = iconData;
	}
	
}
