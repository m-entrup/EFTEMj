
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
