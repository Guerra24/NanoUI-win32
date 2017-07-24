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
import static com.sun.jna.platform.win32.WinUser.SWP_NOZORDER;
import static com.sun.jna.platform.win32.WinUser.SW_MAXIMIZE;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.windows.User32.HTCAPTION;
import static org.lwjgl.system.windows.User32.HTTOP;
import static org.lwjgl.system.windows.User32.SWP_FRAMECHANGED;
import static org.lwjgl.system.windows.User32.SWP_NOMOVE;
import static org.lwjgl.system.windows.User32.SWP_NOSIZE;
import static org.lwjgl.system.windows.User32.WM_NCCALCSIZE;
import static org.lwjgl.system.windows.User32.WM_NCHITTEST;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.WindowProc;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
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
import net.luxvacuos.nanoui.ui.Box;
import net.luxvacuos.nanoui.ui.Button;
import net.luxvacuos.nanoui.ui.ComponentWindow;
import net.luxvacuos.nanoui.ui.Direction;
import net.luxvacuos.nanoui.ui.FlowLayout;
import net.luxvacuos.nanoui.ui.ScrollArea;
import net.luxvacuos.nanoui.ui.TextArea;
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
		window.setBackgroundColor(0, 0, 0, 1f);

		long hwndGLFW = glfwGetWin32Window(AppUI.getMainWindow().getID());
		HWND hwnd = new HWND(new Pointer(hwndGLFW));

		long dwp = User32.INSTANCE.GetWindowLongPtr(hwnd, GWL_WNDPROC).longValue();
		WindowProc proc = new WindowProc() {

			@Override
			public long invoke(long hw, int uMsg, long wParam, long lParam) {
				switch (uMsg) {
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

						par.rgrc[0].left += 8;
						if (winpl.showCmd != SW_MAXIMIZE)
							par.rgrc[0].top += 0;
						else
							par.rgrc[0].top += 7;
						par.rgrc[0].right -= 8;
						par.rgrc[0].bottom -= 8;
						par.write();
						return lParam;
					}
					break;
				}
				return JNI.callPPPP(dwp, hw, uMsg, wParam, lParam);
			}
		};
		User32.INSTANCE.SetWindowLongPtr(hwnd, GWL_WNDPROC, new Pointer(proc.address()));
		User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);

		MARGINS margins = new MARGINS();

		margins.cxLeftWidth = 0;
		margins.cxRightWidth = 0;
		margins.cyBottomHeight = 0;
		margins.cyTopHeight = 32;
		DWMapiExt.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, margins);

		Box right = new Box(200, 0, 600, 0);
		right.setColor("#8C8C8C8C");
		right.setResizeV(true);
		right.setResizeH(true);
		TextArea text = new TextArea(
				"Lorem ipsum dolor sit amet, ut dicta doctus pertinax ius. Te pro tantas eruditi, aperiri epicuri probatus sea eu. Duo ad ridens melius aeterno, ei eligendi laboramus voluptatum his. At graece hendrerit nec, ius homero patrioque an. Pro cu dicunt perpetua percipitur.\n"
						+ "\n"
						+ "Has ex option vivendo imperdiet. Aeque deserunt at cum. Ut vis nostrud platonem, est aliquam recusabo ad. Te eum quas rebum equidem, no quo case ubique accommodare, pri ex propriae sapientem.\n"
						+ "\n"
						+ "Ius aliquando definiebas ex, ad mei quando nonumy menandri, ut cum principes expetendis. Vim ea veri cetero feugait, id vim dolore nonumes appareat, equidem deterruisset an eos. Qui te deleniti salutatus. Mei electram laboramus torquatos id, cum ei cibo summo electram, eam at facilis percipit. Eam atqui iuvaret imperdiet in. Ridens sensibus duo ad, has veniam accusamus ex.\n"
						+ "\n"
						+ "Ne porro docendi cum, paulo labores assueverit ne est. No vel sanctus menandri prodesset. In ridens aliquam vim, sit in elitr adipisci. Nam ut veniam petentium. Per id suas urbanitas, integre elaboraret ne quo. Ea habeo ponderum consetetur qui. Per populo doctus lobortis et.\n"
						+ "\n"
						+ "Sed ex nulla errem utroque, eu persius veritus volumus mea. Nisl legere qualisque ex mei, te eleifend pericula usu. Ea est aeque interpretaris. Pericula dissentias mel ei, te saperet utroque definiebas qui. Eripuit omittantur an vel, ius eu prompta delectus accusamus, eum et paulo audire prodesset. Cu denique mediocrem sit, mundi mediocrem ut vel. ",
				214 + 20, -20, 600 - 40);
		text.setWindowAlignment(Alignment.LEFT_TOP);
		text.setResizeH(true);
		window.addComponent(right);
		window.addComponent(text);

		ScrollArea left = new ScrollArea(0, 0, 200, 0, 0, 0);
		left.setResizeH(false);
		left.setLayout(new FlowLayout(Direction.DOWN, 10, 0));
		for (int i = 0; i < 16; i++) {
			Button btn = new Button(0, 0, 184, 40, "Button " + i);
			btn.setWindowAlignment(Alignment.LEFT_TOP);
			btn.setAlignment(Alignment.RIGHT_BOTTOM);
			left.addComponent(btn);
		}
		window.addComponent(left);
		AppUI.getMainWindow().setVisible(true);
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
