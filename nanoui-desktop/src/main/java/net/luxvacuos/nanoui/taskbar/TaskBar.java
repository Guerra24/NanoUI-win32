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
import static com.sun.jna.platform.win32.WinUser.MONITOR_DEFAULTTOPRIMARY;
import static com.sun.jna.platform.win32.WinUser.SW_HIDE;
import static com.sun.jna.platform.win32.WinUser.SW_MAXIMIZE;
import static com.sun.jna.platform.win32.WinUser.SW_RESTORE;
import static com.sun.jna.platform.win32.WinUser.SW_SHOW;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.WindowProc;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.HMONITOR;
import com.sun.jna.platform.win32.WinUser.MONITORINFO;
import com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;

import net.luxvacuos.nanoui.bootstrap.Bootstrap;
import net.luxvacuos.nanoui.core.App;
import net.luxvacuos.nanoui.core.AppUI;
import net.luxvacuos.nanoui.core.Variables;
import net.luxvacuos.nanoui.core.states.AbstractState;
import net.luxvacuos.nanoui.core.states.StateMachine;
import net.luxvacuos.nanoui.input.KeyboardHandler;
import net.luxvacuos.nanoui.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.nanoui.ui.Alignment;
import net.luxvacuos.nanoui.ui.Button;
import net.luxvacuos.nanoui.ui.Component;
import net.luxvacuos.nanoui.ui.ComponentWindow;
import net.luxvacuos.nanoui.ui.Container;
import net.luxvacuos.nanoui.ui.Direction;
import net.luxvacuos.nanoui.ui.FlowLayout;
import net.luxvacuos.win32.User32Ext;
import net.luxvacuos.win32.User32Ext.ARW;
import net.luxvacuos.win32.User32Ext.Accent;
import net.luxvacuos.win32.User32Ext.AccentPolicy;
import net.luxvacuos.win32.User32Ext.HSHELL;
import net.luxvacuos.win32.User32Ext.MINIMIZEDMETRICS;
import net.luxvacuos.win32.User32Ext.SPI;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttribute;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttributeData;

public class TaskBar extends AbstractState {

	private static final List<String> IGNORE_WINDOWS = new ArrayList<>();

	private Map<HWND, WindowButton> windows = new HashMap<>();

	private RECT old;
	private HWND taskbar;
	private ComponentWindow window;
	private int msgNotify;
	private Container tasks;

	protected TaskBar() {
		super("_main");
	}

