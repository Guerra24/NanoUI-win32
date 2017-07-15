/*
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

package net.luxvacuos.nanoui.core.states;

import static com.sun.jna.platform.win32.User32.INSTANCE;
import static com.sun.jna.platform.win32.WinUser.GWL_EXSTYLE;
import static com.sun.jna.platform.win32.WinUser.GWL_WNDPROC;
import static com.sun.jna.platform.win32.WinUser.MONITOR_DEFAULTTOPRIMARY;
import static com.sun.jna.platform.win32.WinUser.SW_HIDE;
import static com.sun.jna.platform.win32.WinUser.SW_MAXIMIZE;
import static com.sun.jna.platform.win32.WinUser.SW_RESTORE;
import static com.sun.jna.platform.win32.WinUser.SW_SHOW;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.windows.User32.GetWindowLongPtr;
import static org.lwjgl.system.windows.User32.SWP_FRAMECHANGED;
import static org.lwjgl.system.windows.User32.SWP_NOMOVE;
import static org.lwjgl.system.windows.User32.SWP_NOSIZE;
import static org.lwjgl.system.windows.User32.SWP_NOZORDER;
import static org.lwjgl.system.windows.User32.SetWindowLongPtr;
import static org.lwjgl.system.windows.User32.SetWindowPos;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.WindowProc;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
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
import net.luxvacuos.nanoui.input.KeyboardHandler;
import net.luxvacuos.nanoui.ui.Alignment;
import net.luxvacuos.nanoui.ui.Button;
import net.luxvacuos.nanoui.ui.Component;
import net.luxvacuos.nanoui.ui.ComponentWindow;
import net.luxvacuos.nanoui.ui.Container;
import net.luxvacuos.nanoui.ui.Direction;
import net.luxvacuos.nanoui.ui.FlowLayout;
import net.luxvacuos.nanoui.ui.WindowButton;
import net.luxvacuos.win32.DWMapi.MARGINS;
import net.luxvacuos.win32.User32;
import net.luxvacuos.win32.User32.AccentPolicy;
import net.luxvacuos.win32.User32.AccentState;
import net.luxvacuos.win32.User32.SPI;
import net.luxvacuos.win32.User32.HSHELL;
import net.luxvacuos.win32.User32.WindowCompositionAttribute;
import net.luxvacuos.win32.User32.WindowCompositionAttributeData;
import net.luxvacuos.win32.User32.WindowInfo;

public class TaskBar extends AbstractState {

	private static final List<String> IGNORE_WINDOWS = new ArrayList<>();

	private RECT old;
	private HWND taskbar;
	private ComponentWindow window;
	private int msgNotify;
	private Container tasks;

	public TaskBar() {
		super("internal_demo");
	}

	@Override
	public void init() {
		super.init();
		IGNORE_WINDOWS.add("Program Manager");
		IGNORE_WINDOWS.add("Windows Shell Experience Host");

		window = new ComponentWindow(AppUI.getMainWindow());
		window.setBackgroundColor(0, 0, 0, 0);
		window.setLayout(new FlowLayout(Direction.RIGHT, 0, 0));

		long hwndGLFW = glfwGetWin32Window(AppUI.getMainWindow().getID());
		HWND hwnd = new HWND(new Pointer(hwndGLFW));

		msgNotify = User32.INSTANCE.RegisterWindowMessageA(new WString("SHELLHOOK"));
		User32.INSTANCE.RegisterShellHookWindow(hwnd);

		long dwp = GetWindowLongPtr(hwndGLFW, GWL_WNDPROC);

		WindowProc proc = new WindowProc() {
			@Override
			public long invoke(long hwnd, int uMsg, long wParam, long lParam) {
				if (uMsg == 49195) {
					HWND hwndD = new HWND(new Pointer(lParam));
					byte[] buffer = new byte[1024];
					User32.INSTANCE.GetWindowTextA(hwndD, buffer, buffer.length);
					String title = Native.toString(buffer);
					int action = (int) wParam;
					System.out.println(action + " - " + lParam + ": " + title);
					switch ((int) wParam) {
					case HSHELL.HSHELL_WINDOWCREATED:
						WindowButton btn = new WindowButton(0, 0, 200, Variables.HEIGHT, title, hwndD);
						btn.setOnButtonPress(() -> {
							WINDOWPLACEMENT winpl = new WINDOWPLACEMENT();
							User32.INSTANCE.GetWindowPlacement(hwndD, winpl);
							if (winpl.showCmd != SW_MAXIMIZE)
								INSTANCE.ShowWindow(hwndD, SW_RESTORE);
							INSTANCE.SetForegroundWindow(hwndD);
						});
						tasks.addComponent(btn);
						break;
					case HSHELL.HSHELL_WINDOWDESTROYED:
						List<Component> tasksL = new ArrayList<>(tasks.getComponents());
						for (Component comp : tasksL) {
							WindowButton wb = (WindowButton) comp;
							if (wb.getHwnd().getPointer().equals(hwndD.getPointer()))
								tasks.removeComponent(comp);
						}
						break;
					case HSHELL.HSHELL_WINDOWACTIVATED:
						break;
					case HSHELL.HSHELL_REDRAW:
						for (Component comp : tasks.getComponents()) {
							WindowButton wb = (WindowButton) comp;
							if (wb.getHwnd().getPointer().equals(hwndD.getPointer()))
								wb.setText(title);
						}
						break;
					}
				}
				return JNI.callPPPP(dwp, hwnd, uMsg, wParam, lParam);
			}
		};
		SetWindowLongPtr(hwndGLFW, GWL_WNDPROC, proc.address());

		SetWindowLongPtr(hwndGLFW, GWL_EXSTYLE, WS_EX_TOOLWINDOW);

		MARGINS margins = new MARGINS();

		margins.cxLeftWidth = 8;
		margins.cxRightWidth = 8;
		margins.cyBottomHeight = 8;
		margins.cyTopHeight = 27;

		// DWMapi.INSTANCE.DwmExtendFrameIntoClientArea(aeroFrameHWND, margins);

		SetWindowPos(hwndGLFW, 0, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);

		AccentPolicy accent = new AccentPolicy();
		accent.AccentState = AccentState.ACCENT_ENABLE_BLURBEHIND;
		accent.GradientColor = 0x7F7F7F7F;
		accent.AccentFlags = 2;
		int accentStructSize = accent.size();
		accent.write();
		Pointer accentPtr = accent.getPointer();

		WindowCompositionAttributeData data = new WindowCompositionAttributeData();
		data.Attribute = WindowCompositionAttribute.WCA_ACCENT_POLICY;
		data.SizeOfData = accentStructSize;
		data.Data = accentPtr;

		User32.INSTANCE.SetWindowCompositionAttribute(hwnd, data);

		HMONITOR monitor = INSTANCE.MonitorFromWindow(hwnd, MONITOR_DEFAULTTOPRIMARY);
		MONITORINFO info = new MONITORINFO();
		INSTANCE.GetMonitorInfo(monitor, info);

		taskbar = INSTANCE.FindWindow("Shell_TrayWnd", null);

		if (taskbar != null) {
			INSTANCE.ShowWindow(taskbar, SW_HIDE);
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
		User32.INSTANCE.SystemParametersInfo(SPI.SPI_SETWORKAREA, 0, rc, 0);

		Container startBtns = new Container(0, 0, 200, Variables.HEIGHT);
		startBtns.setLayout(new FlowLayout(Direction.RIGHT, 0, 0));

		Button start = new Button(0, 0, Variables.HEIGHT, Variables.HEIGHT, "");
		start.setOnButtonPress(() -> {
			User32.INSTANCE.keybd_event(User32.VK_LWIN, 0, User32.KEYEVENTF_KEYDOWN, 0);
			User32.INSTANCE.keybd_event(User32.VK_LWIN, 0, User32.KEYEVENTF_KEYUP, 0);

			/*
			 * HWND hStartButtonWnd = INSTANCE.GetWindow(taskbar, new DWORD(GW_CHILD));
			 * User32.INSTANCE.SendMessage(hStartButtonWnd, WM_LBUTTONDOWN, new
			 * WPARAM(MK_LBUTTON), new LPARAM(0));
			 * User32.INSTANCE.SendMessage(hStartButtonWnd, WM_LBUTTONUP, new
			 * WPARAM(MK_LBUTTON), new LPARAM(0));
			 */
		});

		Button search = new Button(0, 0, Variables.HEIGHT, Variables.HEIGHT, "");
		search.setOnButtonPress(() -> {
			User32.INSTANCE.keybd_event(User32.VK_LWIN, 0, User32.KEYEVENTF_KEYDOWN, 0);
			User32.INSTANCE.keybd_event(User32.VK_S, 0, User32.KEYEVENTF_KEYDOWN, 0);
			User32.INSTANCE.keybd_event(User32.VK_LWIN, 0, User32.KEYEVENTF_KEYUP, 0);
			User32.INSTANCE.keybd_event(User32.VK_S, 0, User32.KEYEVENTF_KEYUP, 0);
		});

		Button taskview = new Button(0, 0, Variables.HEIGHT, Variables.HEIGHT, "");
		taskview.setOnButtonPress(() -> {
			User32.INSTANCE.keybd_event(User32.VK_LWIN, 0, User32.KEYEVENTF_KEYDOWN, 0);
			User32.INSTANCE.keybd_event(User32.VK_TAB, 0, User32.KEYEVENTF_KEYDOWN, 0);
			User32.INSTANCE.keybd_event(User32.VK_LWIN, 0, User32.KEYEVENTF_KEYUP, 0);
			User32.INSTANCE.keybd_event(User32.VK_TAB, 0, User32.KEYEVENTF_KEYUP, 0);
		});

		startBtns.addComponent(start);
		startBtns.addComponent(search);
		startBtns.addComponent(taskview);

		final List<WindowInfo> inflList = new ArrayList<>();

		User32.INSTANCE.EnumWindows(new WNDENUMPROC() {
			@Override
			public boolean callback(HWND hWnd, Pointer arg1) {
				if (INSTANCE.IsWindowVisible(hWnd)) {
					RECT r = new RECT();
					INSTANCE.GetWindowRect(hWnd, r);
					byte[] buffer = new byte[1024];
					User32.INSTANCE.GetWindowTextA(hWnd, buffer, buffer.length);
					String title = Native.toString(buffer);
					if (!title.isEmpty() && !IGNORE_WINDOWS.contains(title)) {
						inflList.add(new WindowInfo(hWnd, r, title));
					}
				}
				return true;
			}
		}, null);
		tasks = new Container(0, 0, Variables.WIDTH - 300, Variables.HEIGHT);
		tasks.setLayout(new FlowLayout(Direction.RIGHT, 0, 0));
		for (WindowInfo w : inflList) {
			WindowButton btn = new WindowButton(0, 0, 200, Variables.HEIGHT, w.getTitle(), w.getHWND());
			btn.setOnButtonPress(() -> {
				WINDOWPLACEMENT winpl = new WINDOWPLACEMENT();
				User32.INSTANCE.GetWindowPlacement(w.getHWND(), winpl);
				if (winpl.showCmd != SW_MAXIMIZE)
					INSTANCE.ShowWindow(w.getHWND(), SW_RESTORE);
				INSTANCE.SetForegroundWindow(w.getHWND());
			});
			tasks.addComponent(btn);
			System.out.println(w);
		}

		Container rightBtns = new Container(0, 0, 100, Variables.HEIGHT);
		rightBtns.setLayout(new FlowLayout(Direction.LEFT, 0, 0));

		Button actionCenter = new Button(0, 0, Variables.HEIGHT, Variables.HEIGHT, "");
		actionCenter.setWindowAlignment(Alignment.RIGHT_BOTTOM);
		actionCenter.setAlignment(Alignment.LEFT_TOP);
		actionCenter.setOnButtonPress(() -> {
			User32.INSTANCE.keybd_event(User32.VK_LWIN, 0, User32.KEYEVENTF_KEYDOWN, 0);
			User32.INSTANCE.keybd_event(User32.VK_A, 0, User32.KEYEVENTF_KEYDOWN, 0);
			User32.INSTANCE.keybd_event(User32.VK_LWIN, 0, User32.KEYEVENTF_KEYUP, 0);
			User32.INSTANCE.keybd_event(User32.VK_A, 0, User32.KEYEVENTF_KEYUP, 0);
		});
		rightBtns.addComponent(actionCenter);

		window.addComponent(startBtns);
		window.addComponent(tasks);
		window.addComponent(rightBtns);
	}

	@Override
	public void dispose() {
		super.dispose();
		window.dispose(AppUI.getMainWindow());
		User32.INSTANCE.SystemParametersInfo(SPI.SPI_SETWORKAREA, 0, old, 0);
		long hwndGLFW = glfwGetWin32Window(AppUI.getMainWindow().getID());
		HWND hwnd = new HWND(new Pointer(hwndGLFW));
		User32.INSTANCE.DeregisterShellHookWindow(hwnd);
		if (taskbar != null) {
			INSTANCE.ShowWindow(taskbar, SW_SHOW);
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
		Variables.WIDTH = 1440;
		Variables.HEIGHT = 40;
		new App(new TaskBar());
	}

}
