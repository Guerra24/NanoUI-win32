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

package net.luxvacuos.nanoui.test;

import static com.sun.jna.platform.win32.WinUser.GWL_WNDPROC;
import static com.sun.jna.platform.win32.WinUser.SW_MAXIMIZE;
import static com.sun.jna.platform.win32.WinUser.SWP_NOZORDER;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_BOTTOM;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.system.windows.User32.HTCAPTION;
import static org.lwjgl.system.windows.User32.HTTOP;
import static org.lwjgl.system.windows.User32.SWP_FRAMECHANGED;
import static org.lwjgl.system.windows.User32.SWP_NOMOVE;
import static org.lwjgl.system.windows.User32.SWP_NOSIZE;
import static org.lwjgl.system.windows.User32.WM_DWMCOLORIZATIONCOLORCHANGED;
import static org.lwjgl.system.windows.User32.WM_NCCALCSIZE;
import static org.lwjgl.system.windows.User32.WM_NCHITTEST;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.WindowProc;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.BOOLByReference;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT;

import net.luxvacuos.nanoui.bootstrap.Bootstrap;
import net.luxvacuos.nanoui.core.App;
import net.luxvacuos.nanoui.core.AppUI;
import net.luxvacuos.nanoui.core.Variables;
import net.luxvacuos.nanoui.core.states.AbstractState;
import net.luxvacuos.nanoui.core.states.StateMachine;
import net.luxvacuos.nanoui.input.KeyboardHandler;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.ui.Alignment;
import net.luxvacuos.nanoui.ui.Button;
import net.luxvacuos.nanoui.ui.ComponentWindow;
import net.luxvacuos.nanoui.ui.Container;
import net.luxvacuos.nanoui.ui.EditBox;
import net.luxvacuos.nanoui.ui.Image;
import net.luxvacuos.nanoui.ui.Text;
import net.luxvacuos.win32.DWMapiExt;
import net.luxvacuos.win32.DWMapiExt.MARGINS;
import net.luxvacuos.win32.DWMapiExt.NCCALCSIZE_PARAMS;
import net.luxvacuos.win32.Macros;
import net.luxvacuos.win32.User32Ext;

public class TestApp extends AbstractState {

	private ComponentWindow window;

	public TestApp() {
		super("_main");
	}

	@Override
	public void init() {
		super.init();

		window = new ComponentWindow(AppUI.getMainWindow());
		window.init(AppUI.getMainWindow());
		window.setBackgroundColor(0, 0, 0, 0f);

		long hwndGLFW = glfwGetWin32Window(AppUI.getMainWindow().getID());
		HWND hwnd = new HWND(Pointer.createConstant(hwndGLFW));

		long dwp = User32.INSTANCE.GetWindowLongPtr(hwnd, GWL_WNDPROC).longValue();
		WindowProc proc = new WindowProc() {

			@Override
			public long invoke(long hw, int uMsg, long wParam, long lParam) {
				switch (uMsg) {
				case WM_DWMCOLORIZATIONCOLORCHANGED:
					DWORD color = new DWORD(wParam);
					BOOL blend =  new BOOL(lParam);
					String col = Long.toHexString(color.longValue());
					String a = col.substring(0, 2);
					col = col.substring(2);
					window.getTitlebar().setColor("#" + col + "ff");
					break;
				case WM_NCHITTEST:
					RECT rect = new RECT();
					int x, y;
					User32.INSTANCE.GetWindowRect(hwnd, rect);
					x = Macros.GET_X_LPARAM(new LPARAM(lParam)) - 8;
					y = Macros.GET_Y_LPARAM(new LPARAM(lParam));
					if (y < rect.top + 6 && x >= rect.left && x <= rect.right - 16)
						return HTTOP;
					if (window.getTitlebar().isInside(AppUI.getMainWindow(), x - rect.left, y - rect.top))
						return HTCAPTION;
					else
						return JNI.callPPPP(dwp, hw, uMsg, wParam, lParam);
				case WM_NCCALCSIZE:
					if (wParam == 1) {
						NCCALCSIZE_PARAMS par = new NCCALCSIZE_PARAMS(new Pointer(lParam));

						WINDOWPLACEMENT winpl = new WINDOWPLACEMENT();
						User32Ext.INSTANCE.GetWindowPlacement(hwnd, winpl);

						if (winpl.showCmd != SW_MAXIMIZE) {
							par.rgrc[0].left += 8;
							par.rgrc[0].top += 0;
							par.rgrc[0].right -= 8;
							par.rgrc[0].bottom -= 8;
						} else {
							par.rgrc[0].left += 8;
							par.rgrc[0].top += 7;
							par.rgrc[0].right -= 8;
							par.rgrc[0].bottom -= 8;
						}
						par.write();
						return lParam;
					}
					break;
				}
				return JNI.callPPPP(dwp, hw, uMsg, wParam, lParam);
			}
		};
		User32.INSTANCE.SetWindowLongPtr(hwnd, GWL_WNDPROC, Pointer.createConstant(proc.address()));
		User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);

