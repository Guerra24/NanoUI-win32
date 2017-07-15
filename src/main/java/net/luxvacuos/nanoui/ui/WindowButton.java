package net.luxvacuos.nanoui.ui;

import com.sun.jna.platform.win32.WinDef.HWND;

public class WindowButton extends Button {
	
	private HWND hwnd;

	public WindowButton(float x, float y, float w, float h, String text, HWND hwnd) {
		super(x, y, w, h, text);
		this.hwnd = hwnd;
	}
	
	public HWND getHwnd() {
		return hwnd;
	}

}
