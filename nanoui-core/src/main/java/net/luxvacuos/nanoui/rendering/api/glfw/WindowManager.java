/*
 * This file is part of NanoUI
 * 
 * Copyright (C) 2016-2017 Lux Vacuos
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

package net.luxvacuos.nanoui.rendering.api.glfw;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;

import com.badlogic.gdx.utils.Array;

import net.luxvacuos.nanoui.core.Variables;
import net.luxvacuos.nanoui.core.exception.DecodeTextureException;
import net.luxvacuos.nanoui.core.exception.GLFWException;
import net.luxvacuos.nanoui.core.states.StateMachine;
import net.luxvacuos.nanoui.input.Mouse;
import net.luxvacuos.nanoui.resources.ResourceLoader;

public final class WindowManager {

	private static Array<Window> windows = new Array<>();

	private WindowManager() {
	}

	public static WindowHandle generateHandle(int width, int height, String title) {
		return new WindowHandle(width, height, title);
	}

	public static long createWindow(WindowHandle handle, boolean vsync) {
		long windowID = GLFW.glfwCreateWindow(handle.width, handle.height, handle.title, NULL, NULL);
		if (windowID == NULL)
			throw new GLFWException("Failed to create GLFW Window '" + handle.title + "'");

		Window window = new Window(windowID, handle.width, handle.height);

		GLFW.glfwSetWindowPos(windowID, Variables.X, Variables.Y);
		GLFW.glfwMakeContextCurrent(windowID);
		GLFW.glfwSwapInterval(vsync ? 1 : 0);

		if (handle.cursor != null) {

			ByteBuffer imageBuffer;
			try {
				imageBuffer = ResourceLoader.ioResourceToByteBuffer("assets/cursors/" + handle.cursor + ".png",
						8 * 1024);
			} catch (IOException e) {
				throw new GLFWException(e);
			}

			IntBuffer w = BufferUtils.createIntBuffer(1);
			IntBuffer h = BufferUtils.createIntBuffer(1);
			IntBuffer comp = BufferUtils.createIntBuffer(1);

			if (!stbi_info_from_memory(imageBuffer, w, h, comp))
				throw new DecodeTextureException("Failed to read image information: " + stbi_failure_reason());

			ByteBuffer image = stbi_load_from_memory(imageBuffer, w, h, comp, 0);
			if (image == null)
				throw new DecodeTextureException("Failed to load image: " + stbi_failure_reason());

			GLFWImage img = GLFWImage.malloc().set(w.get(0), h.get(0), image);
			GLFW.glfwSetCursor(windowID, GLFW.glfwCreateCursor(img, 0, 0));

			stbi_image_free(image);
		}

		if (handle.icons.size != 0) {
			GLFWImage.Buffer iconsbuff = GLFWImage.malloc(handle.icons.size);
			int i = 0;
			for (Icon icon : handle.icons) {
				ByteBuffer imageBuffer;
				try {
					imageBuffer = ResourceLoader.ioResourceToByteBuffer("assets/icons/" + icon.path + ".png", 8 * 1024);
				} catch (IOException e) {
					throw new GLFWException(e);
				}

				IntBuffer w = BufferUtils.createIntBuffer(1);
				IntBuffer h = BufferUtils.createIntBuffer(1);
				IntBuffer comp = BufferUtils.createIntBuffer(1);

				if (!stbi_info_from_memory(imageBuffer, w, h, comp))
					throw new DecodeTextureException("Failed to read image information: " + stbi_failure_reason());

				icon.image = stbi_load_from_memory(imageBuffer, w, h, comp, 0);
				if (icon.image == null)
					throw new DecodeTextureException("Failed to load image: " + stbi_failure_reason());

				icon.image.flip();
				iconsbuff.position(i).width(w.get(0)).height(h.get(0)).pixels(icon.image);
				i++;
			}
			iconsbuff.position(0);
			GLFW.glfwSetWindowIcon(windowID, iconsbuff);
			iconsbuff.free();
			for (Icon icon : handle.icons) {
				stbi_image_free(icon.image);
			}

		}

		boolean forwardCompat = GLFW.glfwGetWindowAttrib(windowID, GLFW.GLFW_OPENGL_FORWARD_COMPAT) == GLFW.GLFW_TRUE;
		window.capabilities = GL.createCapabilities(forwardCompat);

		int nvgFlags = NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES;
		if (Variables.DEBUG)
			nvgFlags = (nvgFlags | NanoVGGL3.NVG_DEBUG);
		window.nvgID = NanoVGGL3.nvgCreate(nvgFlags);

		if (window.nvgID == NULL)
			throw new GLFWException("Fail to create NanoVG context for Window '" + handle.title + "'");

		window.lastLoopTime = getTime();

		int[] h = new int[1];
		int[] w = new int[1];

		GLFW.glfwGetFramebufferSize(windowID, w, h);
		window.framebufferHeight = h[0];
		window.framebufferWidth = w[0];
		GLFW.glfwGetWindowSize(windowID, w, h);
		window.height = h[0];
		window.width = w[0];
		window.pixelRatio = (float) window.framebufferWidth / (float) window.width;
		window.resetViewport();

		window.created = true;

		windows.add(window);

		return windowID;
	}

	public static Window getWindow(long windowID) {
		for (Window window : windows) {
			if (window.windowID == windowID) {
				int index = windows.indexOf(window, true);
				if (index != 0)
					windows.swap(0, index); // Swap the window to the front of
											// the array to speed up future
											// recurring searches
				if (GLFW.glfwGetCurrentContext() != windowID)
					GLFW.glfwMakeContextCurrent(windowID);
				return window;
			}

		}

		return null;
	}

	public static void closeAllDisplays() {
		for (Window window : windows) {
			if (!window.created)
				continue;
			window.dispose();
			window.closeDisplay();
		}
	}

	public static void update() {
		glfwPollEvents();
		Mouse.poll();
	}

	public static double getTime() {
		return GLFW.glfwGetTime();
	}

	public static long getNanoTime() {
		return (long) (getTime() * (1000L * 1000L * 1000L));
	}

	protected static GLFWCursorEnterCallback cursorEnterCallback;
	protected static GLFWCursorPosCallback cursorPosCallback;
	protected static GLFWMouseButtonCallback mouseButtonCallback;
	protected static GLFWWindowFocusCallback windowFocusCallback;
	protected static GLFWWindowSizeCallback windowSizeCallback;
	protected static GLFWWindowPosCallback windowPosCallback;
	protected static GLFWWindowRefreshCallback windowRefreshCallback;
	protected static GLFWFramebufferSizeCallback framebufferSizeCallback;
	protected static GLFWScrollCallback scrollCallback;

	static {
		cursorEnterCallback = new GLFWCursorEnterCallback() {
			@Override
			public void invoke(long windowID, boolean entered) {
				Mouse.setMouseInsideWindow(entered);
			}
		};

		cursorPosCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long windowID, double xpos, double ypos) {
				Mouse.addMoveEvent(xpos, ypos);
			}
		};

		mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long windowID, int button, int action, int mods) {
				Mouse.addButtonEvent(button, action == GLFW.GLFW_PRESS ? true : false);
			}
		};

		scrollCallback = new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				Mouse.addWheelEvent((int) yoffset);
			}
		};

		windowFocusCallback = new GLFWWindowFocusCallback() {
			@Override
			public void invoke(long windowID, boolean focused) {
				if (focused && getWindow(windowID) != null && !getWindow(windowID).isWindowFocused())
					GLFW.glfwFocusWindow(windowID);
			}
		};

		windowSizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long windowID, int width, int height) {
				Window window = getWindow(windowID);
				if (window == null)
					return;

				IntBuffer w = BufferUtils.createIntBuffer(1);
				IntBuffer h = BufferUtils.createIntBuffer(1);
				GLFW.glfwGetFramebufferSize(windowID, w, h);
				window.framebufferWidth = w.get(0);
				window.framebufferHeight = h.get(0);

				GLFW.glfwGetWindowSize(windowID, w, h);
				window.width = w.get(0);
				window.height = h.get(0);
				window.pixelRatio = (float) window.framebufferWidth / (float) window.width;
				window.resetViewport();
			}
		};

		windowPosCallback = new GLFWWindowPosCallback() {
			@Override
			public void invoke(long windowID, int xpos, int ypos) {
				Window window = getWindow(windowID);
				if (window == null)
					return;
				window.posX = xpos;
				window.posY = ypos;
			}
		};

		windowRefreshCallback = new GLFWWindowRefreshCallback() {
			@Override
			public void invoke(long windowID) {
				Window window = getWindow(windowID);
				if (window == null)
					return;
				window.dirty = true;
				StateMachine.update(0);
				StateMachine.render(0);
				GLFW.glfwSwapBuffers(windowID);
			}
		};

		framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long windowID, int width, int height) {
				Window window = getWindow(windowID);
				if (window == null)
					return;
				window.framebufferWidth = width;
				window.framebufferHeight = height;
			}
		};

	}

}
