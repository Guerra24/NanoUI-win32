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
import static com.sun.jna.platform.win32.WinUser.MOD_NOREPEAT;
import static com.sun.jna.platform.win32.WinUser.MOD_WIN;
import static com.sun.jna.platform.win32.WinUser.MONITOR_DEFAULTTOPRIMARY;
import static com.sun.jna.platform.win32.WinUser.SW_HIDE;
import static com.sun.jna.platform.win32.WinUser.SW_MAXIMIZE;
import static com.sun.jna.platform.win32.WinUser.SW_MINIMIZE;
import static com.sun.jna.platform.win32.WinUser.SW_RESTORE;
import static com.sun.jna.platform.win32.WinUser.SW_SHOW;
import static com.sun.jna.platform.win32.WinUser.WM_HOTKEY;
import static com.sun.jna.platform.win32.WinUser.WM_QUIT;
import static net.luxvacuos.win32.User32Ext.VK_E;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.windows.User32.WM_COPYDATA;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;

import java.io.IOException;
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
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HMONITOR;
import com.sun.jna.platform.win32.WinUser.MONITORINFO;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;

import net.luxvacuos.nanoui.bootstrap.Bootstrap;
import net.luxvacuos.nanoui.core.App;
import net.luxvacuos.nanoui.core.AppUI;
import net.luxvacuos.nanoui.core.Variables;
import net.luxvacuos.nanoui.core.states.AbstractState;
import net.luxvacuos.nanoui.core.states.StateMachine;
import net.luxvacuos.nanoui.input.KeyboardHandler;
import net.luxvacuos.nanoui.rendering.api.glfw.PixelBufferHandle;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowHandle;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowManager;
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
import net.luxvacuos.win32.User32Ext.COPYDATASTRUCT;
import net.luxvacuos.win32.User32Ext.HSHELL;
import net.luxvacuos.win32.User32Ext.MINIMIZEDMETRICS;
import net.luxvacuos.win32.User32Ext.NIM;
import net.luxvacuos.win32.User32Ext.NOTIFYICONDATA;
import net.luxvacuos.win32.User32Ext.SHELLHOOKINFO;
import net.luxvacuos.win32.User32Ext.SHELLTRAYDATA;
import net.luxvacuos.win32.User32Ext.SPI;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttribute;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttributeData;

public class TaskBar extends AbstractState {

	private static final List<String> IGNORE_WINDOWS = new ArrayList<>();
	private static final List<String> IGNORE_WINDOWS_UWP = new ArrayList<>();

	private Map<HWND, WindowButton> windows = new HashMap<>();

	private RECT old;
	private MINIMIZEDMETRICS oldMM;
	private ComponentWindow window;
	private int msgNotify;
	private Container tasks;
	private HWND local;

	private Background backgroundWindow;
	private ContextWindow contextWindow;
	private WindowPreview previewWindow;
	private NotificationsWindow notificationsWindow;

	private boolean running = true;

	private int keysThreadID;

	private HWND taskbar;

	private boolean noExplorer = false;
	private boolean printMessages = true;

	protected TaskBar() {
		super("_main");
	}

