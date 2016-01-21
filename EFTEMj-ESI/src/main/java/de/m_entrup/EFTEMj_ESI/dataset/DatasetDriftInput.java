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

import java.awt.Rectangle;

import ij.process.FloatProcessor;

/**
 * {@link DatasetDriftInput} stores all data that is used at the drift
 * correction.
 */
public class DatasetDriftInput {

	/**
	 * An array of cropped images that is used for the crosscorrelation. The image
	 * at the position referenceImageIndex is the reference image, which is larger
	 * (width+2*delta, height+2*delta) than the other once.
	 */
	protected FloatProcessor[] array_CroppedImages;
	/**
	 * This is the largest considered drift.
	 */
	protected int delta;
	/**
	 * Because the used crosscorrelation is normalised a mean value of each image,
	 * expect the reference image, is needed.
	 */
	protected float[] mean;
	/**
	 * This is the index of the reference image. The reference image is saved at
	 * the same array as all other images.
	 */
	protected int referenceImageIndex;
	/**
	 * This is the variance of each image. It is part of the denominator of the
	 * crosscorrelation coefficient. This value is calculated for each image,
	 * expect the reference image.
	 */
	protected double[] sigma;
	protected Rectangle roi;

	public DatasetDriftInput(final FloatProcessor[] array_croppedImages,
		final Rectangle roi, final int referenceIndex, final int delta)
	{
		this.array_CroppedImages = array_croppedImages;
		this.roi = (Rectangle) roi.clone();
		referenceImageIndex = referenceIndex - 1;
		this.delta = delta;
		mean = new float[array_croppedImages.length];
		sigma = new double[array_croppedImages.length];
		calcMeanAndSigma();
	}

	/**
	 * This method calculates mean and sigma which are used to calculate the
	 * normalised cross correlation coefficient.
	 */
	private void calcMeanAndSigma() {
		for (int i = 0; i < array_CroppedImages.length; i++) {
			if (i != referenceImageIndex) {
				final float[] pixels = (float[]) array_CroppedImages[i].getPixels();
				mean[i] = 0;
				double squareSum = 0;
				for (int j = 0; j < pixels.length; j++) {
					final double q = pixels[j];
					mean[i] += q / pixels.length;
					squareSum += q * q;
				}
				sigma[i] = Math.sqrt(squareSum - pixels.length * Math.pow(mean[i], 2));
			}
		}
	}

}
