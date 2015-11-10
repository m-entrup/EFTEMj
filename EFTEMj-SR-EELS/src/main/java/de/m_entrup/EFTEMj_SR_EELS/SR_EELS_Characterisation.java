
package de.m_entrup.EFTEMj_SR_EELS;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.gui.ProfilePlot;
import ij.io.DirectoryChooser;
import ij.measure.CurveFitter;
import ij.measure.Measurements;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageStatistics;

public class SR_EELS_Characterisation implements PlugIn {

	@Override
	public void run(final String arg) {
		try {
			final SR_EELS_CharacterisationSettings settings =
				new SR_EELS_CharacterisationSettings();

			/*
			 * Use the same folder as SR-EELS_ImportCharacterisation.js.
			 * The user has to select a data set (sub-folder), but it is still possible to select any other folder.
			 */
			settings.path = Prefs.get(SR_EELS_PrefsKeys.databasePath.getValue(),
				null);
			if (settings.path == null) {
				IJ.showMessage("Can't find database", "EFTMj can't find a database.");
				return;
			}
			DirectoryChooser.setDefaultDirectory(settings.path);
			final DirectoryChooser dc = new DirectoryChooser(
				"Select folder for characterisation...");
			settings.path = dc.getDirectory();
			if (settings.path == null) return;
			settings.images = getImages(settings);
			final SR_EELS_CharacterisationResults results =
				new SR_EELS_CharacterisationResults();
			results.settings = settings;
			results.timeStart = new Date().getTime();
			runCharacterisation(results);
			plotResults(results);
			saveResults(results);
			results.timeStop = new Date().getTime();
			IJ.showStatus("Finished in " + Math.round((results.timeStop -
				results.timeStart) / 1000) + " seconds!");
		}
		catch (final InternalError e) {
			IJ.showMessage("Script aborted", e.getMessage());
		}
	}

	private ArrayList<String> getImages(
		final SR_EELS_CharacterisationSettings settings)
	{
		ArrayList<String> images = new ArrayList<String>();
		final File folder = new File(settings.path);
		final String[] fileList = folder.list();
		images = getFilteredImages(settings, fileList);
		return images;
	}

	private ArrayList<String> getFilteredImages(
		final SR_EELS_CharacterisationSettings settings, final String[] fileList)
	{
		final ArrayList<String> filteredList = new ArrayList<String>();
		Arrays.sort(fileList);
		final GenericDialog gd = new GenericDialog("Select files");
		int counter = 0;
		for (int i = 0; i < fileList.length; i++) {
			if (new File(settings.path + fileList[i]).isFile()) {
				counter++;
				if (fileList[i].endsWith(".tif") & !fileList[i].contains("-exclude")) {
					gd.addCheckbox(fileList[i], true);
				}
				else {
					gd.addCheckbox(fileList[i], false);
				}
			}
		}
		if (counter < 1) {
			throw new InternalError("There are no files to load in\n" +
				settings.path);
		}
		gd.showDialog();
		if (gd.wasCanceled()) {
			throw new InternalError("The script has been canceld by the user.");
		}
		for (int i = 0; i < counter; i++) {
			if (gd.getNextBoolean()) {
				filteredList.add(fileList[i]);
			}
		}
		return filteredList;
	}