	@Override
	public void init() {
		super.init();
		Theme.setTheme(new TaskBarTheme());

		IGNORE_WINDOWS.add("Program Manager");
		IGNORE_WINDOWS.add("Windows Shell Experience Host");
		IGNORE_WINDOWS.add("Date and Time Information");

		window = new ComponentWindow(AppUI.getMainWindow());
		window.getTitlebar().setEnabled(false);
		window.init(AppUI.getMainWindow());
		window.setBackgroundColor(0, 0, 0, 0);
		window.setLayout(new FlowLayout(Direction.RIGHT, 0, 0));

		long hwndGLFW = glfwGetWin32Window(AppUI.getMainWindow().getID());
		HWND hwnd = new HWND(new Pointer(hwndGLFW));

		MINIMIZEDMETRICS minMet = new MINIMIZEDMETRICS();
		minMet.cbSize = minMet.size();
		minMet.iWidth = 30;
		minMet.iHorzGap = 1;
		minMet.iVertGap = 1;
		minMet.iArrange = ARW.ARW_HIDE;
		minMet.write();

		User32Ext.INSTANCE.SystemParametersInfo(SPI.SPI_SETMINIMIZEDMETRICS, minMet.size(), minMet.getPointer(), 0);

		User32Ext.INSTANCE.SetTaskmanWindow(hwnd);
		User32Ext.INSTANCE.RegisterShellHookWindow(hwnd);
		User32Ext.INSTANCE.SetShellWindow(hwnd);
		msgNotify = User32Ext.INSTANCE.RegisterWindowMessage("SHELLHOOK");
		long dwp = User32Ext.INSTANCE.GetWindowLongPtr(hwnd, GWL_WNDPROC);

		WindowProc proc = new WindowProc() {
			@Override
			public long invoke(long hwnd, int uMsg, long wParam, long lParam) {
				if (uMsg == msgNotify) {
					HWND hwndD = new HWND(new Pointer(lParam));
					byte[] buffer = new byte[1024];
					User32Ext.INSTANCE.GetWindowTextA(hwndD, buffer, buffer.length);
					String title = Native.toString(buffer);
					int action = (int) wParam;
					System.out.println(action + " - " + lParam + ": " + title);
					switch ((int) wParam) {
					case HSHELL.HSHELL_WINDOWCREATED:
						if (User32.INSTANCE.IsWindowVisible(hwndD))
							if ((User32Ext.INSTANCE.GetWindowLongPtr(hwndD, GWL_EXSTYLE) & WS_EX_TOOLWINDOW) == 0)
								if (!title.isEmpty() && !IGNORE_WINDOWS.contains(title)) {
									WindowButton btn = new WindowButton(0, 0, 200, Variables.HEIGHT, title, hwndD);
									btn.setOnButtonPress(() -> {
										WINDOWPLACEMENT winpl = new WINDOWPLACEMENT();
										User32Ext.INSTANCE.GetWindowPlacement(hwndD, winpl);
										if (winpl.showCmd != SW_MAXIMIZE)
											User32.INSTANCE.ShowWindow(hwndD, SW_RESTORE);
										User32.INSTANCE.SetForegroundWindow(hwndD);
									});
									tasks.addComponent(btn);
									windows.put(hwndD, btn);
								}
						break;
					case HSHELL.HSHELL_WINDOWDESTROYED:
						WindowButton btnD = windows.get(hwndD);
						if (btnD != null)
							tasks.removeComponent(windows.remove(hwndD));
						break;
					case HSHELL.HSHELL_WINDOWACTIVATED:
						for (Component comp : tasks.getComponents()) {
							WindowButton wb = (WindowButton) comp;
							wb.active = false;
						}
						WindowButton btnA = windows.get(hwndD);
						if (btnA != null)
							btnA.active = true;
						break;
					case HSHELL.HSHELL_REDRAW:
						WindowButton btnR = windows.get(hwndD);
						if (btnR != null)
							btnR.setText(title);
						break;
					}
				}
				return JNI.callPPPP(dwp, hwnd, uMsg, wParam, lParam);
			}
		};
		User32Ext.INSTANCE.SetWindowLongPtr(hwnd, GWL_WNDPROC, proc.address());
		User32Ext.INSTANCE.SetWindowLongPtr(hwnd, GWL_EXSTYLE, WS_EX_TOOLWINDOW);
		
		AccentPolicy accent = new AccentPolicy();
		accent.AccentState = Accent.ACCENT_ENABLE_BLURBEHIND;
		accent.GradientColor = 0xC87F7F7F;
		accent.AccentFlags = 2;
		int accentStructSize = accent.size();
		accent.write();
		Pointer accentPtr = accent.getPointer();

		WindowCompositionAttributeData data = new WindowCompositionAttributeData();
		data.Attribute = WindowCompositionAttribute.WCA_ACCENT_POLICY;
		data.SizeOfData = accentStructSize;
		data.Data = accentPtr;

		User32Ext.INSTANCE.SetWindowCompositionAttribute(hwnd, data);

		HMONITOR monitor = User32.INSTANCE.MonitorFromWindow(hwnd, MONITOR_DEFAULTTOPRIMARY);
		MONITORINFO info = new MONITORINFO();
		User32.INSTANCE.GetMonitorInfo(monitor, info);

		taskbar = User32.INSTANCE.FindWindow("Shell_TrayWnd", null);

		if (taskbar != null) {
			User32.INSTANCE.ShowWindow(taskbar, SW_HIDE);
		}

		old = new RECT();
		old.top = info.rcWork.top;
		old.bottom = info.rcWork.bottom;
		old.right = info.rcWork.right;
		old.left = info.rcWork.left;

		RECT rc = new RECT();
		rc.top = info.rcWork.top;
		rc.bottom = info.rcWork.bottom;
		rc.right = info.rcWork.right;
		rc.left = info.rcWork.left;
		User32Ext.INSTANCE.SystemParametersInfo(SPI.SPI_SETWORKAREA, 0, rc.getPointer(), 0);

		Container startBtns = new Container(0, 0, 120, Variables.HEIGHT);
		startBtns.setLayout(new FlowLayout(Direction.RIGHT, 0, 0));

		Button start = new Button(0, 0, Variables.HEIGHT, Variables.HEIGHT, "");
		start.setPreicon(Theme.uniToString(0xF17a));
		start.setEntypo("Entypo");
		start.setOnButtonPress(() -> {
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYUP, 0);
		});

