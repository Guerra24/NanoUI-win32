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

import static com.sun.jna.platform.win32.WinUser.GCL_HICON;
import static com.sun.jna.platform.win32.WinUser.GCL_HICONSM;
import static com.sun.jna.platform.win32.WinUser.ICON_BIG;
import static com.sun.jna.platform.win32.WinUser.ICON_SMALL;
import static com.sun.jna.platform.win32.WinUser.ICON_SMALL2;
import static com.sun.jna.platform.win32.WinUser.WM_GETICON;
import static org.lwjgl.nanovg.NanoVG.nvgCreateImageMem;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.lwjgl.BufferUtils;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFOHEADER;
import com.sun.jna.platform.win32.WinGDI.ICONINFO;

import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.win32.User32Ext;

public class Util {
	
	
	public static int getIcon(HWND hwnd, Window window) {
		File exe = new File(WindowUtils.getProcessFilePath(hwnd));
		BufferedImage image = null;
		try {
			sun.awt.shell.ShellFolder sf = sun.awt.shell.ShellFolder.getShellFolder(exe);
			image = (BufferedImage) sf.getIcon(true);
		} catch (FileNotFoundException e1) {
		}
		if (image == null)
			if (exe.exists()) {
				Icon icon = FileSystemView.getFileSystemView().getSystemIcon(exe);
				if (icon instanceof ImageIcon) {
					image = (BufferedImage) ((ImageIcon) icon).getImage();
				}
			}

		if (image == null) {
			long iconHandle = User32Ext.INSTANCE.SendMessage(hwnd, WM_GETICON, new WPARAM(ICON_SMALL2), new LPARAM())
					.longValue();
			if (iconHandle == 0)
				iconHandle = User32Ext.INSTANCE.SendMessage(hwnd, WM_GETICON, new WPARAM(ICON_SMALL), new LPARAM())
						.longValue();
			if (iconHandle == 0)
				iconHandle = User32Ext.INSTANCE.SendMessage(hwnd, WM_GETICON, new WPARAM(ICON_BIG), new LPARAM())
						.longValue();
			if (iconHandle == 0)
				iconHandle = User32Ext.INSTANCE.GetClassLongPtr(hwnd, GCL_HICON);
			if (iconHandle == 0)
				iconHandle = User32Ext.INSTANCE.GetClassLongPtr(hwnd, GCL_HICONSM);

			final HICON hIcon = new HICON(new Pointer(iconHandle));
			final Dimension iconSize = WindowUtils.getIconSize(hIcon);
			if (iconSize.width == 0 || iconSize.height == 0)
				return 0;

			final int width = iconSize.width;
			final int height = iconSize.height;
			final short depth = 24;

			final byte[] lpBitsColor = new byte[width * height * depth / 8];
			final Pointer lpBitsColorPtr = new Memory(lpBitsColor.length);
			final byte[] lpBitsMask = new byte[width * height * depth / 8];
			final Pointer lpBitsMaskPtr = new Memory(lpBitsMask.length);
			final BITMAPINFO bitmapInfo = new BITMAPINFO();
			final BITMAPINFOHEADER hdr = new BITMAPINFOHEADER();

			bitmapInfo.bmiHeader = hdr;
			hdr.biWidth = width;
			hdr.biHeight = height;
			hdr.biPlanes = 1;
			hdr.biBitCount = depth;
			hdr.biCompression = 0;
			hdr.write();
			bitmapInfo.write();

			final HDC hDC = User32.INSTANCE.GetDC(null);
			final ICONINFO iconInfo = new ICONINFO();
			User32.INSTANCE.GetIconInfo(hIcon, iconInfo);
			iconInfo.read();
			GDI32.INSTANCE.GetDIBits(hDC, iconInfo.hbmColor, 0, height, lpBitsColorPtr, bitmapInfo, 0);
			lpBitsColorPtr.read(0, lpBitsColor, 0, lpBitsColor.length);
			GDI32.INSTANCE.GetDIBits(hDC, iconInfo.hbmMask, 0, height, lpBitsMaskPtr, bitmapInfo, 0);
			lpBitsMaskPtr.read(0, lpBitsMask, 0, lpBitsMask.length);
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

			int r, g, b, a, argb;
			int x = 0, y = height - 1;
			for (int i = 0; i < lpBitsColor.length; i = i + 3) {
				b = lpBitsColor[i] & 0xFF;
				g = lpBitsColor[i + 1] & 0xFF;
				r = lpBitsColor[i + 2] & 0xFF;
				a = 0xFF - lpBitsMask[i] & 0xFF;
				argb = (a << 24) | (r << 16) | (g << 8) | b;
				image.setRGB(x, y, argb);
				x = (x + 1) % width;
				if (x == 0)
					y--;
			}
			User32.INSTANCE.ReleaseDC(null, hDC);
		}
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteBuffer buffer;
		buffer = BufferUtils.createByteBuffer(8 * 1024);
		InputStream source = new ByteArrayInputStream(os.toByteArray());
		try {
			ReadableByteChannel rbc = Channels.newChannel(source);
			while (true) {
				int bytes = rbc.read(buffer);
				if (bytes == -1)
					break;
				if (buffer.remaining() == 0)
					buffer = resizeBuffer(buffer, buffer.capacity() * 2);
			}
			rbc.close();
			source.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		buffer.put((byte) 0);
		buffer.flip();
		return nvgCreateImageMem(window.getNVGID(), 0, buffer);

	}

	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}
}
