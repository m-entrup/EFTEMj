
package de.m_entrup.EFTEMj_ESI.map;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;

/**
 * This class calculates the parameter of the background model pixel by pixel.
 * The used background model is the power law. Each instance of
 * {@link PowerLawFitCalculation} processes an image row and saves the result.
 * To calculate the background the maximum likelihood estimation is used. All
 * errors that occur during the calculation are classified and logged.
 */
public class PowerLawFitCalculation extends Thread {

	private final int DEBUGCODE = 1;
	private AbstractFitRoutine typeOfFit;
	/**
	 * The exit condition for the MLE. There is a static method to change this
	 * value.
	 */
	private static float epsilon = PluginConstants.EPSILON;
	/**
	 * The lower limit for the parameter <code>r</code>. There is a static method
	 * to change this value.
	 */
	private static float rLimit = PluginConstants.R_LIMIT;

	/**
	 * @return The current value of epsilon.
	 */
	public static float getEpsilon() {
		return epsilon;
	}

	/**
	 * @return The current value of rLimit.
	 */
	public static float getRLimit() {
		return rLimit;
	}

	/**
	 * The exit condition is saved as a static field so each instance can access
	 * it. This method is used to change the value.
	 *
	 * @param newEpsilon The new exit condition that is set by the GUI.
	 */
	public static void setEpsilon(final float newEpsilon) {
		epsilon = newEpsilon;
	}

	/**
	 * The r-limit is saved as a static filed so each instance can access it. This
	 * method is used to change the value.
	 *
	 * @param newRLimit The new r-limit that is set by the GUI.
	 */
	public static void setRLimit(final float newRLimit) {
		rLimit = newRLimit;
	}

	/**
	 * The second parameter of the power law. It is derived from r.
	 */
	private float[] a;
	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * This field stores the error type of each pixel before it is saved.
	 */
	short[] errorType;
	/**
	 * The parameter of the power law that is calculated. This field saves the
	 * result of each x-position of the processed image line.
	 */
	private float[] r;

	/**
	 * A shortcut to access the instance of {@link ThreadInterface}.
	 */
	private final ThreadInterface threadInterface = ThreadInterface.getInstance();
	/**
	 * The y coordinate of the processed pixel.
	 */
	private final int y;

	/**
	 * The constructor creates a new instance of {@link PowerLawFitCalculation}
	 * for the parameter calculation of each pixel at an image row.
	 *
	 * @param y The image row that is processed by the new instance of
	 *          {@link PowerLawFitCalculation}.
	 */
	public PowerLawFitCalculation(final int y) {
		super();
		threadInterface.addThread();
		this.y = y;
	}

	@Override
	public void run() {
		selectTypeOfFit();
		final int width = datasetAPI.getWidth();
		r = new float[width];
		a = new float[width];
		errorType = new short[width];
		for (int x = 0; x < width; x++) {
			final int index = x + y * width;
			errorType[x] = typeOfFit.calculateByPixel(index);
			r[x] = typeOfFit.getR(index);
			a[x] = typeOfFit.getA(index);
		}
		// ToDo Call a instance of FitCalculation
		datasetAPI.saveR(y * width, r);
		datasetAPI.saveA(y * width, a);
		datasetAPI.saveError(y * width, errorType);
		threadInterface.removeThread(ThreadInterface.MLE);
	}

	private void selectTypeOfFit() {
		switch (DEBUGCODE) {
			case 1:
				typeOfFit = new MLERoutine(epsilon, rLimit);
				break;
			default:
				typeOfFit = new MLERoutine(epsilon, rLimit);
				break;
		}
	}

}
