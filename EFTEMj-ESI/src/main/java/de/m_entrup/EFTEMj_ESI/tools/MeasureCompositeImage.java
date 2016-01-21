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

import java.awt.FileDialog;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;

import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class MeasureCompositeImage implements PlugInFilter {

	/**
	 * This inner Class is used to calculate the different measurements. Each
	 * measurement has its own method.
	 */
	private class Measurement {

		private class MyRunnable implements Runnable {

			private final Measurement task;

			public MyRunnable(final Measurement task) {
				super();
				this.task = task;
			}

			@Override
			public void run() {
				task.showResult();
			}
		}

		private float[] dataFiltered;
		private final FloatProcessor dataProc;
		private final FloatProcessor errorProc;
		private final int h;
		private final ImagePlus impToMeasure;
		private double mean;
		private boolean meanCalcIsDone = false;
		private final double pixels;
		private double pixelsRoi;
		private Vector<String> result;
		private Rectangle roi;

		private final int w;

		public Measurement(final ImagePlus impToMeasure) {
			this.impToMeasure = impToMeasure;
			dataProc = (FloatProcessor) impToMeasure.getStack().getProcessor(2);
			errorProc = (FloatProcessor) impToMeasure.getStack().getProcessor(1);
			w = impToMeasure.getWidth();
			h = impToMeasure.getHeight();
			pixels = w * h;
			if (impToMeasure.getRoi() != null) {
				roi = impToMeasure.getRoi().getBounds();
				pixelsRoi = roi.getWidth() * roi.getHeight();
			}
			dataFiltered = getFilteredData();
		}

		private float[] getExtremes() {
			// extremes[0] = value of max
			// extremes[1] = x position of max
			// extremes[2] = y position of max
			// extremes[3] = value of min
			// extremes[4] = x position of min
			// extremes[5] = y position of min
			final float extremes[] = new float[6];
			int offsetX;
			int offsetY;
			int deltaX;
			int deltaY;
			if (roi != null) {
				offsetX = (int) roi.getX();
				offsetY = (int) roi.getY();
				deltaX = (int) roi.getWidth();
				deltaY = (int) roi.getHeight();
			}
			else {
				offsetX = 0;
				offsetY = 0;
				deltaX = w;
				deltaY = h;
			}
			extremes[0] = dataProc.getf(offsetX, offsetY);
			extremes[1] = offsetX;
			extremes[2] = offsetY;
			extremes[3] = dataProc.getf(offsetX, offsetY);
			extremes[4] = offsetX;
			extremes[5] = offsetY;
			float value;
			for (int y = offsetY; y < offsetY + deltaY; y++) {
				for (int x = offsetX + 1; x < offsetX + deltaX; x++) {
					value = dataProc.getf(x, y);
					if (value < extremes[0]) {
						extremes[0] = value;
						extremes[1] = x;
						extremes[2] = y;
					}
					else {
						if (value > extremes[3]) {
							extremes[3] = value;
							extremes[4] = x;
							extremes[5] = y;
						}
					}
				}
			}
			return extremes;
		}

		private float[] getFilteredData() {
			if (dataFiltered == null) {
				final float[] dataArray = (float[]) dataProc.getPixels();
				final float[] errorArray = (float[]) errorProc.getPixels();
				if (roi == null) {
					int noErrorCount = 0;
					for (int i = 0; i < pixels; i++) {
						if (errorArray[i] == 0) {
							noErrorCount++;
						}
					}
					dataFiltered = new float[noErrorCount];
					int j = 0;
					for (int i = 0; i < pixels; i++) {
						if (errorArray[i] == 0) {
							dataFiltered[j] = dataArray[i];
							j++;
						}
					}
				}
				else {
					int noErrorCount = 0;
					for (int y = (int) roi.getY(); y < roi.getY() + roi
						.getHeight(); y++)
					{
						for (int x = (int) roi.getX(); x < roi.getX() + roi
							.getWidth(); x++)
						{
							if (errorProc.getf(x, y) == 0) {
								noErrorCount++;
							}
						}
					}
					dataFiltered = new float[noErrorCount];
					int j = 0;
					for (int y = (int) roi.getY(); y < roi.getY() + roi
						.getHeight(); y++)
					{
						for (int x = (int) roi.getX(); x < roi.getX() + roi
							.getWidth(); x++)
						{
							if (errorProc.getf(x, y) == 0) {
								dataFiltered[j] = dataProc.getf(x, y);
								j++;
							}
						}
					}
				}
			}
			return dataFiltered;
		}

		private double getMean() {
			if (meanCalcIsDone == true) {
				return mean;
			}
			mean = 0;
			final int pixelCount = dataFiltered.length;
			for (int i = 0; i < pixelCount; i++) {
				mean += 1.0 / pixelCount * dataFiltered[i];
			}
			meanCalcIsDone = true;
			return mean;
		}

		private double getMedian() {
			double median = 0;
			Arrays.sort(dataFiltered);
			if (dataFiltered.length % 2 == 0) {
				median = 0.5 * (dataFiltered[dataFiltered.length / 2] +
					dataFiltered[(dataFiltered.length / 2) - 1]);
			}
			else {
				median = dataFiltered[(int) Math.floor(dataFiltered.length / 2)];
			}
			return median;
		}

		private int getPixelsUsed() {
			int pixelsUsed;
			pixelsUsed = dataFiltered.length;
			return pixelsUsed;
		}

		private double getSTDV() {
			double stdv = 0;
			final double meanValue = getMean();
			final int pixelCount = dataFiltered.length;
			for (int i = 0; i < pixelCount; i++) {
				stdv += 1.0 / pixelCount * Math.pow(dataFiltered[i] - meanValue, 2);
			}
			stdv = Math.sqrt(stdv);
			return stdv;
		}

		private double getSumOfCounts() {
			double sumOfCounts = 0;
			for (int i = 0; i < dataFiltered.length; i++) {
				sumOfCounts += dataFiltered[i];
			}
			return sumOfCounts;
		}

		private void measure() {
			final String title = impToMeasure.getTitle();
			System.out.println(String.format(Locale.ENGLISH, "Measurements on \"%s\"",
				title));
			if (roi != null) {
				System.out.println(String.format(
					"A ROI is set:%n x = %.0f%n y = %.0f%n w = %.0f%n h = %.0f", roi
						.getX(), roi.getY(), roi.getWidth(), roi.getHeight()));
				System.out.println(String.format(Locale.ENGLISH, " %.0f of %.0f pixels",
					pixelsRoi, pixels));
			}
			final int pixelsUsed = getPixelsUsed();
			double pixelsUsedPercent;
			if (roi == null) {
				pixelsUsedPercent = 1.0 * pixelsUsed / pixels;
			}
			else {
				pixelsUsedPercent = 1.0 * pixelsUsed / pixelsRoi;
			}
			final double sumOfCounts = getSumOfCounts();
			final double median = getMedian();
			final double meanValue = getMean();
			final double stdv = getSTDV();
			final float[] extremes = getExtremes();
			result = new Vector<String>();
			if (roi != null) {
				result.add(String.format("ROI: x=%d,  y=%d, w=%d, h=%d, px=%d (%s)",
					roi.x, roi.y, roi.width, roi.height, roi.width * roi.height,
					NumberFormat.getPercentInstance().format(1.0 * roi.width *
						roi.height / w / h)));
			}
			result.add(String.format(Locale.ENGLISH,
				"Pixels used for measurement: %d (%s)", pixelsUsed, NumberFormat
					.getPercentInstance().format(pixelsUsedPercent)));
			result.add(String.format(Locale.ENGLISH, "Sum of counts: %.3f",
				sumOfCounts));
			result.add(String.format(Locale.ENGLISH, "Median: %.3f", median));
			result.add(String.format(Locale.ENGLISH, "Mean: %.3f", meanValue));
			result.add(String.format(Locale.ENGLISH, "Standarddev.: %.3f", stdv));
			result.add(String.format(Locale.ENGLISH,
				"Minimum: %.3f at %.0f/%.0f%nMaximum: %.3f at %.0f/%.0f", extremes[0],
				extremes[1], extremes[2], extremes[3], extremes[4], extremes[5]));
			final MyRunnable runnable = new MyRunnable(this);
			new Thread(runnable).start();
		}

		private void showResult() {
			final GenericDialog gd = new GenericDialog(PluginMessages.getString(
				"Titel.MeasureComposite"));
			String text = impToMeasure.getTitle() + PluginConstants.LINE_SEPARATOR;
			int maxLength = 0;
			for (int i = 0; i < result.size(); i++) {
				if (result.get(i).length() > maxLength) {
					maxLength = result.get(i).length();
				}
				text += result.get(i) + PluginConstants.LINE_SEPARATOR;
			}
			gd.addTextAreas(text, null, result.size() + 1, maxLength + 5);
			gd.addMessage(PluginMessages.getString("Label.SaveMeasurement"));
			gd.setResizable(false);
			gd.showDialog();
			if (gd.wasCanceled()) {
				return;
			}
			final FileDialog fDialog = new FileDialog(gd, PluginMessages.getString(
				"Titel.SaveMeasurement"), FileDialog.SAVE);
			/*
			 * MultiMode is not available in Java 6
			 * fDialog.setMultipleMode(false);
			 */
			fDialog.setDirectory(IJ.getDirectory("image"));
			fDialog.setFile("Measurement_" + imp.getTitle() + ".txt");
			fDialog.setVisible(true);
			if (fDialog.getFile() != null) {
				final String path = fDialog.getDirectory() + System.getProperty(
					"file.separator") + fDialog.getFile();
				IJ.saveString(text, path);
			}
		}
	}

	private ImagePlus imp;

	@Override
	public void run(final ImageProcessor ip) {
		if (imp.getStackSize() == 2 & imp.isComposite()) {
			final Measurement measurment = new Measurement(imp);
			measurment.measure();
		}
		else {
			IJ.showMessage(PluginMessages.getString("Error.NoComposite"));
		}
	}

	@Override
	public int setup(final String arg, final ImagePlus imp) {
		this.imp = imp;
		return DOES_32 + STACK_REQUIRED + NO_CHANGES;
	}

}
