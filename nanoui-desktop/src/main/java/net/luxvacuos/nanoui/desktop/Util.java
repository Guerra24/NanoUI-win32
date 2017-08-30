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

import static com.sun.jna.platform.win32.WinNT.PROCESS_QUERY_LIMITED_INFORMATION;
import static com.sun.jna.platform.win32.WinUser.GCLP_HICON;
import static com.sun.jna.platform.win32.WinUser.GCLP_HICONSM;
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
import java.io.FileFilter;
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

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.lwjgl.BufferUtils;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.UINTByReference;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFOHEADER;
import com.sun.jna.platform.win32.WinGDI.ICONINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;

import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.win32.Kernel32Ext;
import net.luxvacuos.win32.User32Ext;

public class Util {

	public static String getAppUserModelId(HWND hwnd) {
		IntByReference processID = new IntByReference();
		User32.INSTANCE.GetWindowThreadProcessId(hwnd, processID);
		HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION, false, processID.getValue());
		UINTByReference length = new UINTByReference();
		Kernel32Ext.INSTANCE.GetApplicationUserModelId(hProcess, length, null);
		char[] modelID = new char[length.getValue().intValue()];
		Kernel32Ext.INSTANCE.GetApplicationUserModelId(hProcess, length, modelID);
		return new String(Native.toString(modelID));
	}

	public static int getIcon(HWND hwnd, Window window) {
		BufferedImage image = null;
		HWND[] ret = new HWND[1];
		char[] classNameC = new char[128];
		User32.INSTANCE.GetClassName(hwnd, classNameC, classNameC.length);
		String className = Native.toString(classNameC);
		if (className.equals("ApplicationFrameWindow")) {
			User32.INSTANCE.EnumChildWindows(hwnd, new WNDENUMPROC() {
				@Override
				public boolean callback(HWND hWndC, Pointer data) {
					char[] classNameC = new char[128];
					User32.INSTANCE.GetClassName(hWndC, classNameC, classNameC.length);
					String className = Native.toString(classNameC);
					if (className.equals("Windows.UI.Core.CoreWindow")) {
						ret[0] = hWndC;
						return false;
					}
					return true;
				}
			}, null);
			if (ret[0] == null)
				return -1;
			else
				hwnd = ret[0];
			try {
				File appExe = new File(WindowUtils.getProcessFilePath(hwnd));
				SAXBuilder builder = new SAXBuilder();
				Document document = builder.build(new File(appExe.getParent() + "\\AppxManifest.xml"));
				Element rootNode = document.getRootElement();
				Element applicationsNode = rootNode.getChild("Applications",
						Namespace.getNamespace("http://schemas.microsoft.com/appx/manifest/foundation/windows10"));
				for (Element application : applicationsNode.getChildren()) {
					if (application.getAttributeValue("Executable").equals(appExe.getName())) {
						Element visualElements = application.getChild("VisualElements",
								Namespace.getNamespace("http://schemas.microsoft.com/appx/manifest/uap/windows10"));
						String logoFilePath = visualElements.getAttributeValue("Square44x44Logo");
						logoFilePath = logoFilePath.replaceAll("/", "\\\\");
						String[] logoSplitBack = logoFilePath.split("\\\\");
						for (int i = 1; i < logoSplitBack.length - 1; i++) {
							logoSplitBack[0] += "\\\\" + logoSplitBack[i];
						}
						String[] logoFilePathSplit = logoSplitBack[logoSplitBack.length - 1].split("\\.");
						logoFilePath = logoFilePathSplit[0];
						logoFilePath += ".targetsize-24*" + "." + logoFilePathSplit[1];
						File assetsFolder = new File(appExe.getParent() + "\\" + logoSplitBack[0]);
						FileFilter fileFilter = new WildcardFileFilter(logoFilePath);
						File[] files = assetsFolder.listFiles(fileFilter);
						if (files.length > 0)
							return window.getResourceLoader().loadNVGTexture(files[0].getPath(), true);
						else {
							fileFilter = new WildcardFileFilter(logoFilePath.toLowerCase());
							files = assetsFolder.listFiles(fileFilter);
							if (files.length > 0)
								return window.getResourceLoader().loadNVGTexture(files[0].getPath(), true);

							logoFilePath = logoFilePathSplit[0];
							logoFilePath += ".scale-200" + "." + logoFilePathSplit[1];
							File logoFile = new File(
									appExe.getParent() + "\\" + logoSplitBack[0] + "\\" + logoFilePath);
							if (logoFile.exists())
								return window.getResourceLoader().loadNVGTexture(logoFile.getPath(), true);
							else {
								logoFilePath = logoFilePathSplit[0];
								logoFilePath += ".scale-100" + "." + logoFilePathSplit[1];
								logoFile = new File(appExe.getParent() + "\\" + logoSplitBack[0] + "\\" + logoFilePath);
								if (logoFile.exists())
									return window.getResourceLoader().loadNVGTexture(logoFile.getPath(), true);
								else {
									logoFilePath = logoFilePathSplit[0];
									logoFilePath += "." + logoFilePathSplit[1];
									logoFile = new File(
											appExe.getParent() + "\\" + logoSplitBack[0] + "\\" + logoFilePath);
									return window.getResourceLoader().loadNVGTexture(logoFile.getPath(), true);
								}
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		File exe = new File(WindowUtils.getProcessFilePath(hwnd));
		if (image == null) {
			long iconHandle = User32Ext.INSTANCE.GetClassLongPtr(hwnd, GCLP_HICON);
			if (iconHandle == 0)
				iconHandle = User32Ext.INSTANCE.GetClassLongPtr(hwnd, GCLP_HICONSM);
			if (!exe.exists()) {
				if (iconHandle == 0)
					iconHandle = User32Ext.INSTANCE.SendMessage(hwnd, WM_GETICON, new WPARAM(ICON_BIG), new LPARAM())
							.longValue();
				if (iconHandle == 0)
					iconHandle = User32Ext.INSTANCE.SendMessage(hwnd, WM_GETICON, new WPARAM(ICON_SMALL), new LPARAM())
							.longValue();
				if (iconHandle == 0)
					iconHandle = User32Ext.INSTANCE.SendMessage(hwnd, WM_GETICON, new WPARAM(ICON_SMALL2), new LPARAM())
							.longValue();
			}
			if (iconHandle != 0) {
				final HICON hIcon = new HICON(new Pointer(iconHandle));
				final Dimension iconSize = WindowUtils.getIconSize(hIcon);
				if (iconSize.width != 0 && iconSize.height != 0) {

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
						if (b == 0 && g == 0 && r == 0)
							a = 0;
						argb = (a << 24) | (r << 16) | (g << 8) | b;
						image.setRGB(x, y, argb);
						x = (x + 1) % width;
						if (x == 0)
							y--;
					}
					User32.INSTANCE.ReleaseDC(null, hDC);
				}
			}
		}
		if (image == null)
			try {
				sun.awt.shell.ShellFolder sf = sun.awt.shell.ShellFolder.getShellFolder(exe);
				image = (BufferedImage) sf.getIcon(true);
			} catch (FileNotFoundException e1) {
			}
		if (image == null) {
			Icon icon = FileSystemView.getFileSystemView().getSystemIcon(exe);
			if (icon instanceof ImageIcon)
				image = (BufferedImage) ((ImageIcon) icon).getImage();
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
		try (ReadableByteChannel rbc = Channels.newChannel(source)) {
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
