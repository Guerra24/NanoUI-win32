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

import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwHideWindow;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMaximizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowRefreshCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVGGL3.nvgDelete;
import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowMaximizeCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GLCapabilities;

import net.luxvacuos.nanoui.input.KeyboardHandler;
import net.luxvacuos.nanoui.input.MouseHandler;
import net.luxvacuos.nanoui.resources.ResourceLoader;

public abstract class AbstractWindow implements IWindow {

	protected final long windowID;

	protected KeyboardHandler kbHandle;
	protected MouseHandler mHandle;

	protected DisplayUtils displayUtils;

	protected GLCapabilities capabilities;

	protected boolean created = false;
	protected boolean dirty = false;

	protected int posX = 0;
	protected int posY = 0;

	protected boolean resized = false;
	protected boolean iconified = false;
	protected boolean visible = true;
	protected int width = 0;
	protected int height = 0;
	protected int framebufferWidth = 0;
	protected int framebufferHeight = 0;

	protected boolean latestResized = false;
	protected float pixelRatio;
	protected boolean active = true;
	protected boolean maximized = false;

	protected long nvgID;
	protected ResourceLoader resourceLoader;

	protected double lastLoopTime;
	protected float timeCount;

	protected OnRefresh onRefresh;

	protected GLFWWindowSizeCallback windowSizeCallback;
	protected GLFWWindowPosCallback windowPosCallback;
	protected GLFWWindowRefreshCallback windowRefreshCallback;
	protected GLFWFramebufferSizeCallback framebufferSizeCallback;
	protected GLFWScrollCallback scrollCallback;
	protected GLFWWindowFocusCallback focusCallback;
	protected GLFWWindowMaximizeCallback maximizeCallback;
	protected GLFWWindowIconifyCallback iconifyCallback;

	protected AbstractWindow(long windowID, int width, int height) {
		this.windowID = windowID;
		this.displayUtils = new DisplayUtils();
		this.width = width;
		this.height = height;
		this.visible = getWindowAttribute(GLFW_VISIBLE);
		this.setCallbacks();
	}

	protected void setCallbacks() {
		this.kbHandle = new KeyboardHandler(this.windowID);
		this.mHandle = new MouseHandler(this.windowID, this);

		windowSizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long windowID, int ww, int wh) {
				width = ww;
				height = wh;
				pixelRatio = (float) framebufferWidth / (float) width;
				resetViewport();
			}
		};

		windowPosCallback = new GLFWWindowPosCallback() {
			@Override
			public void invoke(long windowID, int xpos, int ypos) {
				posX = xpos;
				posY = ypos;
			}
		};

		windowRefreshCallback = new GLFWWindowRefreshCallback() {
			@Override
			public void invoke(long windowID) {
				dirty = true;
				if (onRefresh != null)
					onRefresh.onRefresh(windowID);
				GLFW.glfwSwapBuffers(windowID);
			}
		};

		framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long windowID, int width, int height) {
				framebufferWidth = width;
				framebufferHeight = height;
			}
		};

		focusCallback = new GLFWWindowFocusCallback() {

			@Override
			public void invoke(long windowID, boolean focused) {
				active = focused;
			}
		};

		maximizeCallback = new GLFWWindowMaximizeCallback() {

			@Override
			public void invoke(long windowID, boolean max) {
				maximized = max;
			}
		};

		iconifyCallback = new GLFWWindowIconifyCallback() {

			@Override
			public void invoke(long window, boolean icon) {
				iconified = icon;
			}
		};
		glfwSetWindowSizeCallback(windowID, windowSizeCallback);
		glfwSetWindowPosCallback(windowID, windowPosCallback);
		glfwSetWindowRefreshCallback(windowID, windowRefreshCallback);
		glfwSetFramebufferSizeCallback(windowID, framebufferSizeCallback);
		glfwSetWindowMaximizeCallback(windowID, maximizeCallback);
		glfwSetWindowFocusCallback(windowID, focusCallback);
		glfwSetWindowIconifyCallback(windowID, iconifyCallback);
	}

	@Override
	public void setVisible(boolean flag) {
		if (flag)
			glfwShowWindow(this.windowID);
		else
			glfwHideWindow(this.windowID);
		visible = flag;
	}

	public void setPosition(int x, int y) {
		glfwSetWindowPos(this.windowID, x, y);
	}

	public void setSize(int width, int height) {
		glfwSetWindowSize(this.windowID, width, height);
	}

	public void resetViewport() {
		glViewport(0, 0, (int) (width * pixelRatio), (int) (height * pixelRatio));
	}

	public void setViewport(int x, int y, int width, int height) {
		glViewport(0, 0, width, height);
	}

	public void setOnRefresh(OnRefresh onRefresh) {
		this.onRefresh = onRefresh;
	}

	@Override
	public float getDelta() {
		double time = WindowManager.getTime();
		float delta = (float) (time - this.lastLoopTime);
		this.lastLoopTime = time;
		this.timeCount += delta;
		return delta;
	}

	public float getTimeCount() {
		return this.timeCount;
	}

	public void setTimeCount(float timeCount) {
		this.timeCount = timeCount;
	}

	public boolean isWindowCreated() {
		return this.created;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public boolean isResizable() {
		return this.getWindowAttribute(GLFW.GLFW_RESIZABLE);
	}

	public boolean isIconified() {
		return iconified;
	}

	public boolean isVisible() {
		return visible;
	}

	public int getWindowX() {
		return this.posX;
	}

	public int getWindowY() {
		return this.posY;
	}

	public boolean wasResized() {
		return this.resized;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public int getFrameBufferWidth() {
		return this.framebufferWidth;
	}

	public int getFrameBufferHeight() {
		return this.framebufferHeight;
	}

	public float getPixelRatio() {
		return this.pixelRatio;
	}

	public long getID() {
		return this.windowID;
	}

	public long getNVGID() {
		return this.nvgID;
	}

	public ResourceLoader getResourceLoader() {
		if (this.resourceLoader == null)
			this.resourceLoader = new ResourceLoader(this.nvgID);
		return this.resourceLoader;
	}

	public KeyboardHandler getKeyboardHandler() {
		return this.kbHandle;
	}

	public MouseHandler getMouseHandler() {
		return this.mHandle;
	}

	public boolean isCloseRequested() {
		return glfwWindowShouldClose(this.windowID);
	}

	public boolean isActive() {
		return active;
	}

	public boolean isMaximized() {
		return maximized;
	}

	private boolean getWindowAttribute(int attribute) {
		return (GLFW.glfwGetWindowAttrib(this.windowID, attribute) == GLFW.GLFW_TRUE ? true : false);
	}

	public GLCapabilities getCapabilities() {
		return this.capabilities;
	}

	@Override
	public void beingNVGFrame() {
		nvgBeginFrame(this.nvgID, this.width, this.height, this.pixelRatio);
	}

	@Override
	public void endNVGFrame() {
		nvgEndFrame(this.nvgID);
	}

	@Override
	public void closeDisplay() {
		if (!this.created)
			return;
		Callbacks.glfwFreeCallbacks(this.windowID);
		glfwDestroyWindow(this.windowID);
		this.created = false;
	}

	@Override
	public void dispose() {
		nvgDelete(this.nvgID);
	}

	public void setWindowTitle(String text) {
		GLFW.glfwSetWindowTitle(this.windowID, text);
	}

}