	private void runCharacterisation(
		final SR_EELS_CharacterisationResults results)
	{
		final SR_EELS_CharacterisationSettings settings = results.settings;
		final ArrayList<String> images = settings.images;
		for (int i = 0; i < images.size(); i++) {
			final String imageName = settings.images.get(i);
			results.subResults.put(imageName, new SR_EELS_CharacterisationResult());
			final SR_EELS_CharacterisationResult result = results.subResults.get(
				imageName);
			final SR_EELS_ImageObject image = new SR_EELS_ImageObject(imageName,
				settings);
			final ImagePlus imp = image.imp;
			result.imp.put(imageName, imp);
			result.width = imp.getWidth();
			result.height = imp.getHeight();
			int yPos = settings.energyBorderLow;
			int xOffset = 0;
			int roiWidth = image.width;
//			mean = new Array();
			while (yPos < image.height - settings.energyBorderHigh) {
				imp.setRoi(new Rectangle(xOffset, yPos, roiWidth, settings.stepSize));
				SR_EELS_SubImageObject subImage = new SR_EELS_SubImageObject(
					new Duplicator().run(imp));
				final ProfilePlot profile = new ProfilePlot(subImage.imp);
				final double[] xValues = new double[profile.getProfile().length];
				for (int p = 0; p < profile.getProfile().length; p++) {
					xValues[p] = p;
				}
				final CurveFitter fit = new CurveFitter(xValues, profile.getProfile());
				fit.doFit(CurveFitter.GAUSSIAN);
				final double gaussCentre = fit.getParams()[2];
				final double gaussSigma = fit.getParams()[3];
				final double gaussSigmaWeighted = settings.sigmaWeight * gaussSigma /
					Math.pow(fit.getRSquared(), 2);
				xOffset = (int) Math.max(xOffset + Math.round(gaussCentre -
					gaussSigmaWeighted), 0);
				roiWidth = (int) Math.round(2 * gaussSigmaWeighted);
				imp.setRoi(new Rectangle(xOffset, yPos, roiWidth, settings.stepSize));
				subImage = new SR_EELS_SubImageObject(new Duplicator().run(imp));
				subImage.xOffset = xOffset;
				subImage.yOffset = yPos;
				subImage.threshold = settings.threshold;
				result.add(runCharacterisationSub(subImage, settings.useThresholding));
				yPos += settings.stepSize;
			}
		}
	}

	private SR_EELS_CharacterisationSubResults runCharacterisationSub(
		final SR_EELS_SubImageObject subImage, final boolean useThresholding)
	{
		final ImagePlus subImp = subImage.imp;
		final SR_EELS_CharacterisationSubResults subResult =
			new SR_EELS_CharacterisationSubResults();
		if (useThresholding == true) {
			IJ.setAutoThreshold(subImp, subImage.threshold + " dark");
			IJ.run(subImp, "NaN Background", "");
			final int measurements = Measurements.MEAN +
				Measurements.INTEGRATED_DENSITY + Measurements.CENTER_OF_MASS;
			final ImageStatistics statistic = ImageStatistics.getStatistics(subImp
				.getProcessor(), measurements, null);
//			double mean = statistic.mean;
			final double specWidth = statistic.area / subImp.getHeight();
			final double xm = statistic.xCenterOfMass;
			final double ym = statistic.yCenterOfMass;
			IJ.run(subImp, "Macro...", "code=[if(isNaN(v)) v=-10000;]");
			IJ.run(subImp, "Find Edges", "");
			subImp.setRoi(new Rectangle((int) Math.max(xm - specWidth, 0), 0,
				(int) (xm - Math.max(xm - specWidth, 0)), subImp.getHeight()));
			IJ.run(subImp, "Find Maxima...", "output=[Point Selection] exclude");
			Rectangle roi = subImp.getRoi().getBounds();
			final int xLeft = roi.x;
			subImp.setRoi(new Rectangle((int) xm, 0, subImp.getWidth() - (int) Math
				.max(xm - specWidth, 0), subImp.getHeight()));
			IJ.run(subImp, "Find Maxima...", "output=[Point Selection] exclude");
			roi = subImp.getRoi().getBounds();
			final int xRight = roi.x + roi.width;
			subResult.x = (float) (xm + subImage.xOffset);
			subResult.xError = 0;
			subResult.y = (float) (ym + subImage.yOffset);
			subResult.yError = 0;
			subResult.left = xLeft + subImage.xOffset;
			subResult.leftError = 0;
			subResult.right = xRight + subImage.xOffset;
			subResult.rightError = 0;
			subResult.width = xRight - xLeft;
			subResult.widthError = 0;
		}
		else {
			final int stepSize = subImp.getHeight();
			IJ.run(subImp, "Bin...", "x=1 y=" + subImp.getHeight() + " bin=Average");
			final ImageStatistics statistic = ImageStatistics.getStatistics(subImp
				.getProcessor(), Measurements.MEAN + Measurements.STD_DEV, null);
			final double limit = statistic.mean; // statistic.stdDev
			int left = 0;
			int right = subImp.getWidth();
			boolean searchLeft = true;
			for (int i = 0; i < subImp.getWidth(); i++) {
				if (searchLeft == true) {
					if (subImp.getProcessor().getf(i, 0) > limit) {
						left = i;
						searchLeft = false;
					}
				}
				else {
					if (subImp.getProcessor().getf(i, 0) < limit) {
						right = i;
						break;
					}
				}
			}
			subResult.x = (left + right) / 2 + subImage.xOffset;
			subResult.xError = 1;
			subResult.y = stepSize / 2 + subImage.yOffset;
			subResult.yError = 1;
			subResult.left = left + subImage.xOffset;
			subResult.leftError = 1;
			subResult.right = right + subImage.xOffset;
			subResult.rightError = 1;
			subResult.width = right - left;
			subResult.widthError = 2;
			subResult.limit = (float) limit;
		}
		return subResult;
	}

