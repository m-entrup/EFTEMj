
package de.m_entrup.EFTEMj_ESI.map;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.dataset.EFTEMImage;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import ij.ImageStack;

/**
 * This class calculates the Signal-to-Noise-Ratio (SNR) pixel by pixel. Each
 * instance processes a single image row.
 */
public class SNRCalculation extends Thread {

	/**
	 * Map of parameter <code>a</code>.
	 */
	private final float[] aMap;
	/**
	 * For each input image, with an index larger than edgeIndex, a SNR image is
	 * calculated.
	 */
	private final EFTEMImage[] array_EFTEMImages;
	/**
	 * This constant determines if the derivative from a is used.
	 */
	private final int da = 0;
	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * This constant determines if the derivative from r is used.
	 */
	private final int dr = 1;
	/**
	 * The index of the first post-edge image.
	 */
	private final int edgeIndex;
	/**
	 * The energy loss of the {@link EFTEMImage} at the array position imageIndex.
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
	 * <code>x + y * width</code>
	 */
	private int index;
	/**
	 * Map of parameter <code>r</code>.
	 */
	private final float[] rMap;
	/**
	 * The pixels of the {@link EFTEMImage} at the array position imageIndex.
	 */
	private final float[] signal;
	/**
	 * A shortcut to access the instance of {@link ThreadInterface}.
	 */
	private final ThreadInterface threadInterface = ThreadInterface.getInstance();
	/**
	 * The x coordinate of the processed pixel.
	 */
	private int currentX;
	/**
	 * The y coordinate of the processed pixel.
	 */
	private final int currentY;

	/**
	 * The constructor creates a new instance of {@link SNRCalculation} for the
	 * SNR calculation of each pixel at an image row.
	 *
	 * @param y The image row that is processed by the new instance of
	 *          {@link SNRCalculation}
	 * @param imageIndex The position of the processed image at the sorted input
	 *          {@link ImageStack}. This is used to save the results of the
	 *          calculation by using the {@link DatasetAPI}.
	 */
	public SNRCalculation(final int y, final int imageIndex) {
		super();
		threadInterface.addThread();
		this.currentX = 0;
		this.currentY = y;
		this.imageIndex = imageIndex;
		edgeIndex = datasetAPI.getEdgeIndex();
		array_EFTEMImages = datasetAPI.getEFTEMImageArray();
		eLoss = array_EFTEMImages[imageIndex].getELoss();
		rMap = datasetAPI.getRMap();
		aMap = datasetAPI.getAMap();
		signal = datasetAPI.getMap(imageIndex - edgeIndex).getPixels();
		errorValues = datasetAPI.getErrorMap();
	}

	/**
	 * Calculating the background again is faster than reading it from the saved
	 * results, especially with a increasing number of pre-edge images.
	 *
	 * @param energyLoss The energy loss that is used for the calculation.
	 * @return The background signal.
	 */
	private float calcBG(final float energyLoss) {
		final float bg = (float) Math.exp(aMap[index] - rMap[index] * Math.log(
			energyLoss));
		return bg;
	}

	/**
	 * @return The covariance of parameter <code>r</code> and <code>a</code>.
	 */
	private double covar() {
		return m(1) * varR();
	}

	/**
	 * By using a switch statement the power law can be differentiated with
	 * respect to different variables.
	 *
	 * @param x The power law is differentiated with respect to x
	 * @return The derivative of the power law at E=eloss.
	 */
	private double dI(final int x) {
		switch (x) {
			case da:
				return calcBG(eLoss);
			case dr:
				return -Math.log(eLoss) * calcBG(eLoss);
		}
		return Double.NaN;
	}

	/**
	 * This is a weight. The numerator is a sum of <code>bg(eLoss)*eloss^k</code>
	 * and the denominator is the sum of <code>bg(eLoss)</code>.
	 *
	 * @param k Can be 1 or 2.
	 * @return A linear or cubic weight.
	 */
	private double m(final int k) {
		double sum1 = 0;
		for (int i = 0; i < edgeIndex; i++) {
			sum1 += calcBG(array_EFTEMImages[i].getELoss()) * Math.pow(Math.log(
				array_EFTEMImages[i].getELoss()), k);
		}
		double sum2 = 0;
		for (int i = 0; i < edgeIndex; i++) {
			sum2 += calcBG(array_EFTEMImages[i].getELoss());
		}
		return sum1 / sum2;
	}

	@Override
	public void run() {
		final int width = datasetAPI.getWidth();
		final float[] snr = new float[width];
		final float[] sigma2 = new float[width];
		for (currentX = 0; currentX < width; currentX++) {
			index = currentX + currentY * width;
			if (errorValues[index] == 0) {
				sigma2[currentX] = (float) sigma2();
				double snrAtPixel = signal[index] / Math.sqrt(signal[index] + calcBG(
					eLoss) + sigma2[currentX]);
				if (Double.isNaN(snrAtPixel) | Double.isInfinite(snrAtPixel)) {
					snrAtPixel = 0;
				}
				snr[currentX] = (float) snrAtPixel;
			}
			else {
				snr[currentX] = PluginConstants.VALUE_CALCULATION_FAILED;
			}
		}
		datasetAPI.saveSNR(imageIndex, currentY * width, snr);
		datasetAPI.saveSigma2(imageIndex, currentY * width, sigma2);
		threadInterface.removeThread(ThreadInterface.SNR);
	}

	/**
	 * This methods calculates sigma^2. This calculation is split up in several
	 * methods.
	 *
	 * @return Sigma^2
	 */
	private double sigma2() {
		return Math.pow(dI(da), 2) * varA() + Math.pow(dI(dr), 2) * varR() + 2 * dI(
			da) * dI(dr) * covar();
	}

	/**
	 * @return The variance of parameter <code>a</code>.
	 */
	private double varA() {
		return m(2) * varR();
	}

	/**
	 * @return The variance of parameter <code>r</code>.
	 */
	private double varR() {
		double sum = 0;
		for (int i = 0; i < edgeIndex; i++) {
			sum += calcBG(array_EFTEMImages[i].getELoss()) * Math.pow(Math.log(
				array_EFTEMImages[i].getELoss()) - m(1), 2);
		}
		return 1 / sum;
	}
}
