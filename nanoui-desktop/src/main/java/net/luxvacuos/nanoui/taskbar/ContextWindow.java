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
import static com.sun.jna.platform.win32.WinUser.WM_CLOSE;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.windows.User32.WM_KILLFOCUS;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.WindowProc;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;

import net.luxvacuos.nanoui.core.AppUI;
import net.luxvacuos.nanoui.core.TaskManager;
import net.luxvacuos.nanoui.core.states.AbstractState;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowHandle;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowManager;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.nanoui.resources.ResourceLoader;
import net.luxvacuos.nanoui.ui.Button;
import net.luxvacuos.nanoui.ui.ComponentWindow;
import net.luxvacuos.nanoui.ui.Direction;
import net.luxvacuos.nanoui.ui.FlowLayout;
import net.luxvacuos.nanoui.ui.Font;
import net.luxvacuos.win32.User32Ext;
import net.luxvacuos.win32.User32Ext.Accent;
import net.luxvacuos.win32.User32Ext.AccentPolicy;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttribute;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttributeData;

public class ContextWindow extends AbstractState {
	private Window window;
	private WindowHandle handle;
	private ComponentWindow compWin;
	private Font segoeui, segoemdl2;
	private HWND hwnd;

	protected ContextWindow(Window backWindow, WindowHandle handle) {
		super("_main");
		this.window = backWindow;
		this.handle = handle;
	}

	@Override
	public void init() {
		super.init();
		WindowManager.createWindow(handle, window, true);

		ResourceLoader loader = window.getResourceLoader();
		segoeui = loader.loadNVGFont("C:\\Windows\\Fonts\\segoeui", "Segoe UI", true);
		segoemdl2 = loader.loadNVGFont("C:\\Windows\\Fonts\\segmdl2", "Segoe MDL2", true);

		compWin = new ComponentWindow(window);
		compWin.init(window);
		compWin.setBackgroundColor(0, 0, 0, 0);
		compWin.setLayout(new FlowLayout(Direction.UP, 0, 0));
		compWin.getTitlebar().setEnabled(false);

		long hwndGLFW = glfwGetWin32Window(window.getID());
		HWND hwnd = new HWND(new Pointer(hwndGLFW));

		AccentPolicy accent = new AccentPolicy();
		accent.AccentState = Accent.ACCENT_ENABLE_BLURBEHIND;
		accent.GradientColor = 0xBE282828;
		accent.AccentFlags = 2;
		int accentStructSize = accent.size();
		accent.write();
		Pointer accentPtr = accent.getPointer();

		WindowCompositionAttributeData data = new WindowCompositionAttributeData();
		data.Attribute = WindowCompositionAttribute.WCA_ACCENT_POLICY;
		data.SizeOfData = accentStructSize;
		data.Data = accentPtr;

		User32Ext.INSTANCE.SetWindowCompositionAttribute(hwnd, data);
		long dwp = User32Ext.INSTANCE.GetWindowLongPtr(hwnd, GWL_WNDPROC);
		WindowProc proc = new WindowProc() {

			@Override
			public long invoke(long hw, int uMsg, long wParam, long lParam) {
				if (hw == hwndGLFW)
					switch (uMsg) {
					case WM_KILLFOCUS:
						TaskManager.addTask(() -> window.setVisible(false));
						break;
					}
				return JNI.callPPPP(dwp, hw, uMsg, wParam, lParam);
			}
		};
		User32.INSTANCE.SetWindowLongPtr(hwnd, GWL_WNDPROC, new Pointer(proc.address()));
		User32Ext.INSTANCE.SetWindowLongPtr(hwnd, GWL_EXSTYLE, WS_EX_TOOLWINDOW);

		Button btnClose = new Button(0, 0, 200, 30, "Close");
		btnClose.setPreicon(Theme.ICON_CHROME_CLOSE);
		btnClose.setPreiconSize(12);
		btnClose.setOnButtonPress(() -> {
			User32.INSTANCE.PostMessage(this.hwnd, WM_CLOSE, new WPARAM(), new LPARAM());
		});
		compWin.addComponent(btnClose);
	}

	@Override
	public void dispose() {
		super.dispose();
		compWin.dispose(window);
		segoeui.dispose();
		segoemdl2.dispose();
	}

	@Override
	public void update(float delta) {
		compWin.update(delta, window);
	}

	@Override
	public void render(float alpha) {
		AppUI.clearBuffer(GL11.GL_COLOR_BUFFER_BIT);
		AppUI.clearColors(0f, 0f, 0f, 0);
		compWin.render(window);
	}

	public Window getWindow() {
		return window;
	}
	
	public void setHwnd(HWND hwnd) {
		this.hwnd = hwnd;
	}
}