	private void plotResults(final SR_EELS_CharacterisationResults results) {
		final ArrayList<String> images = results.settings.images;
		ImageStack stack = null;
		for (int i = 0; i < images.size(); i++) {
			final SR_EELS_CharacterisationResult result = results.subResults.get(
				images.get(i));
			final double[] xValues = new double[result.size()];
			final double[] yValues = new double[result.size()];
			final double[] leftValues = new double[result.size()];
			final double[] rightValues = new double[result.size()];
			final double[] widthValues = new double[result.size()];
			final double[] limit = new double[result.size()];
			for (int j = 0; j < result.size(); j++) {
				xValues[j] = result.get(j).y;
				yValues[j] = result.get(j).x;
				leftValues[j] = result.get(j).left;
				rightValues[j] = result.get(j).right;
				widthValues[j] = result.get(j).width;
				limit[j] = result.get(j).limit;
			}
			final Plot plot = new Plot("Spec of " + images.get(i) + " (" +
				results.settings.useThresholding + ")", "position x", "position y",
				xValues, yValues);
			plot.add("", xValues, leftValues);
			plot.add("", xValues, rightValues);
			if (!results.settings.useThresholding) plot.add("CROSS", xValues, limit);
			plot.setColor(java.awt.Color.RED);
			plot.add("", xValues, widthValues);
			plot.setLimits(0, result.height - 1, 0, result.width - 1);
			if (stack == null) {
				stack = new ImageStack(plot.getProcessor().getWidth(), plot
					.getProcessor().getHeight());
			}
			stack.addSlice(results.settings.images.get(i), plot.getProcessor(), i);
			result.leftFit = new CurveFitter(xValues, leftValues);
			result.leftFit.doFit(CurveFitter.POLY3);
			result.centreFit = new CurveFitter(xValues, yValues);
			result.centreFit.doFit(CurveFitter.POLY3);
			result.rightFit = new CurveFitter(xValues, rightValues);
			result.rightFit.doFit(CurveFitter.POLY3);
		}
		final ImagePlus imp = new ImagePlus("Characterisation plots", stack);
		results.plots = imp;
	}

