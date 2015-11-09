
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
