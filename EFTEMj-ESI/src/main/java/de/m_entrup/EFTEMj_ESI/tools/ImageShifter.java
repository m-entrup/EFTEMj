/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2015, Michael Entrup b. Epping
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.m_entrup.EFTEMj_ESI.tools;

import java.awt.Point;
import java.util.Arrays;

import ij.process.FloatProcessor;

/**
 * This class shifts images depending on the result of the cross correlation.
 */
public class ImageShifter {

	/**
	 * The Position of the maximum is detected.
	 *
	 * @param fp An image that contains the cross correlation coefficients (size:
	 *          [2*delta+1; 2*delta+1])
	 */
	public static Point calcShift(final FloatProcessor fp) {
		final int sizeX = fp.getWidth();
		final int sizeY = fp.getHeight();
		final int delta = (sizeX - 1) / 2;
		final Point maxPos = new Point(0, 0);
		int max = 0;

		for (int i = 0; i < sizeY; i++) {
			for (int j = 0; j < sizeX; j++) {
				if (fp.get(j, i) > max) {
					max = fp.get(j, i);
					maxPos.x = j - delta;
					maxPos.y = i - delta;
				}
			}
		}

		return maxPos;
	}

	/**
	 * This methods creates a new {@link FloatProcessor} with a contend that is
	 * shifted by the value saved in shiftValue. The passed {@link FloatProcessor}
	 * is not changed.
	 *
	 * @param shiftValue Number of pixel the image has to be shifted [x, y]
	 * @param fp The image that has to be shifted
	 * @return A new {@link FloatProcessor} with the shifted image
	 */
	public static FloatProcessor moveImage(final Point shiftValue,
		final FloatProcessor fp)
	{
		final int width = fp.getWidth();
		final int height = fp.getHeight();

		final float[] toMove = (float[]) fp.getPixelsCopy();
		final float[] newPixels = toMove.clone();

		if (shiftValue.y < 0) {
			Arrays.fill(newPixels, 0, toMove.length, 0);
			for (int i = 0; i < (toMove.length + (shiftValue.y * width)); i++) {
				newPixels[i] = toMove[i - (shiftValue.y * width)];
			}
		}

		if (shiftValue.y > 0) {
			Arrays.fill(newPixels, 0, toMove.length, 0);
			for (int i = shiftValue.y * width; i < toMove.length; i++) {
				newPixels[i] = toMove[i - (shiftValue.y * width)];
			}
		}

		if (shiftValue.x != 0) {
			final float[] temp = newPixels.clone();
			Arrays.fill(newPixels, 0, toMove.length, 0);
			if (shiftValue.x < 0) {
				for (int j = 0; j < height; j++) {
					for (int i = 0; i < width + shiftValue.x; i++) {
						newPixels[(j * width) + i] = temp[(j * width) + i - shiftValue.x];
					}
				}
			}

			if (shiftValue.x > 0) {
				for (int j = 0; j < height; j++) {
					for (int i = shiftValue.x; i < width; i++) {
						newPixels[(j * width) + i] = temp[(j * width) + i - shiftValue.x];
					}
				}
			}
		}

		final FloatProcessor moved = new FloatProcessor(width, height, newPixels,
			null);
		return moved;
	}
}
