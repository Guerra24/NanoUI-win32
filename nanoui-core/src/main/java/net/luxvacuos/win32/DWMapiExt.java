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
import com.sun.jna.platform.win32.WinDef.BOOLByReference;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.INT_PTR;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.platform.win32.WinUser.SIZE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface DWMapiExt extends StdCallLibrary {

	public static final DWMapiExt INSTANCE = Native.loadLibrary("dwmapi", DWMapiExt.class,
			W32APIOptions.DEFAULT_OPTIONS);

	public class MARGINS extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("cxLeftWidth", "cxRightWidth", "cyTopHeight",
				"cyBottomHeight");

		public int cxLeftWidth;
		public int cxRightWidth;
		public int cyTopHeight;
		public int cyBottomHeight;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	public class WINDOWPOS extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("hwnd", "hwndInsertAfter", "x", "y", "cx", "cy",
				"flags");

		public WINDOWPOS(Pointer pointer) {
			super(pointer);
			read();
		}

		public HWND hwnd;
		public HWND hwndInsertAfter;
		public int x;
		public int y;
		public int cx;
		public int cy;
		public int flags;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	public class NCCALCSIZE_PARAMS extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("rgrc", "lppos");

		public RECT[] rgrc = new RECT[3];
		public WINDOWPOS lppos;

		public NCCALCSIZE_PARAMS(Pointer pointer) {
			super(pointer);
			read();
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	public class PSIZE extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("x", "y");
		public int x;
		public int y;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public class DWM_THUMBNAIL_PROPERTIES extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("dwFlags", "rcDestination", "rcSource", "opacity",
				"fVisible", "fSourceClientAreaOnly");
		public int dwFlags;
		public RECT rcDestination;
		public RECT rcSource;
		public byte opacity;
		public boolean fVisible;
		public boolean fSourceClientAreaOnly;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public interface DWM_TNP {
		public static final int DWM_TNP_RECTDESTINATION = 0x1;
		public static final int DWM_TNP_RECTSOURCE = 0x2;
		public static final int DWM_TNP_OPACITY = 0x4;
		public static final int DWM_TNP_VISIBLE = 0x8;
		public static final int DWM_TNP_SOURCECLIENTAREAONLY = 0x10;
	}

	public HRESULT DwmExtendFrameIntoClientArea(HWND window, MARGINS margins);

	public HRESULT DwmRegisterThumbnail(HWND hwndDestination, HWND hwndSource, IntByReference phThumbnailId);

	public HRESULT DwmUnregisterThumbnail(INT_PTR hThumbnailId);

	public HRESULT DwmQueryThumbnailSourceSize(INT_PTR hThumbnail, SIZE size);

	public HRESULT DwmUpdateThumbnailProperties(INT_PTR hThumbnailId, DWM_THUMBNAIL_PROPERTIES ptnProperties);
	
	public HRESULT DwmGetColorizationColor(DWORDByReference color, BOOLByReference pfOpaqueBlend);
	
	// Aero Peek - functions not available in Win10... 
	public int InvokeAeroPeek(boolean EM, HWND hwndWin, HWND local, long pT, INT_PTR hPN0, int x3244);
	
	public int DwmpActivateLivePreview(int par1, HWND hWnd, HWND c, int d);

	
}
