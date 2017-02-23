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
 * This class calculates the elemental-map at a given energy loss. It subtracts
 * the calculated background from the measured image. Pixel that have an error
 * at the error map are ignored.
 */
public class MapCalculation extends Thread {

	/**
	 * The calculated background signal.
	 */
	private final float[] bg;
	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * Map of errors that occurred during the MLE calculation.
	 */
	private final float[] errorValues;
	/**
	 * The position of the processed image at the sorted input
	 * {@link ImageStack}.
	 */
	private final int imageIndex;
	/**
	 * The pixels of the measured image.
	 */
	private final float[] signal;
	/**
	 * A shortcut to access the instance of {@link ThreadInterface}.
	 */
	private final ThreadInterface threadInterface = ThreadInterface.getInstance();

	/**
	 * Each instance calculates the elemental-map of the given eftemImage by
	 * subtracting the calculated background.
	 *
	 * @param eftemImage
	 *            The measured signal is taken from this object.
	 * @param imageIndex
	 *            The position of the processed image at the sorted input
	 *            {@link ImageStack}. This is used to save the results of the
	 *            calculation by using the {@link DatasetAPI}.
	 */
	public MapCalculation(final EFTEMImage eftemImage, final int imageIndex) {
		super();
		threadInterface.addThread();
		this.imageIndex = imageIndex;
		bg = datasetAPI.getBackgroundPixels(imageIndex);
		errorValues = datasetAPI.getErrorMap();
		signal = eftemImage.getPixels();
	}

	@Override
	public void run() {
		final float[] map = new float[signal.length];
		for (int index = 0; index < signal.length; index++) {
			if (errorValues[index] == 0) {
				map[index] = signal[index] - bg[index];
			} else {
				map[index] = PluginConstants.VALUE_CALCULATION_FAILED;
			}
		}
		datasetAPI.saveMap(map, imageIndex);
		threadInterface.removeThread(ThreadInterface.MAP);
	}

}
