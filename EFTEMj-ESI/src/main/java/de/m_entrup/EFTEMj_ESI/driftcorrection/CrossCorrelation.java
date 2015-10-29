
package de.m_entrup.EFTEMj_ESI.driftcorrection;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import ij.ImageStack;
import ij.process.FloatProcessor;

/**
 * An instance of this class creates a map of cross correlation coefficients for
 * each possible shift between two images. Each row of this cross correlation
 * coefficients image is calculated at an independent thread of
 * {@link CrossCorrelation}. This class uses cropped images. For the reference
 * image the cropped area has to be larger in order to allow shifting.
 */
public class CrossCorrelation extends Thread {

	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * The index of the processed image at the {@link ImageStack}.
	 */
	private final int index;
	/**
	 * A shortcut to access the instance of {@link ThreadInterface}.
	 */
	private final ThreadInterface threadInterface = ThreadInterface.getInstance();
	/**
	 * The x-coordinate of the processed pixel of the crosscorrelation coefficient
	 * map. This corresponds to a shift in x-direction between the analysed
	 * images.
	 */
	private int x;
	/**
	 * The y-coordinate of the processed pixel of the crosscorrelation coefficient
	 * map. This corresponds to a shift in y-direction between the analysed
	 * images.
	 */
	private final int y;

	/**
	 * Each {@link CrossCorrelation} thread processes a single image row. At the
	 * constructor it is declared which image and row are processed.
	 *
	 * @param index The index the stack, it starts a 0.
	 * @param y The row of the crosscorrelation coefficient map.
	 */
	public CrossCorrelation(final int index, final int y) {
		super();
		threadInterface.addThread();
		this.x = 0;
		this.y = y;
		this.index = index;
	}

	@Override
	public void run() {
		final int width = 2 * datasetAPI.getDelta() + 1;
		final FloatProcessor image = datasetAPI.getCroppedImage(index);
		final FloatProcessor referenceImage = datasetAPI.getCroppedReferenceImage();
		final float[] array_crossCorrelationCoefficients = new float[width];
		for (x = 0; x < width; x++) {
			double tSum = 0;
			double tSquareSum = 0;
			double covariance = 0;
			/*
			 * i and j are the coordinates of the cropped image that is
			 * analysed. The reference image is shifted by x and y.
			 */
			for (int j = 0; j < image.getHeight(); j++) {
				for (int i = 0; i < image.getWidth(); i++) {
					final double templateQ = referenceImage.getf(x + i, y + j);
					final double imageQ = image.getf(i, j);
					tSum += templateQ;
					tSquareSum += templateQ * templateQ;
					covariance += templateQ * imageQ;
				}
			}
			final double tMean = tSum / (image.getWidth() * image.getHeight());
			array_crossCorrelationCoefficients[x] = (float) ((covariance - (image
				.getWidth() * image.getHeight()) * tMean * datasetAPI.getMean(index)) /
				(Math.sqrt(tSquareSum - (image.getWidth() * image.getHeight()) * tMean *
					tMean) * datasetAPI.getSigma(index)));
		}
		datasetAPI.saveCross(array_crossCorrelationCoefficients, index, y * width);
		threadInterface.removeThread(ThreadInterface.CROSS);
	}
}