		Button search = new Button(0, 0, Variables.HEIGHT, Variables.HEIGHT, "");
		search.setPreicon(Theme.ICON_SEARCH);
		search.setOnButtonPress(() -> {
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_S, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYUP, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_S, 0, User32Ext.KEYEVENTF_KEYUP, 0);
		});

		Button taskview = new Button(0, 0, Variables.HEIGHT, Variables.HEIGHT, "");
		taskview.setPreicon(Theme.ICON_MULTITASK);
		taskview.setPreiconSize(24);
		taskview.setOnButtonPress(() -> {
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_TAB, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYUP, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_TAB, 0, User32Ext.KEYEVENTF_KEYUP, 0);
		});

		startBtns.addComponent(start);
		startBtns.addComponent(search);
		startBtns.addComponent(taskview);

		tasks = new Container(0, 0, Variables.WIDTH - 320, Variables.HEIGHT);
		tasks.setLayout(new FlowLayout(Direction.RIGHT, 0, 0));
		User32Ext.INSTANCE.EnumWindows(new WNDENUMPROC() {
			@Override
			public boolean callback(HWND hwndD, Pointer arg1) {
				if (User32.INSTANCE.IsWindowVisible(hwndD)) {
					byte[] buffer = new byte[1024];
					User32Ext.INSTANCE.GetWindowTextA(hwndD, buffer, buffer.length);
					String title = Native.toString(buffer);
					if ((User32Ext.INSTANCE.GetWindowLongPtr(hwndD, GWL_EXSTYLE) & WS_EX_TOOLWINDOW) == 0)
						if (!title.isEmpty() && !IGNORE_WINDOWS.contains(title)) {
							WindowButton btn = new WindowButton(0, 0, 200, Variables.HEIGHT, title, hwndD);
							btn.setOnButtonPress(() -> {
								WINDOWPLACEMENT winpl = new WINDOWPLACEMENT();
								User32Ext.INSTANCE.GetWindowPlacement(hwndD, winpl);
								if (winpl.showCmd != SW_MAXIMIZE)
									User32.INSTANCE.ShowWindow(hwndD, SW_RESTORE);
								User32.INSTANCE.SetForegroundWindow(hwndD);
							});
							tasks.addComponent(btn);
							windows.put(hwndD, btn);
						}
				}
				return true;
			}
		}, null);

		Container rightBtns = new Container(0, 0, 200, Variables.HEIGHT);
		rightBtns.setLayout(new FlowLayout(Direction.LEFT, 0, 0));

		Button minimizeAll = new Button(0, 0, 5, Variables.HEIGHT, "");
		minimizeAll.setWindowAlignment(Alignment.RIGHT_BOTTOM);
		minimizeAll.setAlignment(Alignment.LEFT_TOP);
		minimizeAll.setOnButtonPress(() -> {
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_D, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYUP, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_D, 0, User32Ext.KEYEVENTF_KEYUP, 0);
		});
		rightBtns.addComponent(minimizeAll);
		Button actionCenter = new Button(0, 0, Variables.HEIGHT, Variables.HEIGHT, "");
		actionCenter.setWindowAlignment(Alignment.RIGHT_BOTTOM);
		actionCenter.setAlignment(Alignment.LEFT_TOP);
		actionCenter.setPreicon(Theme.ICON_ACTION_CENTER);
		actionCenter.setOnButtonPress(() -> {
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_A, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYUP, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_A, 0, User32Ext.KEYEVENTF_KEYUP, 0);
		});
		rightBtns.addComponent(actionCenter);
		ClockButton clock = new ClockButton(0, 0, 78, Variables.HEIGHT);
		clock.setWindowAlignment(Alignment.RIGHT_BOTTOM);
		clock.setAlignment(Alignment.LEFT_TOP);
		clock.setOnButtonPress(() -> {
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_MENU, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_D, 0, User32Ext.KEYEVENTF_KEYDOWN, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_LWIN, 0, User32Ext.KEYEVENTF_KEYUP, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_MENU, 0, User32Ext.KEYEVENTF_KEYUP, 0);
			User32Ext.INSTANCE.keybd_event(User32Ext.VK_D, 0, User32Ext.KEYEVENTF_KEYUP, 0);
		});

		rightBtns.addComponent(clock);

		window.addComponent(startBtns);
		window.addComponent(tasks);
		window.addComponent(rightBtns);
		AppUI.getMainWindow().setVisible(true);
		System.gc();
	}

	@Override
	public void dispose() {
		super.dispose();
		window.dispose(AppUI.getMainWindow());
		User32Ext.INSTANCE.SystemParametersInfo(SPI.SPI_SETWORKAREA, 0, old.getPointer(), 0);
		long hwndGLFW = glfwGetWin32Window(AppUI.getMainWindow().getID());
		HWND hwnd = new HWND(new Pointer(hwndGLFW));
		User32Ext.INSTANCE.DeregisterShellHookWindow(hwnd);
		if (taskbar != null) {
			User32.INSTANCE.ShowWindow(taskbar, SW_SHOW);
		}
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
		Variables.WIDTH = vidmode.width();
		Variables.HEIGHT = 40;
		Variables.X = 0;
		Variables.Y = vidmode.height() - 40;
		Variables.ALWAYS_ON_TOP = true;
		Variables.DECORATED = false;
		new App(new TaskBar());
	}

}
