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

import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32Ext extends StdCallLibrary {

	public static final User32Ext INSTANCE = Native.loadLibrary("user32", User32Ext.class,
			W32APIOptions.DEFAULT_OPTIONS);

	public interface Accent {
		public static final int ACCENT_DISABLED = 0;
		public static final int ACCENT_ENABLE_GRADIENT = 1;
		public static final int ACCENT_ENABLE_TRANSPARENTGRADIENT = 2;
		public static final int ACCENT_ENABLE_BLURBEHIND = 3;
		public static final int ACCENT_INVALID_STATE = 4;
	}

	public interface SPI {
		public static final int SPI_SETWORKAREA = 0x002F;
		public static final int SPI_GETWORKAREA = 0x0030;
		public static final int SPI_GETMINIMIZEDMETRICS = 0x002B;
		public static final int SPI_SETMINIMIZEDMETRICS = 0x002C;
		public static final int SPI_GETDESKWALLPAPER = 0x73;
	}

	public interface HSHELL {
		public static final int HSHELL_WINDOWCREATED = 1;
		public static final int HSHELL_WINDOWDESTROYED = 2;
		public static final int HSHELL_ACTIVATESHELLWINDOW = 3;
		public static final int HSHELL_WINDOWACTIVATED = 32772;
		public static final int HSHELL_GETMINRECT = 5;
		public static final int HSHELL_REDRAW = 6;
		public static final int HSHELL_TASKMAN = 7;
		public static final int HSHELL_LANGUAGE = 8;
		public static final int HSHELL_ACCESSIBILITYSTATE = 11;
		public static final int HSHELL_APPCOMMAND = 12;
	}

	public interface ARW {
		public static final int ARW_BOTTOMLEFT = 0x0000;
		public static final int ARW_HIDE = 0x0008;
	}

	public interface WindowCompositionAttribute {
		public static final int WCA_ACCENT_POLICY = 19;
	}

	public interface NIM {
		public static final int NIM_ADD = 0x00000000;
		public static final int NIM_MODIFY = 0x00000001;
		public static final int NIM_DELETE = 0x00000002;
	}

	public class AccentPolicy extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("AccentState", "AccentFlags", "GradientColor",
				"AnimationId");
		public int AccentState;
		public int AccentFlags;
		public int GradientColor;
		public int AnimationId;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public class WindowCompositionAttributeData extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("Attribute", "Data", "SizeOfData");
		public int Attribute;
		public Pointer Data;
		public int SizeOfData;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public class MINIMIZEDMETRICS extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("cbSize", "iWidth", "iHorzGap", "iVertGap",
				"iArrange");
		public int cbSize;
		public int iWidth;
		public int iHorzGap;
		public int iVertGap;
		public int iArrange;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static final int MAX_PATH = 260;

	public static final int KEYEVENTF_KEYDOWN = 0;
	public static final int KEYEVENTF_EXTENDEDKEY = 1;
	public static final int KEYEVENTF_KEYUP = 2;

	public static final int VK_TAB = 0x09;
	public static final int VK_MENU = 0x12;
	public static final int VK_A = 0x41;
	public static final int VK_D = 0x44;
	public static final int VK_E = 0x45;
	public static final int VK_S = 0x53;
	public static final int VK_LWIN = 0x5B;

	public int RegisterWindowMessage(String lpString);

	public int DeregisterShellHookWindow(HWND hWnd);

	public boolean RegisterShellHookWindow(HWND hWnd);

	public HRESULT SetWindowCompositionAttribute(HWND hwnd, WindowCompositionAttributeData data);

	public boolean SystemParametersInfo(int uiAction, int uiParam, Pointer pvParam, int fWinIni);

	boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer ptr);

	int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);

	public LRESULT SendMessage(HWND hwnd, int msg, WPARAM wParam, LPARAM lParam);

	public void keybd_event(int bVk, int bScan, int dwFlags, int dwExtraInfo);

	public boolean GetWindowPlacement(HWND hwnd, WINDOWPLACEMENT winpl);

	public long GetWindowLongPtr(HWND hWnd, int nIndex);

	public long SetWindowLongPtr(HWND hWnd, int nIndex, long dwNewLong);

	public long GetClassLongPtr(HWND hwnd, int nIndex);

	public boolean ReleaseCapture();

	public boolean SetShellWindow(HWND hwnd);

	public boolean SetTaskmanWindow(HWND hwnd);

	public boolean SendNotifyMessage(HWND hwnd, int msg, WPARAM wParam, LPARAM lParam);

}