	@Override
	public void init() {
		super.init();

		Thread.currentThread().setName("Taskbar");
		Theme.setTheme(new TaskBarTheme());

		IGNORE_WINDOWS.add("Program Manager");
		IGNORE_WINDOWS.add("Windows Shell Experience Host");
		IGNORE_WINDOWS.add("Date and Time Information");
		IGNORE_WINDOWS.add("Windows Ink Workspace");
		IGNORE_WINDOWS.add("ShareX - Region capture");
		IGNORE_WINDOWS.add("ShareX - Screen recording");

		IGNORE_WINDOWS_UWP.add("Windows.UI.Core.CoreWindow");

		window = new ComponentWindow(AppUI.getMainWindow());
		window.getTitlebar().setEnabled(false);
		window.init(AppUI.getMainWindow());
		window.setBackgroundColor(0, 0, 0, 0);
		window.setLayout(new FlowLayout(Direction.RIGHT, 0, 0));

		long hwndGLFW = glfwGetWin32Window(AppUI.getMainWindow().getID());
		local = new HWND(Pointer.createConstant(hwndGLFW));

		if (noExplorer) {
			oldMM = new MINIMIZEDMETRICS();
			User32Ext.INSTANCE.SystemParametersInfo(SPI.SPI_GETMINIMIZEDMETRICS, oldMM.size(), oldMM.getPointer(), 0);
			oldMM.read();

			MINIMIZEDMETRICS minMet = new MINIMIZEDMETRICS();
			minMet.cbSize = minMet.size();
			minMet.iWidth = 30;
			minMet.iHorzGap = 1;
			minMet.iVertGap = 1;
			minMet.iArrange = ARW.ARW_HIDE;
			minMet.write();

			User32Ext.INSTANCE.SystemParametersInfo(SPI.SPI_SETMINIMIZEDMETRICS, minMet.size(), minMet.getPointer(), 0);
			User32Ext.INSTANCE.SetTaskmanWindow(local);
			User32Ext.INSTANCE.RegisterShellHookWindow(local);
			User32Ext.INSTANCE.SetShellWindow(local);
			msgNotify = User32Ext.INSTANCE.RegisterWindowMessage("SHELLHOOK");
		} else {
			User32Ext.INSTANCE.RegisterShellHookWindow(local);
			msgNotify = 49195;
		}
		long dwp = User32Ext.INSTANCE.GetWindowLongPtr(local, GWL_WNDPROC);

		WindowProc proc = new WindowProc() {
			@Override
			public long invoke(long hwnd, int uMsg, long wParam, long lParam) {
				if (uMsg == msgNotify) {
					HWND hwndD = new HWND(Pointer.createConstant(lParam));
					char[] buffer = new char[1024];
					User32Ext.INSTANCE.GetWindowTextW(hwndD, buffer, buffer.length);
					String title = Native.toString(buffer);
					int action = (int) wParam;
					if (printMessages)
						System.out.println(action + " - " + lParam + ": " + title);
					switch (action) {
					case HSHELL.HSHELL_WINDOWCREATED:
						if (User32.INSTANCE.IsWindowVisible(hwndD))
							if ((User32Ext.INSTANCE.GetWindowLongPtr(hwndD, GWL_EXSTYLE) & WS_EX_TOOLWINDOW) == 0) {
								char[] classNameC = new char[128];
								User32.INSTANCE.GetClassName(hwndD, classNameC, classNameC.length);
								String className = Native.toString(classNameC);
								if (!IGNORE_WINDOWS.contains(title) && !IGNORE_WINDOWS_UWP.contains(className)
										&& !windows.containsKey(hwndD)) {
									WindowButton btn = new WindowButton(0, 0, 200, Variables.HEIGHT, title, hwndD);
									btn.setOnButtonPress(() -> {
										if (btn.active) {
											User32.INSTANCE.SetForegroundWindow(hwndD);
											User32Ext.INSTANCE.ShowWindowAsync(hwndD, SW_MINIMIZE);
										} else {
											WINDOWPLACEMENT winpl = new WINDOWPLACEMENT();
											User32Ext.INSTANCE.GetWindowPlacement(hwndD, winpl);
											if (winpl.showCmd != SW_MAXIMIZE)
												User32Ext.INSTANCE.ShowWindowAsync(hwndD, SW_RESTORE);
											User32.INSTANCE.SetForegroundWindow(hwndD);
										}
									});
									btn.setOnButtonRightPress(() -> {
										GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
										contextWindow.getWindow().setPosition((int) btn.getX(), vidmode.height() - 240);
										contextWindow.setHwnd(hwndD);
									});
									btn.setOnHover(() -> {
										GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
										previewWindow.getWindow().setPosition((int) btn.getX(), vidmode.height() - 240);
										previewWindow.setHwnd(hwndD);
									});
									btn.reDraw(hwndD, AppUI.getMainWindow());
									tasks.addComponent(btn);
									windows.put(hwndD, btn);
								}
							}
						return 1;
					case HSHELL.HSHELL_WINDOWDESTROYED:
						WindowButton btnD = windows.get(hwndD);
						if (btnD != null)
							tasks.removeComponent(windows.remove(hwndD), AppUI.getMainWindow());
						return 1;
					case HSHELL.HSHELL_WINDOWACTIVATED:
						for (Component comp : tasks.getComponents()) {
							WindowButton wb = (WindowButton) comp;
							wb.active = false;
						}
						WindowButton btnA = windows.get(hwndD);
						if (btnA != null)
							btnA.active = true;
						return 1;
					case HSHELL.HSHELL_REDRAW:
						WindowButton btnR = windows.get(hwndD);
						if (btnR != null) {
							btnR.setText(title);
							btnR.reDraw(hwndD, AppUI.getMainWindow());
						}
						return 1;
					case HSHELL.HSHELL_GETMINRECT:
						GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
						SHELLHOOKINFO info = new SHELLHOOKINFO(new Pointer(lParam));
						WindowButton btnM = windows.get(info.hWnd);
						if (btnM != null) {
							info.rc.left = (short) (btnM.getX() + 80);
							info.rc.top = (short) (vidmode.height() - 40);
							info.rc.right = (short) (btnM.getX() + 120);
							info.rc.bottom = (short) (vidmode.height());
							info.write();
						}
						return 1;
					case HSHELL.HSHELL_WINDOWFULLSCREEN:
						if (!title.isEmpty() && !IGNORE_WINDOWS.contains(title)) {
							AppUI.getMainWindow().setVisible(false);
							WindowButton btnWF = windows.get(hwndD);
							if (btnWF != null)
								btnWF.truefullscreen = true;
						}
						return 1;
					case HSHELL.HSHELL_WINDOWNORMAL:
						if (!title.isEmpty() && !IGNORE_WINDOWS.contains(title)) {
							AppUI.getMainWindow().setVisible(true);
							WindowButton btnWF = windows.get(hwndD);
							if (btnWF != null)
								btnWF.truefullscreen = false;
						}
						return 1;
					case HSHELL.HSHELL_FLASH:
						WindowButton btnF = windows.get(hwndD);
						if (btnF != null) {
							btnF.flash();
						}
						return 1;
					}
				}
				switch (uMsg) {
				case WM_COPYDATA:
					COPYDATASTRUCT cpData = new COPYDATASTRUCT(new Pointer(lParam));
					if (cpData.dwData.intValue() == 1) {
						SHELLTRAYDATA trayData = new SHELLTRAYDATA(cpData.lpData);
						if (trayData.dwHz == 0x34753423) {
							NOTIFYICONDATA iconData = trayData.nicon_data;
							if (printMessages)
								System.out.println("NotifyIcon Code: " + trayData.dwMessage);
							switch (trayData.dwMessage) {
							case NIM.NIM_ADD:
								notificationsWindow.iconAdded(iconData);
								break;
							case NIM.NIM_MODIFY:
								notificationsWindow.iconModified(iconData);
								break;
							case NIM.NIM_DELETE:
								notificationsWindow.iconDeleted(iconData);
								break;
							}
						}
					}
					break;
				}
				return JNI.callPPPP(dwp, hwnd, uMsg, wParam, lParam);
			}
		};
		User32Ext.INSTANCE.SetWindowLongPtr(local, GWL_WNDPROC, proc.address());
		User32Ext.INSTANCE.SetWindowLongPtr(local, GWL_EXSTYLE, WS_EX_TOOLWINDOW);

		TrayHook.INSTANCE.Init();
		TrayHook.INSTANCE.RegisterSystemTrayHook(local);

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

		HMONITOR monitor = User32.INSTANCE.MonitorFromWindow(local, MONITOR_DEFAULTTOPRIMARY);
		MONITORINFO info = new MONITORINFO();
		User32.INSTANCE.GetMonitorInfo(monitor, info);
		if (!noExplorer) {
			taskbar = User32.INSTANCE.FindWindow("Shell_TrayWnd", null);

			if (taskbar != null) {
				User32.INSTANCE.ShowWindow(taskbar, SW_HIDE);
			}
		}

		old = new RECT();
		old.top = info.rcWork.top;
		old.bottom = info.rcWork.bottom;
		old.right = info.rcWork.right;
		old.left = info.rcWork.left;

		RECT rc = new RECT();
		rc.top = info.rcMonitor.top;
		rc.bottom = info.rcMonitor.bottom - 40;
		rc.right = info.rcMonitor.right;
		rc.left = info.rcMonitor.left;
		rc.write();
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

		tasks = new Container(0, 0, Variables.WIDTH - 267, Variables.HEIGHT);
		tasks.setLayout(new FlowLayout(Direction.RIGHT, 0, 0));
		User32Ext.INSTANCE.EnumWindows(new WNDENUMPROC() {
			@Override
			public boolean callback(HWND hwndD, Pointer arg1) {
				if (User32.INSTANCE.IsWindowVisible(hwndD)) {
					char[] buffer = new char[1024];
					User32Ext.INSTANCE.GetWindowTextW(hwndD, buffer, buffer.length);
					String title = Native.toString(buffer);
					if ((User32Ext.INSTANCE.GetWindowLongPtr(hwndD, GWL_EXSTYLE) & WS_EX_TOOLWINDOW) == 0) {
						char[] classNameC = new char[128];
						User32.INSTANCE.GetClassName(hwndD, classNameC, classNameC.length);
						String className = Native.toString(classNameC);
						if (!title.isEmpty() && !IGNORE_WINDOWS.contains(title)
								&& !IGNORE_WINDOWS_UWP.contains(className) && !windows.containsKey(hwndD)) {
							WindowButton btn = new WindowButton(0, 0, 200, Variables.HEIGHT, title, hwndD);
							btn.setOnButtonPress(() -> {
								if (btn.active) {
									User32.INSTANCE.SetForegroundWindow(hwndD);
									User32Ext.INSTANCE.ShowWindowAsync(hwndD, SW_MINIMIZE);
								} else {
									WINDOWPLACEMENT winpl = new WINDOWPLACEMENT();
									User32Ext.INSTANCE.GetWindowPlacement(hwndD, winpl);
									if (winpl.showCmd != SW_MAXIMIZE)
										User32Ext.INSTANCE.ShowWindowAsync(hwndD, SW_RESTORE);
									User32.INSTANCE.SetForegroundWindow(hwndD);
								}
							});
							btn.setOnButtonRightPress(() -> {
								GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
								contextWindow.setHwnd(hwndD);
								contextWindow.getWindow().setPosition((int) btn.getX(), vidmode.height() - 240);
							});
							btn.setOnHover(() -> {
								GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
								previewWindow.setHwnd(hwndD);
								previewWindow.getWindow().setPosition((int) btn.getX(), vidmode.height() - 240);
							});
							btn.reDraw(hwndD, AppUI.getMainWindow());
							tasks.addComponent(btn);
							windows.put(hwndD, btn);
						}
					}
				}
				return true;
			}
		}, null);

		Container rightBtns = new Container(0, 0, 147, Variables.HEIGHT);
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
		Button notificationsBtn = new Button(0, 0, 24, Variables.HEIGHT, "");
		notificationsBtn.setPreicon(Theme.ICON_CHEVRON_UP);
		notificationsBtn.setPreiconSize(14);
		notificationsBtn.setWindowAlignment(Alignment.RIGHT_BOTTOM);
		notificationsBtn.setAlignment(Alignment.LEFT_TOP);
		notificationsBtn.setOnButtonPress(() -> {
			GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
			notificationsWindow.getWindow().setVisible(true);
			notificationsWindow.getWindow().setPosition(
					(int) rightBtns.getAlignedX() + (int) notificationsBtn.getAlignedX() - 88, vidmode.height() - 240);
		});
		rightBtns.addComponent(notificationsBtn);

		window.addComponent(startBtns);
		window.addComponent(tasks);
		window.addComponent(rightBtns);

		AppUI.getMainWindow().setVisible(true);

		if (noExplorer) {
			createBackground();
			createHotKeys();
		}
		User32Ext.INSTANCE.SendNotifyMessage(WinUser.HWND_BROADCAST,
				User32Ext.INSTANCE.RegisterWindowMessage("TaskbarCreated"), new WPARAM(), new LPARAM());

		createContext();
		createPreview();
		createNotifications();

		System.gc();
	}

