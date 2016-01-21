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

package de.m_entrup.EFTEMj_ESI.plugin;

import de.m_entrup.EFTEMj_ESI.dqe.DQE_ByBinning;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class PluginDQE implements PlugInFilter {

	private class MyRunnable implements Runnable {

		private final DQE_ByBinning task;

		public MyRunnable(final DQE_ByBinning task) {
			super();
			this.task = task;
		}

		@Override
		public void run() {
			task.saveResultAndShowPlot();
		}
	}

	private int border;
	private ImagePlus imp;
	private int maxBinning;

	private float sensitivity;

	private String createSummary() {
		String text = "";
		text = "Image: " + imp.getTitle() + PluginConstants.LINE_SEPARATOR;
		text += "Ignored border: " + border + PluginConstants.LINE_SEPARATOR;
		text += "Max. binning: " + maxBinning + PluginConstants.LINE_SEPARATOR;
		text += "Sensitivity: " + sensitivity + PluginConstants.LINE_SEPARATOR;
		text += PluginConstants.LINE_SEPARATOR;
		return text;
	}

	@Override
	public void run(final ImageProcessor ip) {
		FloatProcessor fp = (FloatProcessor) imp.getProcessor().clone();
		fp.setRoi(border, border, imp.getWidth() - 2 * border, imp.getHeight() - 2 *
			border);
		fp = (FloatProcessor) fp.crop();
		if (maxBinning == 0 | maxBinning > fp.getWidth() / 10 | maxBinning > fp
			.getHeight() / 10)
		{
			maxBinning = Math.min(fp.getWidth(), fp.getHeight()) / 10;
			IJ.showMessage("Max. binning", String.format(
				"The max. binning was set to %d", maxBinning));
		}
		// Reset timer to get the correct processing time.
		imp.startTiming();
		final String summary = createSummary();
		final DQE_ByBinning task = new DQE_ByBinning(maxBinning, sensitivity, fp,
			imp.getTitle(), summary);
		task.waitForResults();
		final MyRunnable runnable = new MyRunnable(task);
		new Thread(runnable).start();
	}

	@Override
	public int setup(final String arg, final ImagePlus imp) {
		if (imp == null) return DOES_ALL;
		this.imp = imp;
		if (showDialog() == false) return DONE;
		return DOES_32 + NO_CHANGES;
	}

	private boolean showDialog() {
		final GenericDialog gd = new GenericDialog("Measure DQE...");
		gd.addNumericField("Border to ignore", 0, 0);
		gd.addNumericField("Max. binning (0 = auto)", 0, 0);
		gd.addNumericField("Sensitivity S", 1, 0);
		gd.setResizable(false);
		gd.showDialog();
		if (gd.wasCanceled()) return false;
		border = (int) gd.getNextNumber();
		maxBinning = (int) gd.getNextNumber();
		sensitivity = (float) gd.getNextNumber();
		System.out.println(String.format(
			"Border: %d; Max. Binning: %d; Sensitivity: %f", border, maxBinning,
			sensitivity));
		return true;
	}

}