	private void saveResults(final SR_EELS_CharacterisationResults results) {
		final ArrayList<String> images = results.settings.images;
		final String path = results.settings.path + results.settings.toString();
		new File(path).mkdirs();
		final int width = 14;
		int height = 0;
		final ArrayList<FloatProcessor> fps = new ArrayList<FloatProcessor>();
		for (int i = 0; i < images.size(); i++) {
			final SR_EELS_CharacterisationResult result = results.subResults.get(
				images.get(i));
			final ImagePlus jpegImp = result.imp.get(images.get(i));
			IJ.run(jpegImp, "Select None", "");
			IJ.run(jpegImp, "Log", "");
			IJ.run(jpegImp, "Enhance Contrast", "saturated=0.35");
			IJ.run(jpegImp, "Flip Horizontally", "");
			IJ.run(jpegImp, "Rotate 90 Degrees Left", "");
			final ImagePlus jpeg = jpegImp.flatten();
			IJ.run(jpeg, "Divide...", "value=1.3");
			int bin = 1;
			while (Math.max(jpeg.getWidth(), jpeg.getHeight()) > 1024) {
				IJ.run(jpeg, "Bin...", "x=2 y=2 bin=Average");
				bin *= 2;
			}
			if (height == 0) height = result.size();
			final FloatProcessor fp = new FloatProcessor(width, height);
			for (int j = 0; j < result.size(); j++) {
				final float y = (float) result.get(j).y;
				fp.setf(0, j, y);
				fp.setf(1, j, (float) result.get(j).yError);
				fp.setf(2, j, (float) result.get(j).x);
				fp.setf(3, j, (float) result.get(j).xError);
				fp.setf(4, j, (float) result.get(j).left);
				fp.setf(5, j, (float) result.get(j).leftError);
				fp.setf(6, j, (float) result.get(j).right);
				fp.setf(7, j, (float) result.get(j).rightError);
				fp.setf(8, j, (float) result.get(j).width);
				fp.setf(9, j, (float) result.get(j).widthError);
				fp.setf(10, j, (float) result.get(j).limit);
				fp.setf(11, j, (float) result.leftFit.f(y));
				fp.setf(12, j, (float) result.centreFit.f(y));
				fp.setf(13, j, (float) result.rightFit.f(y));
			}
			fps.add(fp);
			final ColorProcessor jP = (ColorProcessor) jpeg.getProcessor();
			final int[] value = new int[3];
			int y;
			for (int x = 0; x < jpeg.getWidth(); x++) {
				y = (int) Math.round(result.leftFit.f(x * bin) / bin);
				jP.getPixel(x, y, value);
				value[0] = 255;
				jP.putPixel(x, y, value);
				y = (int) Math.round(result.rightFit.f(x * bin) / bin);
				jP.getPixel(x, y, value);
				value[0] = 255;
				jP.putPixel(x, y, value);
				y = (int) Math.round(result.centreFit.f(x * bin) / bin);
				jP.getPixel(x, y, value);
				value[0] = 255;
				jP.putPixel(x, y, value);
			}
			IJ.save(jpeg, path + images.get(i).split("\\.")[0] + ".jpg");
		}
		final ImageStack stack = new ImageStack(width, height);
		for (int i = 0; i < fps.size(); i++) {
			stack.addSlice(images.get(i), fps.get(i), i);
		}
		final ImagePlus imp = new ImagePlus("Characterisation results", stack);
		IJ.save(imp, path + "results.tif");
		IJ.run(results.plots, "8-bit Color", "number=16");
		if (Prefs.get(SR_EELS_PrefsKeys.plotsAsTif.getValue(), false)) {
			IJ.save(results.plots, path + "plots.tif");
		}
		IJ.saveAs(results.plots, "Gif", path + "plots.gif");
	}

	public static void main(final String[] args) {
		EFTEMj_Debug.debug(SR_EELS_Characterisation.class);
	}

	protected class SR_EELS_CharacterisationSettings {

		int stepSize = 64;
		double filterRadius = Math.sqrt(stepSize);
		int energyBorderLow = 2 * stepSize;
		int energyBorderHigh = 2 * stepSize;
		float energyPosition = 0.5f;
		float sigmaWeight = 3f;
		int polynomialOrder = 3;
		boolean useThresholding = true;
		String threshold = "Li";
		String path = "";
		ArrayList<String> images;

