/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2016, Michael Entrup b. Epping
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package de.m_entrup.EFTEMj_EELS.deconvolve;

import java.awt.Window;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DctNormalization;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastCosineTransformer;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import de.m_entrup.EFTEMj_EELS.importer.EELS_SpectrumFromMsaPlugin;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * @author Michael Entrup b. Epping
 */
public class EELS_DeconvolvePlugin implements PlugInFilter {

	private float[] xValues;
	private float[] yValues;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(final String arg, final ImagePlus imp) {
		if (imp != null) {
			final Window win = imp.getWindow();
			if (win instanceof PlotWindow && win.isVisible()) {
				final PlotWindow plotWin = (PlotWindow) win;
				final Plot plot = plotWin.getPlot();
				xValues = plot.getXValues();
				yValues = plot.getYValues();
				return DOES_ALL;
			}
		}
		return DONE;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(final ImageProcessor ip) {
		final FastFourierTransformer fft = new FastFourierTransformer(
			DftNormalization.STANDARD);
		final FastCosineTransformer fct = new FastCosineTransformer(
			DctNormalization.STANDARD_DCT_I);
		final FastFourierTransformer fftUnitary = new FastFourierTransformer(
			DftNormalization.UNITARY);
		final Complex[] yFft = fft.transform(makePowerOfTwo(convertFloatsToDoubles(
			yValues)), TransformType.FORWARD);
		final Complex[] yFftUnitary = fftUnitary.transform(makePowerOfTwo(
			convertFloatsToDoubles(yValues)), TransformType.FORWARD);
		final Complex[] yFftInverse = fft.transform(yFft, TransformType.INVERSE);
		final Plot plot = new Plot("FFT", "x", "y", createFftXAxis(xValues, yFft),
			absOfComplex(yFft));
		plot.setColor("red");
		plot.addPoints(createFftXAxis(xValues, yFftUnitary), absOfComplex(
			yFftUnitary), Plot.LINE);
		plot.setColor("black");
		plot.setAxisYLog(true);
		plot.setLimitsToFit(true);
		plot.show();
		final Plot plotInverse = new Plot("Inverse FFT", "x", "y",
			convertFloatsToDoubles(xValues), absOfComplex(yFftInverse));
		plotInverse.setColor("red");
		plotInverse.addPoints(xValues, yValues, Plot.LINE);
		plotInverse.setColor("black");
		plotInverse.show();
		final double[] yFct = fct.transform(makePowerOfTwoPlusOne(
			convertFloatsToDoubles(yValues)), TransformType.FORWARD);
		final Plot plotCosinus = new Plot("FCT", "x", "y", createFctXAxis(xValues,
			yFct), yFct);
		plotCosinus.setLimitsToFit(true);
		plotCosinus.show();
	}

	public static double[] convertFloatsToDoubles(final float[] input) {
		if (input == null) {
			return null;
		}
		final double[] output = new double[input.length];
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i];
		}
		return output;
	}

	public static double[] absOfComplex(final Complex[] input) {
		if (input == null) {
			return null;
		}
		final double[] output = new double[input.length];
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i].abs();
		}
		return output;
	}

	public static double[] createFftXAxis(float[] xAxis, Complex[] fft) {
		final double[] output = new double[fft.length];
		double step = 1. / (xAxis[xAxis.length - 1] - xAxis[0]);
		for (int i = 0; i < output.length; i++) {
			output[i] = i * step;
		}
		return output;
	}

	public static double[] createFctXAxis(float[] xAxis, double[] fft) {
		final double[] output = new double[fft.length];
		double step = 1. / (xAxis[xAxis.length - 1] - xAxis[0]);
		for (int i = 0; i < output.length; i++) {
			output[i] = i * step;
		}
		return output;
	}

	public static double[] makePowerOfTwo(double[] input) {
		if (input == null) {
			return null;
		}
		int length = 2;
		while (length < input.length) {
			length *= 2;
		}
		final double[] output = new double[2 * length];
		Arrays.fill(output, 0);
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i];
		}
		return output;
	}

	public static double[] makePowerOfTwoPlusOne(double[] input) {
		if (input == null) {
			return null;
		}
		int length = 2;
		while (length < input.length) {
			length *= 2;
		}
		final double[] output = new double[2 * length + 1];
		Arrays.fill(output, 0);
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i];
		}
		return output;
	}

	public static void main(final String[] args) throws URISyntaxException {
		new ImageJ();
		final URI uri = EELS_DeconvolvePlugin.class.getResource(
			"/examples/FeCr_low-loss.msa").toURI();
		final Plot plot = new EELS_SpectrumFromMsaPlugin().getPlot(new File(uri));
		plot.show();
		IJ.runPlugIn(EELS_DeconvolvePlugin.class.getName(), "");
	}

}
