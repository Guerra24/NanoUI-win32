/*
 * 
 * Copyright (C) 2017 Lux Vacuos
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

package net.luxvacuos.win32;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT;
import com.sun.jna.platform.win32.WinUser.WindowProc;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.win32.W32APIOptions;

public interface User32 extends Library {

	public static final User32 INSTANCE = Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

	public interface AccentState {
		public static final int ACCENT_DISABLED = 0;
		public static final int ACCENT_ENABLE_GRADIENT = 1;
		public static final int ACCENT_ENABLE_TRANSPARENTGRADIENT = 2;
		public static final int ACCENT_ENABLE_BLURBEHIND = 3;
		public static final int ACCENT_INVALID_STATE = 4;
	}

	public interface SPI {
		public static final int SPI_SETWORKAREA = 0x002F;
		public static final int SPI_GETWORKAREA = 0x0030;
	}

	public interface HSHELL {
		public static final int HSHELL_WINDOWCREATED = 1;
		public static final int HSHELL_WINDOWDESTROYED = 2;
		public static final int HSHELL_ACTIVATESHELLWINDOW = 3;
		public static final int HSHELL_WINDOWACTIVATED = 4;
		public static final int HSHELL_GETMINRECT = 5;
		public static final int HSHELL_REDRAW = 6;
		public static final int HSHELL_TASKMAN = 7;
		public static final int HSHELL_LANGUAGE = 8;
		public static final int HSHELL_ACCESSIBILITYSTATE = 11;
		public static final int HSHELL_APPCOMMAND = 12;
	}

	public interface WindowCompositionAttribute {
		public static final int WCA_ACCENT_POLICY = 19;
	}

	public class AccentPolicy extends Structure implements Structure.ByReference {
		public int AccentState;
		public int AccentFlags;
		public int GradientColor;
		public int AnimationId;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("AccentState", "AccentFlags", "GradientColor", "AnimationId");
		}
	}

	public class WindowCompositionAttributeData extends Structure implements Structure.ByReference {
		public int Attribute;
		public Pointer Data;
		public int SizeOfData;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("Attribute", "Data", "SizeOfData");
		}
	}

	public class WindowInfo {
		HWND hwnd;
		RECT rect;
		String title;

		public WindowInfo(HWND hwnd, RECT rect, String title) {
			this.hwnd = hwnd;
			this.rect = rect;
			this.title = title;
		}

		public String toString() {
			return String.format("(%d,%d)-(%d,%d) : \"%s\"", rect.left, rect.top, rect.right, rect.bottom, title);
		}

		public String getTitle() {
			return title;
		}

		public HWND getHWND() {
			return hwnd;
		}
	}

	public static final int KEYEVENTF_KEYDOWN = 0;
	public static final int KEYEVENTF_EXTENDEDKEY = 1;
	public static final int KEYEVENTF_KEYUP = 2;

	public static final int VK_TAB = 0x09;
	public static final int VK_A = 0x41;
	public static final int VK_S = 0x53;
	public static final int VK_LWIN = 0x5B;

	public int RegisterWindowMessageA(WString lpString);

	public int DeregisterShellHookWindow(HWND hWnd);

	public boolean RegisterShellHookWindow(HWND hWnd);

	public HRESULT SetWindowCompositionAttribute(HWND hwnd, WindowCompositionAttributeData data);

	public boolean SystemParametersInfo(int uiAction, int uiParam, RECT pvParam, int fWinIni);

	boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer ptr);

	int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);

	public LRESULT SendMessage(HWND hwnd, int msg, WPARAM wParam, LPARAM lParam);

	public void keybd_event(int bVk, int bScan, int dwFlags, int dwExtraInfo);

	public boolean GetWindowPlacement(HWND hwnd, WINDOWPLACEMENT winpl);

	public long SetWindowLongPtr(HWND hWnd, int nIndex, WindowProc dwNewLong);

}
