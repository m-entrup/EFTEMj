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
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;

/**
 * This class calculates Chi² pixel by pixel. Each instance processes a single
 * image row.
 */
public class Chi2Calculation extends Thread {

	/**
	 * Map of parameter <code>a</code>.
	 */
	private final float[] aMap;
	/**
	 * The pre-edge images of this array are used at the calculation.
	 */
	private final EFTEMImage[] array_EFTEMImages;
	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * The index of the first post-edge image.
	 */
	private final int edgeIndex;
	/**
	 * Map of errors that occurred during the MLE calculation.
	 */
	private final float[] errorMap;
	/**
	 * <code>x + y * width</code>
	 */
	private int index;
	/**
	 * Map of parameter <code>r</code>.
	 */
	private final float[] rMap;
	/**
	 * A shortcut to access the instance of {@link ThreadInterface}.
	 */
	private final ThreadInterface threadInterface = ThreadInterface.getInstance();
	/**
	 * The x coordinate of the processed pixel.
	 */
	private int x;
	/**
	 * The y coordinate of the processed pixel.
	 */
	private final int y;

	/**
	 * The constructor creates a new instance of {@link Chi2Calculation} for the
	 * Chi^2 calculation of each pixel at an image row.
	 *
	 * @param y
	 *            The image row that is processed by the new instance of
	 *            {@link Chi2Calculation}.
	 */
	public Chi2Calculation(final int y) {
		super();
		threadInterface.addThread();
		this.x = 0;
		this.y = y;
		edgeIndex = datasetAPI.getEdgeIndex();
		array_EFTEMImages = datasetAPI.getEFTEMImageArray();
		rMap = datasetAPI.getRMap();
		aMap = datasetAPI.getAMap();
		errorMap = datasetAPI.getErrorMap();
	}

	/**
	 * Calculating the background again is faster than reading it from the saved
	 * results, especially with a increasing number of pre-edge images.
	 *
	 * @param eLoss
	 *            The energy loss that is used for the calculation.
	 * @return The background signal.
	 */
	private float calculateBG(final float eLoss) {
		final float bg = (float) Math.exp(aMap[index] - rMap[index] * Math.log(eLoss));
		return bg;
	}

	/**
	 * @return The value of Chi² at the pixel at the array position
	 *         <code>index</code>.
	 */
	private float calculateChi2() {
		float sum = 0;
		float counts;
		float fit;
		for (int i = 0; i < edgeIndex; i++) {
			counts = array_EFTEMImages[i].getPixels()[index];
			fit = calculateBG(array_EFTEMImages[i].getELoss());
			sum += Math.pow((counts - fit), 2) / counts;
		}
		return sum;
	}

	@Override
	public void run() {
		final int width = datasetAPI.getWidth();
		final float[] chi2 = new float[width];
		for (x = 0; x < width; x++) {
			index = x + y * width;
			if (errorMap[index] == 0) {
				chi2[x] = calculateChi2();
			} else {
				chi2[x] = 0;
			}
		}
		datasetAPI.saveChi2(y * width, chi2);
		threadInterface.removeThread(ThreadInterface.CHI2);
	}
}
