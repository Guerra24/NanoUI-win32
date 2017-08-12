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
import static org.lwjgl.system.windows.User32.WM_KILLFOCUS;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.WindowProc;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.INT_PTR;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.SIZE;
import com.sun.jna.ptr.IntByReference;

import net.luxvacuos.nanoui.core.AppUI;
import net.luxvacuos.nanoui.core.TaskManager;
import net.luxvacuos.nanoui.core.states.AbstractState;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowHandle;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowManager;
import net.luxvacuos.nanoui.resources.ResourceLoader;
import net.luxvacuos.nanoui.ui.ComponentWindow;
import net.luxvacuos.nanoui.ui.Font;
import net.luxvacuos.win32.DWMapiExt;
import net.luxvacuos.win32.DWMapiExt.DWM_THUMBNAIL_PROPERTIES;
import net.luxvacuos.win32.DWMapiExt.DWM_TNP;
import net.luxvacuos.win32.User32Ext;
import net.luxvacuos.win32.User32Ext.Accent;
import net.luxvacuos.win32.User32Ext.AccentPolicy;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttribute;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttributeData;

public class WindowPreview extends AbstractState {

	private ComponentWindow compWin;
	private Window window;
	private WindowHandle handle;
	private HWND local;
	private HWND hwndWin;
	private Font segoeui, segoemdl2;
	private boolean run;

	private IntByReference thumbnail;

	public WindowPreview(Window window, WindowHandle handle) {
		super("_main");
		this.window = window;
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
		compWin.setBackgroundColor(0, 0, 0, 0);
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
		thumbnail = new IntByReference();
	}

	@Override
	public void dispose() {
		super.dispose();
		compWin.dispose(window);
		segoeui.dispose();
		segoemdl2.dispose();
		if (thumbnail.getValue() != 0)
			DWMapiExt.INSTANCE.DwmUnregisterThumbnail(new INT_PTR(thumbnail.getValue()));
	}

	@Override
	public void update(float delta) {
		compWin.update(delta, window);
		if (run) {
			if (thumbnail.getValue() != 0)
				DWMapiExt.INSTANCE.DwmUnregisterThumbnail(new INT_PTR(thumbnail.getValue()));
			DWMapiExt.INSTANCE.DwmRegisterThumbnail(local, hwndWin, thumbnail);

			SIZE size = new SIZE();
			DWMapiExt.INSTANCE.DwmQueryThumbnailSourceSize(new INT_PTR(thumbnail.getValue()), size);
			size.read();
			float aspectX = (float) size.cx / (float) size.cy;
			float aspectY = (float) size.cy / (float) size.cx;

			DWM_THUMBNAIL_PROPERTIES props = new DWM_THUMBNAIL_PROPERTIES();
			props.dwFlags = DWM_TNP.DWM_TNP_VISIBLE | DWM_TNP.DWM_TNP_RECTDESTINATION | DWM_TNP.DWM_TNP_OPACITY;

			props.fVisible = true;
			props.opacity = (byte) 0xFF;

			props.rcDestination = new RECT();
			if (size.cx > size.cy) {
				props.rcDestination.left = 5;
				props.rcDestination.top = 100 - (int) (190 / aspectX / 2);
				props.rcDestination.right = props.rcDestination.left + 190;
				props.rcDestination.bottom = props.rcDestination.top + (int) (190 / aspectX);
			} else {
				props.rcDestination.left = 100 - (int) (190 / aspectY / 2);
				props.rcDestination.top = 5;
				props.rcDestination.right = props.rcDestination.left + (int) (190 / aspectY);
				props.rcDestination.bottom = props.rcDestination.top + 190;
			}

			props.write();
			DWMapiExt.INSTANCE.DwmUpdateThumbnailProperties(new INT_PTR(thumbnail.getValue()), props);
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
