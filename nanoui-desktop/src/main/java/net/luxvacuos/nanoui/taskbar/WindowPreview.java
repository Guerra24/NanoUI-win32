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

import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

import net.luxvacuos.nanoui.bootstrap.Bootstrap;
import net.luxvacuos.nanoui.core.App;
import net.luxvacuos.nanoui.core.AppUI;
import net.luxvacuos.nanoui.core.Variables;
import net.luxvacuos.nanoui.core.states.AbstractState;
import net.luxvacuos.nanoui.core.states.StateMachine;
import net.luxvacuos.nanoui.input.KeyboardHandler;
import net.luxvacuos.nanoui.ui.ComponentWindow;
import net.luxvacuos.win32.DWMapiExt;
import net.luxvacuos.win32.DWMapiExt.DWM_THUMBNAIL_PROPERTIES;
import net.luxvacuos.win32.DWMapiExt.DWM_TNP;
import net.luxvacuos.win32.DWMapiExt.PSIZE;
import net.luxvacuos.win32.User32Ext;
import net.luxvacuos.win32.User32Ext.Accent;
import net.luxvacuos.win32.User32Ext.AccentPolicy;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttribute;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttributeData;

public class WindowPreview extends AbstractState {

	private ComponentWindow window;

	private long thumbnail;

	public WindowPreview() {
		super("_main");
	}

	@Override
	public void init() {
		super.init();

		window = new ComponentWindow(AppUI.getMainWindow());
		window.setBackgroundColor(0, 0, 0, 0);

		long hwndGLFW = glfwGetWin32Window(AppUI.getMainWindow().getID());
		HWND hwnd = new HWND(new Pointer(hwndGLFW));

		AccentPolicy accent = new AccentPolicy();
		accent.AccentState = Accent.ACCENT_ENABLE_BLURBEHIND;
		accent.GradientColor = 0xC8000000;
		accent.AccentFlags = 2;
		int accentStructSize = accent.size();
		accent.write();
		Pointer accentPtr = accent.getPointer();

		WindowCompositionAttributeData data = new WindowCompositionAttributeData();
		data.Attribute = WindowCompositionAttribute.WCA_ACCENT_POLICY;
		data.SizeOfData = accentStructSize;
		data.Data = accentPtr;

		User32Ext.INSTANCE.SetWindowCompositionAttribute(hwnd, data);
/*		DWMapiExt.INSTANCE.DwmRegisterThumbnail(hwnd,window, thumbnail);

		PSIZE size = new PSIZE();
		DWMapiExt.INSTANCE.DwmQueryThumbnailSourceSize(thumbnail, size);

		DWM_THUMBNAIL_PROPERTIES props = new DWM_THUMBNAIL_PROPERTIES();
		props.dwFlags = DWM_TNP.DWM_TNP_VISIBLE | DWM_TNP.DWM_TNP_RECTDESTINATION | DWM_TNP.DWM_TNP_OPACITY;

		props.fVisible = true;
		props.opacity = (byte) 0xFF;

		props.rcDestination = new RECT();
		props.rcDestination.left = 0;
		props.rcDestination.top = 0;
		props.rcDestination.right = 200;
		props.rcDestination.bottom = 200;
		if (size.x < 200)
			props.rcDestination.right = props.rcDestination.left + size.x;
		if (size.y < 200)
			props.rcDestination.bottom = props.rcDestination.top + size.y;

		DWMapiExt.INSTANCE.DwmUpdateThumbnailProperties(thumbnail, props);*/
	}

	@Override
	public void dispose() {
		super.dispose();
		window.dispose(AppUI.getMainWindow());
		DWMapiExt.INSTANCE.DwmUnregisterThumbnail(thumbnail);
	}

	@Override
	public void update(float delta) {
		window.update(delta, AppUI.getMainWindow());
		KeyboardHandler kbh = AppUI.getMainWindow().getKeyboardHandler();
		if (kbh.isShiftPressed() && kbh.isKeyPressed(GLFW.GLFW_KEY_ESCAPE))
			StateMachine.stop();
	}

	@Override
	public void render(float alpha) {
		AppUI.clearBuffer(GL11.GL_COLOR_BUFFER_BIT);
		AppUI.clearColors(0f, 0f, 0f, 0);
		window.render(AppUI.getMainWindow());
	}

	public static void main(String[] args) {
		new Bootstrap(args);
		GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		Variables.WIDTH = 200;
		Variables.HEIGHT = 200;
		Variables.X = 400;
		Variables.Y = vidmode.height() - 240;
		Variables.ALWAYS_ON_TOP = false;
		Variables.DECORATED = true;
		new App(new WindowPreview());
	}

}
