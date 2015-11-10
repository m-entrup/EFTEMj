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

package de.m_entrup.EFTEMj_SR_EELS;

/**
 * @author Michael Entrup b. Epping
 */
public abstract class CoordinateCorrector {

	protected SR_EELS_FloatProcessor inputProcessor;
	protected SR_EELS_FloatProcessor outputProcessor;
	protected SR_EELS_Polynomial_2D functionWidth;
	protected SR_EELS_Polynomial_2D functionBorder;

	public CoordinateCorrector(final SR_EELS_FloatProcessor inputProcessor,
		final SR_EELS_FloatProcessor outputProcessor)
	{
		this.inputProcessor = inputProcessor;
		this.outputProcessor = outputProcessor;
		this.functionWidth = inputProcessor.getWidthFunction();
		this.functionBorder = inputProcessor.getBorderFunction();
	}

	public float[] transformCoordinate(final float x1, final float x2)
		throws SR_EELS_Exception
	{
		final float[] pointIn = outputProcessor.convertToFunctionCoordinates(
			new float[] { x1, x2 });
		final float[] pointOut = new float[2];
		final float y2n = calcY2n(pointIn[0], pointIn[1]);
		final float y1 = calcY1(pointIn[0], y2n);
		final float y2 = calcY2(pointIn[0], y2n);
		pointOut[0] = y1;
		pointOut[1] = y2;
		return inputProcessor.convertToImageCoordinates(pointOut);
	}

	abstract float calcY2n(float x1, float x2) throws SR_EELS_Exception;

	abstract float calcY1(float x1, float y2n);

	abstract float calcY2(float y1, float y2n);

}
