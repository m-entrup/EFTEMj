
package de.m_entrup.EFTEMj_ESI.tools;

import java.util.Locale;

/**
 * A class containing static methods to evaluate the results of the elemental
 * map calculation. All methods do only regard pixels with no error.
 */
public class Statistics {

	private double mean = Double.NaN;
	private double median = Double.NaN;
	private double stdv = Double.NaN;
	private int noErrorCount = -1;
	private final float[] input;
	private final float[] errors;

	public Statistics(final float[] input, final float[] errors) {
		this.input = input;
		this.errors = errors;
	}

	/**
	 * Creates a string with the pattern "mean / median / stdv".
	 *
	 * @param input Pixel array of the input image.
	 * @param errors Pixel array of the error map.
	 * @return "getMean / getMedian / getStdv"
	 */
	public String getAllAsString() {
		final double mean = getMean();
		final double median = getMedian();
		final double stdv = getStdv();
		final String output = String.format(Locale.ENGLISH,
			"(mean/median/stdv): %.3f / %.3f / %.3f", mean, median, stdv);
		return output;
	}

	/**
	 * @param input Pixel array of the input image.
	 * @param errors Pixel array of the error map.
	 * @return The mean of all pixels without an error.
	 */
	public double getMean() {
		if (!Double.isNaN(mean)) {
			return mean;
		}
		mean = 0;
		final int count = noErrorCount();
		for (int i = 0; i < input.length; i++) {
			if (errors[i] == 0) {
				mean += input[i] / count;
			}
		}
		return mean;
	}

	/**
	 * @param input Pixel array of the input image.
	 * @param errors Pixel array of the error map.
	 * @return The median of all pixels without an error.
	 */
	public double getMedian() {
		if (!Double.isNaN(median)) {
			return median;
		}
		median = 0;
		final int count = noErrorCount();
		final float[] values = new float[count];
		int j = 0;
		for (int i = 0; i < input.length; i++) {
			if (errors[i] == 0) {
				values[j] = input[i];
				j++;
			}
		}
		if (count % 2 == 1) {
			median = values[(int) Math.floor(count / 2)];
		}
		else {
			median = (values[(int) Math.floor(count / 2)] + values[(int) Math.ceil(
				count / 2)]) / 2;
		}
		return median;
	}

	/**
	 * @param input Pixel array of the input image.
	 * @param errors Pixel array of the error map.
	 * @return The standard deviation of all pixels without an error.
	 */
	public double getStdv() {
		if (!Double.isNaN(stdv)) {
			return stdv;
		}
		stdv = 0;
		final int count = noErrorCount();
		double sum = 0;
		for (int i = 0; i < input.length; i++) {
			if (errors[i] == 0) {
				sum += Math.pow(input[i] - getMean(), 2) / (count - 1);
			}
		}
		stdv = Math.sqrt(sum);
		return stdv;
	}

	private int noErrorCount() {
		if (noErrorCount > 1) {
			return noErrorCount;
		}
		noErrorCount = 0;
		for (int i = 0; i < errors.length; i++) {
			if (errors[i] == 0) {
				noErrorCount++;
			}
		}
		return noErrorCount;
	}

}
