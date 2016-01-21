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

import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.FileSaver;
import ij.plugin.filter.RankFilters;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * This class contains static methods to manipulate {@link ImagePlus} objects or
 * to extract information from them.
 */
public class ImagePlusTool {

	/**
	 * The display limit is adjusted. The lowest and highest 1% are not displayed.
	 *
	 * @param imp
	 */
	private static void calDisplayLimit(final ImagePlus imp) {
		final int histCh = 256;
		try {
			final ImagePlus impNew = imp.duplicate();
			final FloatProcessor fp = (FloatProcessor) impNew.getProcessor();
			final RankFilters rf = new RankFilters();
			rf.rank(fp, 3, RankFilters.MEDIAN);
			final float[] pixels = (float[]) imp.getProcessor().getPixels();
			final int[] histogram = new int[histCh];
			// When an image is changed you have to call this method to get the
			// right min & max.
			fp.resetMinAndMax();
			final double max = fp.getMax();
			final float[] errorMap = PluginAPI.getInstance().getDatasetAPI()
				.getErrorMap();
			int pixelCount = 0;
			for (int i = 0; i < pixels.length; i++) {
				if (errorMap[i] == 0) {
					int value = (int) Math.round(pixels[i] / max * (histCh - 1));
					if (value < 0) {
						value = 0;
					}
					if (value >= histCh) {
						value = histCh - 1;
					}
					histogram[value]++;
					pixelCount++;
				}
			}
			int highLimit = histogram.length - 1;
			int sum = histogram[highLimit];
			while (sum < (int) Math.round(pixelCount * 0.01)) {
				highLimit--;
				sum += histogram[highLimit];
			}
			int lowLimit = 0;
			sum = histogram[lowLimit];
			while (sum < (int) Math.round(pixelCount * 0.01)) {
				lowLimit++;
				sum += histogram[lowLimit];
			}
			imp.setDisplayRange(max * lowLimit / (histCh - 1), max * highLimit /
				(histCh - 1));
		}
		catch (final Exception e) {
			System.out.println(e);
			LogWriter.showWarningAndWriteLog(PluginMessages.getString(
				"Error.DisplyLimits") + "<p>" + e.getMessage() + "</p></html>");
			return;
		}
	}

	/**
	 * Creates an {@link ImagePlus} from an {@link ImageProcessor} and shows it.
	 *
	 * @param title The title of the new {@link ImagePlus}
	 * @param ip The {@link ImageProcessor} that the {@link ImagePlus} is created
	 *          from
	 */
	public static ImagePlus createImagePlus(final String title,
		final ImageProcessor ip, final boolean cal)
	{
		final ImagePlus imp = new ImagePlus(title, ip);
		if (cal & ip.getClass() == FloatProcessor.class) {
			calDisplayLimit(imp);
		}
		imp.changes = false;
		return imp;
	}

	/**
	 * Creates an {@link ImagePlus} from an {@link ImageStack} and shows it.
	 *
	 * @param title The title of the new {@link ImagePlus}
	 * @param stack The {@link ImageStack} that the {@link ImagePlus} is created
	 *          from
	 */
	public static ImagePlus createImagePlus(final String title,
		final ImageStack stack, final boolean cal)
	{
		final ImagePlus imp = new ImagePlus(title, stack);
		if (cal) {
			calDisplayLimit(imp);
		}
		imp.changes = false;
		return imp;
	}

	/**
	 * This method retrieves the titles (getTitle()) of all images opened in
	 * ImageJ.
	 *
	 * @return The titles of all opened images as a string array
	 */
	public static String[] getImagePlusTitels() {
		int[] imageIDs;
		String[] stackTitels;
		imageIDs = WindowManager.getIDList();
		stackTitels = new String[WindowManager.getImageCount()];
		for (int i = 0; i < WindowManager.getImageCount(); i++) {
			final ImagePlus imp = WindowManager.getImage(imageIDs[i]);
			stackTitels[i] = imp.getTitle();
		}
		return stackTitels;
	}

	public static void saveImagePlus(final ImagePlus imp) {
		final FileSaver fs = new FileSaver(imp);
		if (fs.saveAsTiff() == true) {
			final String directory = imp.getOriginalFileInfo().directory;
			final String fileName = imp.getOriginalFileInfo().fileName;
			final String path = directory + fileName;
			LogWriter.writeLog("File saved: " + path);
		}
	}

	public static void saveImagePlus(final ImagePlus imp, final boolean ask) {
		if (ask == true) {
			final GenericDialog gd = new GenericDialog(PluginMessages.getString(
				"Title.ConfirmSaving"));
			String title = imp.getShortTitle();
			if (!title.contains(".")) {
				title += ".tif";
			}
			gd.addMessage(title);
			gd.addMessage(PluginMessages.getString("Label.ConfirmSaving"));
			gd.showDialog();
			if (gd.wasCanceled()) {
				return;
			}
		}
		saveImagePlus(imp);
	}

}
