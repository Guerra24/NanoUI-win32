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

package net.luxvacuos.nanoui.resources;

import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import static org.lwjgl.nanovg.NanoVG.nvgCreateImageMem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.lwjgl.BufferUtils;

import net.luxvacuos.nanoui.core.exception.LoadTextureException;
import net.luxvacuos.nanoui.ui.Font;

/**
 * This objects handles all loading methods from any type of data, models,
 * textures, fonts, etc.
 * 
 * @author Guerra24 <pablo230699@hotmail.com>
 * @category Assets
 */
public class ResourceLoader {
	private long nvgID;

	public ResourceLoader(long nvgID) {
		this.nvgID = nvgID;
	}

	public Font loadNVGFont(String filename, String name) {
		return loadNVGFont(filename, name, 150, false);
	}

	public Font loadNVGFont(String filename, String name, boolean file) {
		return loadNVGFont(filename, name, 150, file);
	}

	public Font loadNVGFont(String filename, String name, int size, boolean file) {
		int font = 0;
		ByteBuffer buffer = null;
		try {
			if (!file)
				buffer = ioResourceToByteBuffer("assets/fonts/" + filename + ".ttf", size * 1024);
			else
				buffer = ioResourceToByteBuffer(filename + ".ttf", size * 1024);
			font = nvgCreateFontMem(nvgID, name, buffer, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Font(name, buffer, font);
	}

	public int loadNVGTexture(String file) {
		return loadNVGTexture(file, false);
	}

	public int loadNVGTexture(String filename, boolean file) {
		int tex = 0;
		try {
			if (!file)
				tex = loadNVGTexture(ioResourceToByteBuffer("assets/" + filename + ".png", 8 * 1024));
			else
				tex = loadNVGTexture(ioResourceToByteBuffer(filename, 8 * 1024));
		} catch (Exception e) {
			throw new LoadTextureException(filename, e);
		}
		return tex;
	}

	public int loadNVGTexture(ByteBuffer buffer) {
		if (buffer != null)
			return nvgCreateImageMem(nvgID, 0, buffer);
		else
			return -1;
	}

	/**
	 * Reads the specified resource and returns the raw data as a ByteBuffer.
	 *
	 * @param resource
	 *            the resource to read
	 * @param bufferSize
	 *            the initial buffer size
	 *
	 * @return the resource data
	 *
	 * @throws IOException
	 *             if an IO error occurs
	 */
	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
		ByteBuffer buffer;

		File file = new File(resource);
		if (file.isFile()) {
			FileInputStream fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();

			buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);

			while (fc.read(buffer) != -1)
				;

			fis.close();
			fc.close();
		} else {
			buffer = BufferUtils.createByteBuffer(bufferSize);

			InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
			if (source == null)
				throw new FileNotFoundException(resource);

			try {
				ReadableByteChannel rbc = Channels.newChannel(source);
				try {
					while (true) {
						int bytes = rbc.read(buffer);
						if (bytes == -1)
							break;
						if (buffer.remaining() == 0)
							buffer = resizeBuffer(buffer, buffer.capacity() * 2);
					}
				} finally {
					rbc.close();
				}
			} finally {
				source.close();
			}
		}
		buffer.put((byte) 0);
		buffer.flip();
		return buffer;
	}

	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}

}