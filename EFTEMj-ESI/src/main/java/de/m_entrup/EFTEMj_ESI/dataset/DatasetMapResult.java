
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
	 * The Chi� calculated by {@link Chi2Calculation}.
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
		vec_LoopCount = new Vector<Integer>();
		array_BackgroundImage = new EFTEMImage[datasetAPI.getStackSize()];
		array_RelativeBackgroundImage = new EFTEMImage[datasetAPI.getStackSize()];
		array_Map = new EFTEMImage[datasetAPI.getStackSize() - datasetAPI
			.getEdgeIndex()];
		array_Sigma2 = new EFTEMImage[datasetAPI.getStackSize() - datasetAPI
			.getEdgeIndex()];
		array_SNR = new EFTEMImage[datasetAPI.getStackSize() - datasetAPI
			.getEdgeIndex()];
		coefficientOfDeterminationMap = new float[datasetAPI.getWidth() * datasetAPI
			.getHeight()];
		chi2Map = new float[datasetAPI.getWidth() * datasetAPI.getHeight()];
	}
}
