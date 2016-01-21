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

package de.m_entrup.EFTEMj_ESI.map;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.dataset.EFTEMImage;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import ij.ImageStack;

/**
 * This class calculates the background images using the parameter
 * <code>r</code> and <code>a</code> of the power law. Each instance of
 * {@link BGCalculation} processes a single image. Additionally to the
 * background the ratio of the calculated background to the measured values is
 * determined.
 */
public class BGCalculation extends Thread {

	/**
	 * Map of parameter <code>a</code>.
	 */
	private final float[] aMap;
	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * The energy loss of the processed image.
	 */
	private final float eLoss;
	/**
	 * Map of errors that occurred during the MLE calculation.
	 */
	private final float[] errorValues;
	/**
	 * The position of the processed image at the sorted input {@link ImageStack}.
	 */
	private final int imageIndex;
	/**
	 * The pixels of the measured image.
	 */
	private final float[] pixels;
	/**
	 * Map of parameter <code>r</code>.
	 */
	private final float[] rMap;
	/**
	 * A shortcut to access the instance of {@link ThreadInterface}.
	 */
	private final ThreadInterface threadInterface = ThreadInterface.getInstance();

	/**
	 * Each instance calculates a background image at the energy loss of the given
	 * eftemImage.
	 *
	 * @param eftemImage The energy loss of this image i used for the background
	 *          calculation. Additionally this image is compared to the calculated
	 *          background.
	 * @param imageIndex The position of the processed image at the sorted input
	 *          {@link ImageStack}. This is used to save the results of the
	 *          calculation by using the {@link DatasetAPI}.
	 */
	public BGCalculation(final EFTEMImage eftemImage, final int imageIndex) {
		super();
		threadInterface.addThread();
		this.imageIndex = imageIndex;
		pixels = eftemImage.getPixels();
		eLoss = eftemImage.getELoss();
		rMap = datasetAPI.getRMap();
		aMap = datasetAPI.getAMap();
		errorValues = datasetAPI.getErrorMap();
	}

	@Override
	public void run() {
		final int length = rMap.length;
		final float[] background = new float[length];
		final float[] relBackground = new float[length];
		for (int index = 0; index < length; index++) {
			if (errorValues[index] == 0) {
				background[index] = (float) Math.exp(aMap[index] - rMap[index] * Math
					.log(eLoss));
				if (pixels[index] != 0) {
					relBackground[index] = background[index] / pixels[index];
				}
				else {
					relBackground[index] = 0;
				}
			}
			else {
				background[index] = PluginConstants.VALUE_CALCULATION_FAILED;
				relBackground[index] = PluginConstants.VALUE_CALCULATION_FAILED;
			}
		}
		datasetAPI.saveBackground(background, imageIndex);
		datasetAPI.saveRelBackground(relBackground, imageIndex);
		threadInterface.removeThread(ThreadInterface.BG);
	}
}
