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

import java.util.Vector;

import de.m_entrup.EFTEMj_ESI.map.BGCalculation;
import de.m_entrup.EFTEMj_ESI.map.Chi2Calculation;
import de.m_entrup.EFTEMj_ESI.map.CoeffOfDetCalculation;
import de.m_entrup.EFTEMj_ESI.map.MapCalculation;
import de.m_entrup.EFTEMj_ESI.map.PowerLawFitCalculation;
import de.m_entrup.EFTEMj_ESI.map.SNRCalculation;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;

/**
 * An instance of this class is used to save the results of the elemental-map
 * calculation. {@link DatasetAPI} is used to access this data.
 * {@link DatasetMapResult} does only contain protected fields and no methods.
 */
public class DatasetMapResult {

	/**
	 * This is the second parameter of the power law. <code>r</code> is used to
	 * calculate <code>a</code>.
	 */
	protected float[] aMap;
	/**
	 * The Background images calculated by {@link BGCalculation}.
	 */
	protected EFTEMImage[] array_BackgroundImage;
	/**
	 * The elemental-maps calculated by {@link MapCalculation}.
	 */
	protected EFTEMImage[] array_Map;
	/**
	 * The ratio of calculated background to measured intensities. This is
	 * calculated by {@link BGCalculation}.
	 */
	protected EFTEMImage[] array_RelativeBackgroundImage;
	/**
	 * The extrapolation errors calculated by {@link SNRCalculation}.
	 */
	protected EFTEMImage[] array_Sigma2;
	/**
	 * The SNR of all elemental-maps calculated by {@link SNRCalculation}.
	 */
	protected EFTEMImage[] array_SNR;
	/**
	 * The Chi² calculated by {@link Chi2Calculation}.
	 */
	protected float[] chi2Map;
	/**
	 * The coefficient of determination calculated by
	 * {@link CoeffOfDetCalculation}.
	 */
	protected float[] coefficientOfDeterminationMap;
	/**
	 * The errorMap saves errors that occur during at
	 * {@link PowerLawFitCalculation} .
	 */
	protected float[] errorMap;
	/**
	 * This is the first parameter of the power law.
	 */
	protected float[] rMap;
	/**
	 * The number of loops at the {@link PowerLawFitCalculation}.
	 */
	protected Vector<Integer> vec_LoopCount;

	/**
	 * the constructor initialises all fields of this class.
	 */
	public DatasetMapResult() {
		super();
		final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
		rMap = new float[datasetAPI.getWidth() * datasetAPI.getHeight()];
		aMap = new float[datasetAPI.getWidth() * datasetAPI.getHeight()];
		errorMap = new float[datasetAPI.getWidth() * datasetAPI.getHeight()];
		vec_LoopCount = new Vector<>();
		array_BackgroundImage = new EFTEMImage[datasetAPI.getStackSize()];
		array_RelativeBackgroundImage = new EFTEMImage[datasetAPI.getStackSize()];
		array_Map = new EFTEMImage[datasetAPI.getStackSize() - datasetAPI.getEdgeIndex()];
		array_Sigma2 = new EFTEMImage[datasetAPI.getStackSize() - datasetAPI.getEdgeIndex()];
		array_SNR = new EFTEMImage[datasetAPI.getStackSize() - datasetAPI.getEdgeIndex()];
		coefficientOfDeterminationMap = new float[datasetAPI.getWidth() * datasetAPI.getHeight()];
		chi2Map = new float[datasetAPI.getWidth() * datasetAPI.getHeight()];
	}
}
