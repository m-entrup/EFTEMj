/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2015, Michael Entrup b. Epping <mail@m-entrup.de>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package de.m_entrup.EFTEMj_SR_EELS.correction;

import ij.process.FloatProcessor;

/**
 * {@link SR_EELS_FloatProcessor} extends the {@link FloatProcessor} by
 * regarding the binning and an origin that not equals 0,0.
 *
 * @author Michael Entrup b. Epping
 */
public class SR_EELS_FloatProcessor extends FloatProcessor {

	private final int binningX;
	private final int binningY;
	private final int originX;
	private final int originY;
	private SR_EELS_Polynomial_2D widthFunction;
	private SR_EELS_Polynomial_2D borderFunction;

	public SR_EELS_FloatProcessor(final FloatProcessor fp, final int binning, final int originX, final int originY) {
		super(fp.getWidth(), fp.getHeight(), (float[]) fp.getPixels());
		this.binningX = binning;
		this.binningY = binning;
		this.originX = originX;
		this.originY = originY;
	}

	public SR_EELS_FloatProcessor(final FloatProcessor fp, final int binningX, final int binningY, final int originX,
			final int originY) {
		super(fp.getWidth(), fp.getHeight(), (float[]) fp.getPixels());
		this.binningX = binningX;
		this.binningY = binningY;
		this.originX = originX;
		this.originY = originY;
	}

	public SR_EELS_FloatProcessor(final int width, final int height, final int binning, final int originX,
			final int originY) {
		super(width, height, new float[width * height]);
		this.binningX = binning;
		this.binningY = binning;
		this.originX = originX;
		this.originY = originY;
	}

	public SR_EELS_FloatProcessor(final int width, final int height, final int binningX, final int binningY,
			final int originX, final int originY) {
		super(width, height, new float[width * height]);
		this.binningX = binningX;
		this.binningY = binningY;
		this.originX = originX;
		this.originY = originY;
	}

	public float getf(final int x, final int y, final boolean useTransform) {
		if (useTransform) {
			return getf(x / binningX + originX, y / binningY + originY);
		}
		return getf(x, y);
	}

	public int getBinningX() {
		return binningX;
	}

	public int getBinningY() {
		return binningY;
	}

	public int getOriginX() {
		return originX;
	}

	public int getOriginY() {
		return originY;
	}

	public void setWidthFunction(final SR_EELS_Polynomial_2D widthFunction) {
		this.widthFunction = widthFunction;
		this.widthFunction.setInputProcessor(this);
		this.widthFunction.setupWidthCorrection();
	}

	public void setBorderFunction(final SR_EELS_Polynomial_2D borderFunction) {
		this.borderFunction = borderFunction;
		this.borderFunction.setInputProcessor(this);
	}

	public SR_EELS_Polynomial_2D getWidthFunction() {
		return widthFunction;
	}

	public SR_EELS_Polynomial_2D getBorderFunction() {
		return borderFunction;
	}

	public float[] convertToFunctionCoordinates(final float[] x2) {
		final float[] point = new float[2];
		point[0] = (x2[0] - originX) * binningX;
		point[1] = (x2[1] - originY) * binningY;
		return point;
	}

	public float[] convertToFunctionCoordinates(final float x1, final float x2) {
		final float[] x = new float[] { x1, x2 };
		return convertToFunctionCoordinates(x);
	}

	public float[] convertToImageCoordinates(final float[] x2) {
		final float[] point = new float[2];
		point[0] = x2[0] / binningX + originX;
		point[1] = x2[1] / binningY + originY;
		return point;
	}

	public float[] convertToImageCoordinates(final float x1, final float x2) {
		final float[] x = new float[] { x1, x2 };
		return convertToImageCoordinates(x);
	}
}
