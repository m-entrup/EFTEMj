
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
	 * The crosscorrelation coefficients of each image are saved at this 2D-array.
	 */
	protected float[][] array_correlationCoefficients;
	/**
	 * To create an {@link ImagePlus} object the crosscorrelation coefficients of
	 * each image are packed into a {@link FloatProcessor}.
	 */
	protected FloatProcessor[] array_correlationCoefficientsAsFP;

	/**
	 * When a new instance of {@link DatasetDriftResult} is created
	 * array_correlationCoefficients is initialised. The size of the array is <br>
	 * [datasetAPI.getStackSize()]*[(2*datasetAPI.getDelta()+1)^2]
	 */
	public DatasetDriftResult() {
		final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
		array_correlationCoefficients = new float[datasetAPI.getStackSize()][(2 *
			datasetAPI.getDelta() + 1) * (2 * datasetAPI.getDelta() + 1)];
	}

	/**
	 * This method creates an array of {@link FloatProcessor}s (field of this
	 * class) from the 2D-array (field of this class) of crosscorrelation
	 * coefficients.
	 */
	protected void createFloatProcessorFromArray() {
		array_correlationCoefficientsAsFP =
			new FloatProcessor[array_correlationCoefficients.length];
		for (int i = 0; i < array_correlationCoefficients.length; i++) {
			final int width = (int) Math.sqrt(
				array_correlationCoefficients[i].length);
			array_correlationCoefficientsAsFP[i] = new FloatProcessor(width, width,
				array_correlationCoefficients[i]);
		}
	}

}
