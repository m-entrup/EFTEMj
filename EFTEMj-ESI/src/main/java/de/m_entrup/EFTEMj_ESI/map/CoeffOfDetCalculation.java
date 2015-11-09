
package de.m_entrup.EFTEMj_ESI.map;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.dataset.EFTEMImage;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;

/**
 * This class calculates the coefficient of determination pixel by pixel. Each
 * instance processes a single image row.
 */
public class CoeffOfDetCalculation extends Thread {

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
	 * The constructor creates a new instance of {@link CoeffOfDetCalculation} for
	 * the coefficient of determination calculation of each pixel at an image row.
	 *
	 * @param y The image row that is processed by the new instance of
	 *          {@link CoeffOfDetCalculation}.
	 */
	public CoeffOfDetCalculation(final int y) {
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
	 * @return The sum of squares of residuals, also called the residual sum of
	 *         squares.
	 */
	private double residual() {
		double residuen = 0;
		for (int i = 0; i < edgeIndex; i++) {
			residuen += Math.pow(array_EFTEMImages[i].getPixels()[index] - Math.exp(
				aMap[index] - rMap[index] * Math.log(array_EFTEMImages[i].getELoss())),
				2);
		}
		return residuen;
	}

	@Override
	public void run() {
		final int width = datasetAPI.getWidth();
		final float[] coeffOfDetArray = new float[width];
		for (x = 0; x < width; x++) {
			index = x + y * width;
			if (errorMap[index] == 0) {
				coeffOfDetArray[x] = (float) (1 - residual() / variationY());
			}
			else {
				coeffOfDetArray[x] = 0;
			}
		}
		datasetAPI.saveCOD(y * width, coeffOfDetArray);
		threadInterface.removeThread(ThreadInterface.COD);
	}

	/**
	 * @return The total sum of squares (proportional to the sample variance).
	 */
	private double variationY() {
		double mittel = 0;
		for (int i = 0; i < edgeIndex; i++) {
			mittel += array_EFTEMImages[i].getPixels()[index] / edgeIndex;
		}
		double variation = 0;
		for (int i = 0; i < edgeIndex; i++) {
			variation += Math.pow(array_EFTEMImages[i].getPixels()[index] - mittel,
				2);
		}
		return variation;
	}
}
