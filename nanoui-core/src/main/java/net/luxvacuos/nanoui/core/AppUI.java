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

package net.luxvacuos.nanoui.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.luxvacuos.nanoui.input.Mouse;
import net.luxvacuos.nanoui.rendering.api.glfw.PixelBufferHandle;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowHandle;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowManager;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.NanoTheme;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.nanoui.resources.ResourceLoader;
import net.luxvacuos.nanoui.ui.Font;

public class AppUI {

	private static Window window;

	private Font poppinsRegular, poppinsLight, poppinsMedium, poppinsBold, poppinsSemiBold, entypo, segoeui, segoemdl2;

	public void init() {
		WindowHandle handle = WindowManager.generateHandle(Variables.WIDTH, Variables.HEIGHT, Variables.TITLE);
		handle.isDecorated(Variables.DECORATED);
		handle.isVisible(false);
		handle.alwaysOnTop(Variables.ALWAYS_ON_TOP);
		PixelBufferHandle pb = new PixelBufferHandle();
		pb.setSrgbCapable(1);
		pb.setSamples(4);
		handle.setPixelBuffer(pb);
		window = WindowManager.generate(handle);
		WindowManager.createWindow(handle, window, true);
		Mouse.setWindow(window);
		Theme.setTheme(new NanoTheme());

		ResourceLoader loader = window.getResourceLoader();
		poppinsRegular = loader.loadNVGFont("Poppins-Regular", "Poppins-Regular");
		poppinsLight = loader.loadNVGFont("Poppins-Light", "Poppins-Light");
		poppinsMedium = loader.loadNVGFont("Poppins-Medium", "Poppins-Medium");
		poppinsBold = loader.loadNVGFont("Poppins-Bold", "Poppins-Bold");
		poppinsSemiBold = loader.loadNVGFont("Poppins-SemiBold", "Poppins-SemiBold");
		entypo = loader.loadNVGFont("fontawesome-webfont", "Entypo", 40, false);
		segoeui = loader.loadNVGFont("C:\\Windows\\Fonts\\segoeui", "Segoe UI", true);
		segoemdl2 = loader.loadNVGFont("C:\\Windows\\Fonts\\segmdl2", "Segoe MDL2", true);
	}

	public void update(float delta) {
		WindowManager.update();
	}

	public void dispose() {
		poppinsRegular.dispose();
		poppinsLight.dispose();
		poppinsMedium.dispose();
		poppinsBold.dispose();
		poppinsSemiBold.dispose();
		entypo.dispose();
		segoeui.dispose();
		segoemdl2.dispose();
		WindowManager.closeAllDisplays();
		GLFW.glfwTerminate();
	}

	public static Window getMainWindow() {
		return window;
	}

	public static void clearColors(float r, float g, float b, float a) {
		GL11.glClearColor(r, g, b, a);
	}

	public static void clearBuffer(int values) {
		GL11.glClear(values);
	}

}