		@Override
		public String toString() {
			final StringBuffer str = new StringBuffer();
			if (useThresholding) {
				str.append(threshold);
			}
			else {
				str.append("Limit");
			}
			str.append(",");
			str.append(stepSize);
			str.append(",");
			str.append(energyBorderLow);
			str.append(",");
			str.append(energyBorderHigh);
			str.append(",");
			str.append("poly");
			str.append(polynomialOrder);
			str.append("/");
			return str.toString();
		}
	}

	private class SR_EELS_CharacterisationResults {

		public ImagePlus plots;
		SR_EELS_CharacterisationSettings settings;
		long timeStart;
		long timeStop;
		HashMap<String, SR_EELS_CharacterisationResult> subResults;

		public SR_EELS_CharacterisationResults() {
			this.subResults = new HashMap<String, SR_EELS_CharacterisationResult>();
		}
	}

	@SuppressWarnings("serial")
	private class SR_EELS_CharacterisationResult extends
		ArrayList<SR_EELS_CharacterisationSubResults>
	{

		public HashMap<String, ImagePlus> imp;
		public CurveFitter leftFit;
		public CurveFitter centreFit;
		public CurveFitter rightFit;
		public int width;
		public int height;

		public SR_EELS_CharacterisationResult() {
			super();
			imp = new HashMap<String, ImagePlus>();
		}

	}

	private class SR_EELS_CharacterisationSubResults {

		/**
		 * This is the coordinate along the lateral axis.
		 */
		double x;
		/**
		 * This is the error of the coordinate along the lateral axis.
		 */
		double xError;
		/**
		 * This is the coordinate along the energy dispersive axis.
		 */
		double y;
		/**
		 * This is the error of the coordinate along the energy dispersive axis.
		 */
		double yError;
		/**
		 * This is the the left one of the detected borders (The top one of the
		 * saved data sets).
		 */
		double left;
		double leftError;
		/**
		 * This is the the right one of the detected borders (The lower one of the
		 * saved data sets).
		 */
		double right;
		double rightError;
		/**
		 * this is the width of the spectrum at the given energy loss (x
		 * coordinate).
		 */
		double width;
		double widthError;
		/**
		 * Only when not performing thresholding this value is used. The value of a
		 * pixel is considered as signal and not noise, when the limit to exceeded.
		 */
		double limit;
	}

	@SuppressWarnings("serial")
	private class InternalError extends Error {

		public InternalError(final String message) {
			super(message);
		}
	}

	private class SR_EELS_ImageObject {

		String path;
		ImagePlus imp;
		int width;
		int height;

		public SR_EELS_ImageObject(final String imageName,
			final SR_EELS_CharacterisationSettings settings)
		{
			final double filterRadius = settings.filterRadius;
			this.path = settings.path + imageName;
			this.imp = IJ.openImage(this.path);
			IJ.run(this.imp, "Rotate 90 Degrees Right", "");
			IJ.run(this.imp, "Flip Horizontally", "");
			this.width = this.imp.getWidth();
			this.height = this.imp.getHeight();
			final double threshold = 2 * this.imp.getStatistics().stdDev;
			IJ.run(this.imp, "Remove Outliers...", "radius=" + filterRadius +
				" threshold=" + threshold + " which=Bright");
			IJ.run(this.imp, "Remove Outliers...", "radius=" + filterRadius +
				" threshold=" + threshold + " which=Dark");
			IJ.run(this.imp, "Median...", "radius=" + filterRadius);
		}
	}

	private class SR_EELS_SubImageObject {

		public String threshold;
		public int yOffset;
		public int xOffset;
		ImagePlus imp;

		public SR_EELS_SubImageObject(final ImagePlus imp) {
			this.imp = imp;
			this.imp.setRoi(new Rectangle(0, 0, this.imp.getWidth(), this.imp
				.getHeight()));
		}
	}
}
