
package de.m_entrup.EFTEMj_ESI.map;

import java.util.HashMap;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.dataset.EFTEMImage;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import ij.ImageStack;

public class MLERoutine extends AbstractFitRoutine {

	/**
	 * The exit condition for the MLE. There is a static method to change this
	 * value.
	 */
	private float epsilon = PluginConstants.EPSILON;
	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * The selected {@link ImageStack} as a sorted array of {@link EFTEMImage}s.
	 */
	private final EFTEMImage[] array_EFTEMImages;
	/**
	 * The index of the first post-edge image.
	 */
	private final int edgeIndex;
	/**
	 * <code>x + y * width</code>
	 */
	private int currentIndex;

	private final HashMap<Integer, Float> r;
	private final HashMap<Integer, Float> a;

	public MLERoutine() {
		edgeIndex = datasetAPI.getEdgeIndex();
		array_EFTEMImages = datasetAPI.getEFTEMImageArray();
		r = new HashMap<Integer, Float>();
		a = new HashMap<Integer, Float>();
	}

	public MLERoutine(final float epsilon) {
		edgeIndex = datasetAPI.getEdgeIndex();
		array_EFTEMImages = datasetAPI.getEFTEMImageArray();
		r = new HashMap<Integer, Float>();
		a = new HashMap<Integer, Float>();
		this.epsilon = epsilon;
	}

	/**
	 * The calculation of <code>r</code> and <code>a</code> is done by this
	 * method. Other methods are used to keep this method short. The different
	 * parts are divided by comments.
	 */
	@Override
	public short calculateByPixel(final int index) {
		this.currentIndex = index;
		if (isLessThanZero()) {
			r.put(index, 0f);
			a.put(index, 0f);
			return PluginConstants.ERROR__SIGNAL_LESS_THAN_ZERO;
		}
		// 4 is a random start value for r.
		double rn = 4.0;
		// rn-rn_prev has to be larger than epsilon
		double rn_prev = rn + 2 * epsilon;
		// converganceCounter is used to stop the iteration if the calculation
		// of r does not converge.
		int convergenceCounter = 0;
		// this variable saves rn-r of the previous iteration. The initial value
		// is a random one. To trigger no convergence error the value is such
		// huge.
		double diff = 10.0;
		double num;
		double denum;
		// Start: Iteration to calculate r
		while (Math.abs(rn_prev - rn) > epsilon) {
			rn_prev = rn;
			num = numerator(rn);
			denum = denominator(rn);
			rn = rn_prev - num / denum;
			// Check for a NaN error
			if (Double.isNaN(rn) | Double.isInfinite(rn)) {
				r.put(index, 0f);
				a.put(index, 0f);
				return PluginConstants.ERROR__NAN;
			}
			// Checks for a convergence error. The combination of the 2. and 3.
			// if statement prevents an infinite number of iterations.
			if (Math.abs(rn_prev - rn) == diff) {
				r.put(index, 0f);
				a.put(index, 0f);
				return PluginConstants.ERROR__CONVERGENCE;
			}
			if (Math.abs(rn_prev - rn) > diff) {
				convergenceCounter++;
			}
			if (convergenceCounter >= 25) {
				// r is set to NaN to make a clean up (see below)
				r.put(index, 0f);
				a.put(index, 0f);
				return PluginConstants.ERROR__CONVERGENCE;
			}
			diff = Math.abs(rn_prev - rn);
		}
		// End: Iteration to calculate r
		// Parameter clean up:
		// This is done to optimise the display of the parameter maps.
		if (Double.isNaN(rn) | Double.isInfinite(rn)) {
			r.put(index, 0f);
			a.put(index, 0f);
			return PluginConstants.ERROR__NAN;
		}
		// Check if a can be calculated
		final double value = sumCounts() / sumExp(rn, 0);
		if (value < 0) {
			a.put(index, 0f);
			r.put(index, (float) rn);
			return PluginConstants.ERROR__A_NOT_POSSIBLE_TO_CALCULATE;
		}
		// Calculation of parameter a
		a.put(index, (float) Math.log(value));
		// Check for a NaN error
		if (Double.isNaN(a.get(index))) {
			a.put(index, 0f);
			r.put(index, (float) rn);
			return PluginConstants.ERROR__NAN;
		}
		// calculation of a is ok
		r.put(index, (float) rn);
		return PluginConstants.ERROR__NON;
	}

	private boolean isLessThanZero() {
		for (int i = 0; i < array_EFTEMImages.length; i++) {
			if (array_EFTEMImages[i].getPixels()[currentIndex] < 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The denominator of the equation to calculate <code>r</code>.
	 *
	 * @param rn The value of <code>r</code> at the current iteration.
	 * @return The result of the equation at the denominator.
	 */
	private double denominator(final double rn) {
		final double s0 = sumExp(rn, 0);
		final double s2 = sumExp(rn, 2);
		return Math.pow((sumExp(rn, 1) / s0), 2) - s2 / s0;
	}

	/**
	 * The numerator of the equation to calculate <code>r</code>.
	 *
	 * @param rn The value of <code>r</code> at the current iteration.
	 * @return The result of the equation at the numerator.
	 */
	private double numerator(final double rn) {
		return sumExp(rn, 1) / sumExp(rn, 0) - weight();
	}

	/**
	 * Sums the counts of all pre-edge images at the currently processed pixel
	 * position.
	 *
	 * @return Sum of the counts.
	 */
	private double sumCounts() {
		double value = 0;
		for (int i = 0; i < edgeIndex; i++) {
			value += array_EFTEMImages[i].getPixels()[currentIndex];
		}
		return value;
	}

	/**
	 * Calculates the sum of E_i^{exponent}.
	 *
	 * @param rn The value of <code>r</code> at the current iteration.
	 * @param exponent can be 0, 1, or 2.
	 * @return The sum of power functions.
	 */
	private double sumExp(final double rn, final int exponent) {
		double value = 0;
		for (int i = 0; i < edgeIndex; i++) {
			value += (Math.pow(Math.log(array_EFTEMImages[i].getELoss()), exponent)) *
				Math.exp(-1 * rn * Math.log(array_EFTEMImages[i].getELoss()));
		}
		return value;
	}

	/**
	 * The MLE uses a weight that is calculated at this method.
	 *
	 * @return A weighted mean energy loss.
	 */
	private double weight() {
		double value1 = 0;
		double value2 = 0;
		for (int i = 0; i < edgeIndex; i++) {
			value1 += Math.log(array_EFTEMImages[i].getELoss()) * array_EFTEMImages[i]
				.getPixels()[currentIndex];
			value2 += array_EFTEMImages[i].getPixels()[currentIndex];
		}
		// If true this will result in 0/1
		if (value2 == 0) value2 = 1;
		return value1 / value2;
	}

	@Override
	public float getR(final int index) {
		return r.get(index);
	}

	@Override
	public float getA(final int index) {
		return a.get(index);
	}
}
