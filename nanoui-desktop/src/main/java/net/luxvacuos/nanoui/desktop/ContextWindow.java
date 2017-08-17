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

package net.luxvacuos.nanoui.desktop;

import static com.sun.jna.platform.win32.WinUser.GWL_EXSTYLE;
import static com.sun.jna.platform.win32.WinUser.GWL_WNDPROC;
import static com.sun.jna.platform.win32.WinUser.WM_CLOSE;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.windows.User32.WM_KILLFOCUS;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.WindowProc;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.WindowUtils;
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
import net.luxvacuos.nanoui.ui.Alignment;
import net.luxvacuos.nanoui.ui.Button;
import net.luxvacuos.nanoui.ui.ComponentWindow;
import net.luxvacuos.nanoui.ui.Container;
import net.luxvacuos.nanoui.ui.Direction;
import net.luxvacuos.nanoui.ui.FlowLayout;
import net.luxvacuos.nanoui.ui.Font;
import net.luxvacuos.nanoui.ui.Image;
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
	private HWND hwndWin;
	private HWND local;
	private boolean run;

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
		local = new HWND(Pointer.createConstant(hwndGLFW));

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

		User32Ext.INSTANCE.SetWindowCompositionAttribute(local, data);
		long dwp = User32Ext.INSTANCE.GetWindowLongPtr(local, GWL_WNDPROC);
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
		User32.INSTANCE.SetWindowLongPtr(local, GWL_WNDPROC, Pointer.createConstant(proc.address()));
		User32Ext.INSTANCE.SetWindowLongPtr(local, GWL_EXSTYLE, WS_EX_TOOLWINDOW);

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
		if (run) {
			compWin.dispose(window);
			Container bottomBtns = new Container(0, 0, 200, 60);

			char[] name = new char[1024];
			User32Ext.INSTANCE.GetWindowTextW(hwndWin, name, name.length);
			String title = Native.toString(name);

			Button btnClose = new Button(0, 0, 200, 30, "Close");
			btnClose.setPreicon(Theme.ICON_CHROME_CLOSE);
			btnClose.setPreiconSize(12);
			btnClose.setOnButtonPress(() -> {
				User32.INSTANCE.PostMessage(this.hwndWin, WM_CLOSE, new WPARAM(), new LPARAM());
				TaskManager.addTask(() -> window.setVisible(false));
			});
			Button btnOpen = new Button(0, 30, 200, 30, title);
			btnOpen.setOnButtonPress(() -> {
				try {
					new ProcessBuilder(WindowUtils.getProcessFilePath(hwndWin), "").start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				TaskManager.addTask(() -> window.setVisible(false));
			});
			bottomBtns.addComponent(btnClose);
			bottomBtns.addComponent(btnOpen);

			Image icon = new Image(13, 38, 16, 16, Util.getIcon(hwndWin, window), true);
			bottomBtns.addComponent(icon);
			compWin.addComponent(bottomBtns);

			Container tasks = new Container(-20, 0, 220, 140);
			tasks.setLayout(new FlowLayout(Direction.DOWN, 0, 10));
			for (int i = 0; i < 4; i++) {
				Button t = new Button(0, 0, 220, 30, "Task " + i);
				t.setWindowAlignment(Alignment.LEFT_TOP);
				t.setAlignment(Alignment.RIGHT_BOTTOM);
				tasks.addComponent(t);
			}
			compWin.addComponent(tasks);
			TaskManager.addTask(() -> window.setVisible(true));
			run = false;
		}

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

	public void setHwnd(HWND hwndWin) {
		this.hwndWin = hwndWin;
		run = true;
	}
}
