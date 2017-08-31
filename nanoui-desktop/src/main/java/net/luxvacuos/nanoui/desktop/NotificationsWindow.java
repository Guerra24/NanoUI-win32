package net.luxvacuos.nanoui.desktop;

import static com.sun.jna.platform.win32.WinUser.GWL_EXSTYLE;
import static com.sun.jna.platform.win32.WinUser.GWL_WNDPROC;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.windows.User32.WM_KILLFOCUS;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;
import static org.lwjgl.system.windows.User32.WM_CONTEXTMENU;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.WindowProc;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;

import net.luxvacuos.nanoui.core.AppUI;
import net.luxvacuos.nanoui.core.TaskManager;
import net.luxvacuos.nanoui.core.states.AbstractState;
import net.luxvacuos.nanoui.rendering.api.glfw.Window;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowHandle;
import net.luxvacuos.nanoui.rendering.api.glfw.WindowManager;
import net.luxvacuos.nanoui.resources.ResourceLoader;
import net.luxvacuos.nanoui.ui.Button;
import net.luxvacuos.nanoui.ui.Component;
import net.luxvacuos.nanoui.ui.ComponentWindow;
import net.luxvacuos.nanoui.ui.Container;
import net.luxvacuos.nanoui.ui.Direction;
import net.luxvacuos.nanoui.ui.FlowLayout;
import net.luxvacuos.nanoui.ui.Font;
import net.luxvacuos.win32.User32Ext;
import net.luxvacuos.win32.User32Ext.Accent;
import net.luxvacuos.win32.User32Ext.AccentPolicy;
import net.luxvacuos.win32.User32Ext.NOTIFYICONDATA;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttribute;
import net.luxvacuos.win32.User32Ext.WindowCompositionAttributeData;

public class NotificationsWindow extends AbstractState {
	private Window window;
	private WindowHandle handle;
	private ComponentWindow compWin;
	private Font segoeui, segoemdl2;
	private HWND local;
	private Container icons;
	private Map<GUID, NotificationButton> iconsMap = new HashMap<>();
	private Queue<Runnable> iconsTask = new ConcurrentLinkedQueue<>();

	protected NotificationsWindow(Window backWindow, WindowHandle handle) {
		super("_main");
		this.window = backWindow;
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
		compWin.init(window);
		compWin.setBackgroundColor(0, 0, 0, 0);
		compWin.getTitlebar().setEnabled(false);
		icons = new Container(0, 0, 200, 200);
		icons.setLayout(new FlowLayout(Direction.RIGHT, 0, 0));
		compWin.addComponent(icons);

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

	}

	@Override
	public void dispose() {
		super.dispose();
		compWin.dispose(window);
		segoeui.dispose();
		segoemdl2.dispose();
	}

	@Override
	public void update(float delta) {
		compWin.update(delta, window);
		while (!iconsTask.isEmpty()) {
			iconsTask.poll().run();
		}
		for (Component comp : iconsMap.values()) {
			NotificationButton btn = (NotificationButton) comp;
			if (!User32.INSTANCE.IsWindow(btn.iconData.hWnd)) {
				iconDeleted(btn.iconData);
			}
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

	public void iconAdded(NOTIFYICONDATA data) {
		if (true)
			return;
		System.out.println("Added: " + data.uID);
		if (!iconsMap.containsKey(data.guidItem))
			iconsTask.add(() -> {
				NotificationButton btn = new NotificationButton(0, 0, 16, 16, "Test", data);
				btn.setOnButtonPress(() -> {
					User32Ext.INSTANCE.SendMessage(data.hWnd, WM_CONTEXTMENU,
							new WPARAM(Pointer.nativeValue(data.hWnd.getPointer())), new LPARAM());
				});
				icons.addComponent(btn);
				iconsMap.put(data.guidItem, btn);
			});
	}

	public void iconModified(NOTIFYICONDATA data) {
		if (true)
			return;
		System.out.println("Modified: " + data.uID);
		iconsTask.add(() -> {
			NotificationButton btn = iconsMap.get(data.guidItem);
			if (btn != null) {
				btn.iconData = data;
			}
		});
	}

	public void iconDeleted(NOTIFYICONDATA data) {
		if (true)
			return;
		System.out.println("Deleted: " + data.uID);
		iconsTask.add(() -> {
			NotificationButton button = iconsMap.remove(data.guidItem);
			if (button != null) {
				icons.removeComponent(button, window);
			}
		});
	}
}
