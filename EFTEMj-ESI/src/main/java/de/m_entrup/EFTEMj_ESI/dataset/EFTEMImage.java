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

package de.m_entrup.EFTEMj_ESI.dataset;

import ij.IJ;

/**
 * An {@link EFTEMImage} is part of an EFTEM stack. The EFTEM stack is realised
 * by an array of {@link EFTEMImage}s. The {@link EFTEMImage} consists of an
 * array of pixels, the energy loss and the label of the image. You can compare
 * {@link EFTEMImage}s to each other. The energy loss is used for the
 * comparison.
 */
public class EFTEMImage implements Comparable<EFTEMImage> {

	/**
	 * The energy loss of the {@link EFTEMImage}.
	 */
	protected float eLoss;
	/**
	 * The label of the {@link EFTEMImage}.
	 */
	protected String label;
	/**
	 * The image saved as an 1D-array
	 */
	protected float[] pixels;
	/**
	 * The width of the image. The height can be calculated from<br>
	 * <code>pixels.length()/width</code>.
	 */
	protected int width;

	/**
	 * Creates a new {@link EFTEMImage}.
	 *
	 * @param eLoss The energy loss of the image.
	 * @param label The label of the image. Use the short label.
	 * @param pixels A float array representing the pixels of the image.
	 * @param width The width of the image.
	 */
	public EFTEMImage(final float eLoss, final String label, final float[] pixels,
		final int width)
	{
		super();
		this.eLoss = eLoss;
		this.label = label;
		this.pixels = pixels;
		this.width = width;
	}

	@Override
	public int compareTo(final EFTEMImage o) {
		if (this.eLoss > o.eLoss) return 1;
		if (this.eLoss < o.eLoss) return -1;
		return 0;
	}

	/**
	 * @return The energy loss of the {@link EFTEMImage}.
	 */
	public float getELoss() {
		return eLoss;
	}

	/**
	 * @return The height of the {@link EFTEMImage}.
	 */
	public int getHeight() {
		return pixels.length / width;
	}

	/**
	 * @return The label of the {@link EFTEMImage}. This should be the same a the
	 *         shortLabel of the initial {@link IJ} image.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return The values of the {@link EFTEMImage} as a float array.
	 */
	public float[] getPixels() {
		return pixels;
	}

	/**
	 * @return The width of the {@link EFTEMImage}.
	 */
	public int getWidth() {
		return width;
	}

}
