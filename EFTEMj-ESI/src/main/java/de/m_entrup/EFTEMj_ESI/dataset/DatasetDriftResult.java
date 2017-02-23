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

import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import ij.ImagePlus;
import ij.process.FloatProcessor;

/**
 * {@link DatasetDriftResult} stores all results of the crosscorrelation, that
 * is part of the drift correction.
 */
public class DatasetDriftResult {

	/**
	 * The crosscorrelation coefficients of each image are saved at this
	 * 2D-array.
	 */
	protected float[][] array_correlationCoefficients;
	/**
	 * To create an {@link ImagePlus} object the crosscorrelation coefficients
	 * of each image are packed into a {@link FloatProcessor}.
	 */
	protected FloatProcessor[] array_correlationCoefficientsAsFP;

	/**
	 * When a new instance of {@link DatasetDriftResult} is created
	 * array_correlationCoefficients is initialised. The size of the array is
	 * <br>
	 * [datasetAPI.getStackSize()]*[(2*datasetAPI.getDelta()+1)^2]
	 */
	public DatasetDriftResult() {
		final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
		array_correlationCoefficients = new float[datasetAPI.getStackSize()][(2 * datasetAPI.getDelta() + 1)
				* (2 * datasetAPI.getDelta() + 1)];
	}

	/**
	 * This method creates an array of {@link FloatProcessor}s (field of this
	 * class) from the 2D-array (field of this class) of crosscorrelation
	 * coefficients.
	 */
	protected void createFloatProcessorFromArray() {
		array_correlationCoefficientsAsFP = new FloatProcessor[array_correlationCoefficients.length];
		for (int i = 0; i < array_correlationCoefficients.length; i++) {
			final int width = (int) Math.sqrt(array_correlationCoefficients[i].length);
			array_correlationCoefficientsAsFP[i] = new FloatProcessor(width, width, array_correlationCoefficients[i]);
		}
	}

}