	private void createPreview() {
		Variables.X = -1000;
		Variables.Y = -1000;
		WindowHandle handle = WindowManager.generateHandle(200, 200, "");
		handle.isDecorated(false);
		handle.isVisible(false);
		PixelBufferHandle pb = new PixelBufferHandle();
		pb.setSrgbCapable(1);
		pb.setSamples(4);
		handle.setPixelBuffer(pb);
		Window prevWindow = WindowManager.generate(handle);

		Thread prevThr = new Thread(() -> {
			previewWindow = new WindowPreview(prevWindow, handle);
			previewWindow.init();
			float delta = 0;
			float accumulator = 0f;
			float interval = 1f / 30;
			float alpha = 0;
			int fps = 30;
			Window window = previewWindow.getWindow();
			while (running) {
				delta = window.getDelta();
				accumulator += delta;
				while (accumulator >= interval) {
					previewWindow.update(interval);
					accumulator -= interval;
				}
				alpha = accumulator / interval;
				if (window.isVisible())
					previewWindow.render(alpha);
				window.updateDisplay(fps);
			}
			previewWindow.dispose();
			window.dispose();
		});
		prevThr.setName("Preview Thread");
		prevThr.start();
	}

	private void createContext() {
		Variables.X = -1000;
		Variables.Y = -1000;
		WindowHandle handle = WindowManager.generateHandle(200, 200, "");
		handle.isDecorated(false);
		handle.isVisible(false);
		PixelBufferHandle pb = new PixelBufferHandle();
		pb.setSrgbCapable(1);
		pb.setSamples(4);
		handle.setPixelBuffer(pb);
		Window contxWindow = WindowManager.generate(handle);

		Thread contxThr = new Thread(() -> {
			contextWindow = new ContextWindow(contxWindow, handle);
			contextWindow.init();
			float delta = 0;
			float accumulator = 0f;
			float interval = 1f / 30;
			float alpha = 0;
			int fps = 30;
			Window window = contextWindow.getWindow();
			while (running) {
				delta = window.getDelta();
				accumulator += delta;
				while (accumulator >= interval) {
					contextWindow.update(interval);
					accumulator -= interval;
				}
				alpha = accumulator / interval;
				if (window.isVisible())
					contextWindow.render(alpha);
				window.updateDisplay(fps);
			}
			contextWindow.dispose();
			window.dispose();
		});
		contxThr.setName("Context Thread");
		contxThr.start();
	}