		DWORDByReference color = new DWORDByReference();
		BOOLByReference blend = new BOOLByReference();
		DWMapiExt.INSTANCE.DwmGetColorizationColor(color, blend);
		System.out.println(Long.toHexString(color.getValue().longValue()));
		System.out.println(blend.getValue().booleanValue());

		String col = Long.toHexString(color.getValue().longValue());
		String a = col.substring(0, 2);
		col = col.substring(2);
		window.getTitlebar().setColor("#" + col + "ff");

		MARGINS margins = new MARGINS();

		margins.cxLeftWidth = 0;
		margins.cxRightWidth = 0;
		margins.cyBottomHeight = 0;
		margins.cyTopHeight = 33;
		DWMapiExt.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, margins);

		Container center = new Container(0, 0, 200, 250);
		center.setAlignment(Alignment.CENTER);
		center.setWindowAlignment(Alignment.CENTER);

		Image logo = new Image(0, 0, 170.6666666666667f, 85.33333333333333f,
				AppUI.getMainWindow().getResourceLoader().loadNVGTexture("logo"), true);
		logo.setAlignment(Alignment.BOTTOM);
		logo.setWindowAlignment(Alignment.TOP);
		center.addComponent(logo);

		Text username = new Text("Username:", -90, 140);
		username.setAlign(NVG_ALIGN_LEFT | NVG_ALIGN_BOTTOM);
		username.setWindowAlignment(Alignment.BOTTOM);
		center.addComponent(username);

		EditBox user = new EditBox(0, 120, 180, 30, "");
		user.setAlignment(Alignment.CENTER);
		user.setWindowAlignment(Alignment.BOTTOM);
		center.addComponent(user);

		Text password = new Text("Password:", -90, 80);
		password.setAlign(NVG_ALIGN_LEFT | NVG_ALIGN_BOTTOM);
		password.setWindowAlignment(Alignment.BOTTOM);
		center.addComponent(password);

		EditBox pass = new EditBox(0, 60, 180, 30, "");
		pass.setAlignment(Alignment.CENTER);
		pass.setWindowAlignment(Alignment.BOTTOM);
		center.addComponent(pass);

		Button login = new Button(0, 20, 60, 30, "Login");
		login.setAlignment(Alignment.CENTER);
		login.setWindowAlignment(Alignment.BOTTOM);
		center.addComponent(login);

		window.addComponent(center);

		AppUI.getMainWindow().setVisible(true);
		AppUI.getMainWindow().setOnRefresh((windowID) -> {
			window.update(0, AppUI.getMainWindow());
			AppUI.clearBuffer(GL11.GL_COLOR_BUFFER_BIT);
			AppUI.clearColors(0f, 0f, 0f, 0);
			Window wind = AppUI.getMainWindow();
			window.render(wind);
		});
	}

	@Override
	public void dispose() {
		super.dispose();
		window.dispose(AppUI.getMainWindow());
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
		Window wind = AppUI.getMainWindow();
		window.render(wind);
	}

	public static void main(String[] args) {
		new Bootstrap(args);
		Variables.WIDTH = 800;
		Variables.HEIGHT = 600;
		Variables.X = 400;
		Variables.Y = 200;
		Variables.TITLE = "Test App";
		new App(new TestApp());
	}

}
