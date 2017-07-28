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

import static com.sun.jna.platform.win32.WinUser.GWL_EXSTYLE;
import static com.sun.jna.platform.win32.WinUser.GWL_WNDPROC;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.nanovg.NanoVG.nvgDeleteImage;
import static org.lwjgl.system.windows.User32.HWND_BOTTOM;
import static org.lwjgl.system.windows.User32.SWP_NOACTIVATE;
import static org.lwjgl.system.windows.User32.SWP_NOMOVE;
import static org.lwjgl.system.windows.User32.SWP_NOSIZE;
import static org.lwjgl.system.windows.User32.SWP_NOZORDER;
import static org.lwjgl.system.windows.User32.WM_WINDOWPOSCHANGING;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.WindowProc;
import org.lwjgl.system.windows.WindowProcI;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import net.luxvacuos.nanoui.core.AppUI;
import net.luxvacuos.nanoui.core.Variables;
import net.luxvacuos.nanoui.core.states.AbstractState;
import net.luxvacuos.nanoui.rendering.api.glfw.PixelBufferHandle;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowHandle;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowManager;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.win32.DWMapiExt.WINDOWPOS;
import net.luxvacuos.win32.User32Ext;
import net.luxvacuos.win32.User32Ext.SPI;

public class Background extends AbstractState {

	private int wallpaper;
	private Window window;
	private WindowHandle handle;

	protected Background(Window backWindow, WindowHandle handle) {
		super("_main");
		this.window = backWindow;
		this.handle = handle;
	}

	@Override
	public void init() {
		super.init();
		WindowManager.createWindow(handle, window, true);

		long hwndGLFW = glfwGetWin32Window(window.getID());
		HWND hwnd = new HWND(new Pointer(hwndGLFW));
		window.setVisible(true);

		WindowProc proc = new WindowProc() {

			@Override
			public long invoke(long hw, int uMsg, long wParam, long lParam) {
				if (hw == hwndGLFW)
					switch (uMsg) {
					case WM_WINDOWPOSCHANGING:
						WINDOWPOS pos = new WINDOWPOS(new Pointer(lParam));
						pos.flags |= SWP_NOZORDER | SWP_NOACTIVATE;
						pos.write();
					}
				return org.lwjgl.system.windows.User32.DefWindowProc(hw, uMsg, wParam, lParam);
			}
		};
		User32.INSTANCE.SetWindowLongPtr(hwnd, GWL_WNDPROC, new Pointer(proc.address()));
		User32Ext.INSTANCE.SetWindowLongPtr(hwnd, GWL_EXSTYLE, WS_EX_TOOLWINDOW);

		User32.INSTANCE.SetWindowPos(hwnd, new HWND(new Pointer(HWND_BOTTOM)), 0, 0, 0, 0,
				SWP_NOMOVE | SWP_NOSIZE | SWP_NOACTIVATE);
		wallpaper = window.getResourceLoader().loadNVGTexture(getCurrentDesktopWallpaper(), true);
	}

	@Override
	public void dispose() {
		super.dispose();
		nvgDeleteImage(window.getNVGID(), wallpaper);
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void render(float alpha) {
		AppUI.clearBuffer(GL11.GL_COLOR_BUFFER_BIT);
		AppUI.clearColors(0f, 0f, 0f, 1);
		window.beingNVGFrame();
		Theme.renderBox(window.getNVGID(), 0, 0, window.getWidth(), window.getHeight(),
				Theme.setColor(1, 1, 1, 1, Theme.colorA), 0, 0, 0, 0);
		Theme.renderImage(window.getNVGID(), 0, 1, window.getWidth(), window.getHeight() - 1, wallpaper, 1);
		window.endNVGFrame();
	}

	public String getCurrentDesktopWallpaper() {
		char[] chars = new char[User32Ext.MAX_PATH];
		String currentWallpaper = new String(chars);
		Pointer m = new Memory(Native.WCHAR_SIZE * (currentWallpaper.length()));
		User32Ext.INSTANCE.SystemParametersInfo(SPI.SPI_GETDESKWALLPAPER, currentWallpaper.length(), m, 0);
		currentWallpaper = m.getWideString(0);
		return currentWallpaper;
	}

	public Window getWindow() {
		return window;
	}

}