	private void createBackground() {
		Variables.X = 0;
		Variables.Y = 0;
		GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		WindowHandle handle = WindowManager.generateHandle(vidmode.width(), vidmode.height(), "");
		handle.isDecorated(false);
		handle.isVisible(false);
		PixelBufferHandle pb = new PixelBufferHandle();
		pb.setSrgbCapable(1);
		pb.setSamples(4);
		handle.setPixelBuffer(pb);
		Window backWindow = WindowManager.generate(handle);

		Thread backThr = new Thread(() -> {
			backgroundWindow = new Background(backWindow, handle);
			backgroundWindow.init();
			float delta = 0;
			float accumulator = 0f;
			float interval = 1f / 5;
			float alpha = 0;
			int fps = 5;
			Window window = backgroundWindow.getWindow();
			while (running) {
				delta = window.getDelta();
				accumulator += delta;
				while (accumulator >= interval) {
					backgroundWindow.update(interval);
					accumulator -= interval;
				}
				alpha = accumulator / interval;
				if (window.isVisible())
					backgroundWindow.render(alpha);
				window.updateDisplay(fps);
			}
			backgroundWindow.dispose();
			window.dispose();
		});
		backThr.start();
	}

	private void createNotifications() {
		Variables.X = -1000;
		Variables.Y = -1000;
		WindowHandle handle = WindowManager.generateHandle(200, 200, "");
		handle.isDecorated(false);
		handle.isVisible(false);
		PixelBufferHandle pb = new PixelBufferHandle();
		pb.setSrgbCapable(1);
		pb.setSamples(4);
		handle.setPixelBuffer(pb);
		Window notWindow = WindowManager.generate(handle);

		Thread notThr = new Thread(() -> {
			notificationsWindow = new NotificationsWindow(notWindow, handle);
			notificationsWindow.init();
			float delta = 0;
			float accumulator = 0f;
			float interval = 1f / 30;
			float alpha = 0;
			int fps = 30;
			Window window = notificationsWindow.getWindow();
			while (running) {
				delta = window.getDelta();
				accumulator += delta;
				while (accumulator >= interval) {
					notificationsWindow.update(interval);
					accumulator -= interval;
				}
				alpha = accumulator / interval;
				if (window.isVisible())
					notificationsWindow.render(alpha);
				window.updateDisplay(fps);
			}
			notificationsWindow.dispose();
			window.dispose();
		});
		notThr.setName("Notifications Thread");
		notThr.start();
	}

