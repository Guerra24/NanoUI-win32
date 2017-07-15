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

package net.luxvacuos.nanoui.bootstrap;

import net.luxvacuos.nanoui.core.App;
import net.luxvacuos.nanoui.core.Variables;

public class Bootstrap {

	public Bootstrap(String[] args) {
		parseArgs(args);
	}

	public void parseArgs(String[] args) {
		// Booleans to prevent setting a previously set value
		boolean gaveWidth = false, gaveHeight = false;
		// Iterate through array
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			// Check for window width
			case "-width":
				if (gaveWidth)
					throw new IllegalStateException("Width already given");
				// Convert and set the width
				Variables.WIDTH = Integer.parseInt(args[++i]);
				if (Variables.WIDTH <= 0)
					throw new IllegalArgumentException("Width must be positive");
				gaveWidth = true;
				break;
			// Check for height
			case "-height":
				if (gaveHeight)
					throw new IllegalStateException("Height already given");
				// Convert and set height
				Variables.HEIGHT = Integer.parseInt(args[++i]);
				if (Variables.HEIGHT <= 0)
					throw new IllegalArgumentException("Height must be positive");
				gaveHeight = true;
				break;
			default:
				// If there is an unknown arg throw exception
				if (args[i].startsWith("-")) {
					throw new IllegalArgumentException("Unknown argument: " + args[i].substring(1));
				} else {
					throw new IllegalArgumentException("Unknown token: " + args[i]);
				}
			}
		}
	}

}
