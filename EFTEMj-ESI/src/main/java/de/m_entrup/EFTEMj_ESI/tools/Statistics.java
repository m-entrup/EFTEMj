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
	 * @return "getMean / getMedian / getStdv"
	 */
	public String getAllAsString() {
		final double meanValue = getMean();
		final double medianValue = getMedian();
		final double stdvValue = getStdv();
		final String output = String.format(Locale.ENGLISH, "(mean/median/stdv): %.3f / %.3f / %.3f", meanValue,
				medianValue, stdvValue);
		return output;
	}

	/**
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
		} else {
			median = (values[(int) Math.floor(count / 2)] + values[(int) Math.ceil(count / 2)]) / 2;
		}
		return median;
	}

	/**
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