	private void createHotKeys() {
		Thread keys = new Thread(() -> {
			keysThreadID = Kernel32.INSTANCE.GetCurrentThreadId();
			User32.INSTANCE.RegisterHotKey(new HWND(Pointer.NULL), 1, MOD_WIN | MOD_NOREPEAT, VK_E);
			MSG msg = new MSG();
			while (User32.INSTANCE.GetMessage(msg, new HWND(Pointer.NULL), 0, 0) != 0 && running) {
				if (msg.message == WM_HOTKEY) {
					try {
						switch (msg.wParam.intValue()) {
						case 1:
							new ProcessBuilder("explorer.exe", ",").start();
							break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			User32.INSTANCE.UnregisterHotKey(Pointer.NULL, 1);
		});
		keys.start();
	}

	@Override
	public void dispose() {
		super.dispose();
		window.dispose(AppUI.getMainWindow());
		User32Ext.INSTANCE.SystemParametersInfo(SPI.SPI_SETWORKAREA, 0, old.getPointer(), 0);
		TrayHook.INSTANCE.UnregisterSystemTrayHook();
		User32Ext.INSTANCE.DeregisterShellHookWindow(local);
		if (noExplorer) {
			backgroundWindow.getWindow().closeDisplay();
			User32.INSTANCE.PostThreadMessage(keysThreadID, WM_QUIT, new WPARAM(), new LPARAM());
			User32Ext.INSTANCE.SystemParametersInfo(SPI.SPI_SETMINIMIZEDMETRICS, oldMM.size(), oldMM.getPointer(), 0);
		} else {
			if (taskbar != null)
				User32.INSTANCE.ShowWindow(taskbar, SW_SHOW);
		}
	}

	@Override
	public void update(float delta) {
		window.update(delta, AppUI.getMainWindow());
		KeyboardHandler kbh = AppUI.getMainWindow().getKeyboardHandler();
		if (kbh.isShiftPressed() && kbh.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
			running = false;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			StateMachine.stop();
		}
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
		Variables.TITLE = "Shell_TrayWnd";
		new App(new TaskBar());
	}

}
