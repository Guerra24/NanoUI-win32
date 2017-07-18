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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.W32APIOptions;

public interface DWMapiExt extends Library {

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
		public static final int DWM_TNP_VISIBLE = 0x8;
		public static final int DWM_TNP_OPACITY = 0x4;
		public static final int DWM_TNP_RECTDESTINATION = 0x1;
	}

	public HRESULT DwmExtendFrameIntoClientArea(HWND window, MARGINS margins);

	public HRESULT DwmRegisterThumbnail(HWND hwndDestination, HWND hwndSource, long phThumbnailId);

	public HRESULT DwmUnregisterThumbnail(long hThumbnailId);

	public HRESULT DwmQueryThumbnailSourceSize(long hThumbnail, PSIZE pSize);

	public HRESULT DwmUpdateThumbnailProperties(long hThumbnailId, DWM_THUMBNAIL_PROPERTIES ptnProperties);

}
