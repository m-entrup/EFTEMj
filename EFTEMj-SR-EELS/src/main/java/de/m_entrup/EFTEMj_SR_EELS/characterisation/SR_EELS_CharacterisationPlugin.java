
package de.m_entrup.EFTEMj_SR_EELS.characterisation;

import com.opencsv.CSVWriter;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.configuration.ConfigurationException;

import de.m_entrup.EFTEMj_SR_EELS.importer.SR_EELS_ImportPlugin;
import de.m_entrup.EFTEMj_SR_EELS.shared.SR_EELS_ConfigurationManager;
import de.m_entrup.EFTEMj_lib.CameraSetup;
import de.m_entrup.EFTEMj_lib.EFTEMj_Configuration;
import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.gui.ProfilePlot;
import ij.io.DirectoryChooser;
import ij.measure.CurveFitter;
import ij.measure.Measurements;
import ij.plugin.Binner;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.plugin.filter.RankFilters;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class SR_EELS_CharacterisationPlugin implements PlugIn {

	private static String configKeyPrefix = "SR-EELS." + SR_EELS_ImportPlugin.class.getSimpleName() + ".";
	public static String plotsAsStackKey = configKeyPrefix + "plotsAsStack";
	private EFTEMj_Configuration config;
	private SR_EELS_CharacterisationResults results;
	private boolean doRotateLeft = false;
	private boolean doRotateRight = false;

	public SR_EELS_CharacterisationPlugin() {
		try {
			config = SR_EELS_ConfigurationManager.getConfiguration();
		} catch (final ConfigurationException e) {
			IJ.error("Failed to load config.", e.toString());
			return;
		}
	}

	@Override
	public void run(final String arg) {
		try {
			final SR_EELS_CharacterisationSettings settings = new SR_EELS_CharacterisationSettings();
			/*
			 * Use the same folder as SR-EELS_ImportCharacterisation.js. The
			 * user has to select a data set (sub-folder), but it is still
			 * possible to select any other folder.
			 */
			settings.path = new File(config.getString(SR_EELS_ImportPlugin.databasePathKey));
			if (settings.path == null) {
				IJ.showMessage("Can't find database", "EFTMj can't find a database.");
				return;
			}
			DirectoryChooser.setDefaultDirectory(settings.path.getAbsolutePath());
			final DirectoryChooser dc = new DirectoryChooser("Select folder for characterisation...");
			settings.path = new File(dc.getDirectory());
			if (settings.path == null)
				return;
			settings.images = getImages(settings);
			this.results = new SR_EELS_CharacterisationResults();
			results.settings = settings;
			results.timeStart = new Date().getTime();
			runCharacterisation();
			saveResults();
			results.timeStop = new Date().getTime();
			IJ.showStatus("Finished in " + Math.round((results.timeStop - results.timeStart) / 1000) + " seconds!");
		} catch (final InternalError e) {
			IJ.showMessage("Script aborted", e.getMessage());
		}
	}

	public SR_EELS_CharacterisationResults performCharacterisation(final SR_EELS_CharacterisationSettings settings) {
		if (settings.images == null) {
			IJ.log("Can't run SR-EELS chracterisation without images.");
			return null;
		}
		results = new SR_EELS_CharacterisationResults(settings);
		runCharacterisation();
		return results;
	}

	public ArrayList<String> getImages(final SR_EELS_CharacterisationSettings settings) {
		ArrayList<String> images = new ArrayList<>();
		final String[] fileList = settings.path.list();
		images = getFilteredImages(settings, fileList);
		return images;
	}

	/**
	 * Filter an array of file and folder names. Folders are excluded
	 * automatically. The user is presented a {@link GenericDialog} to select
	 * the files to process.
	 *
	 * @param settings
	 *            of the current characterization run that is used to get the
	 *            full file path.
	 * @param fileList
	 *            containing files and folders of the directory to process.
	 * @return a filtered list of files, selected by the user.
	 */
	private ArrayList<String> getFilteredImages(final SR_EELS_CharacterisationSettings settings,
			final String[] fileList) {
		final ArrayList<String> filesForSelection = new ArrayList<>();
		final ArrayList<String> filteredList = new ArrayList<>();
		Arrays.sort(fileList);
		final GenericDialog gd = new GenericDialog("Select files");
		int counter = 0;
		for (int i = 0; i < fileList.length; i++) {
			if (new File(settings.path, fileList[i]).isFile()) {
				filesForSelection.add(fileList[i]);
				counter++;
				if (fileList[i].endsWith(".tif") & !fileList[i].contains("-exclude")) {
					gd.addCheckbox(fileList[i], true);
				} else {
					gd.addCheckbox(fileList[i], false);
				}
			}
		}
		if (counter < 1) {
			throw new InternalError("There are no files to load in\n" + settings.path);
		}
		gd.showDialog();
		if (gd.wasCanceled()) {
			throw new InternalError("The script has been canceld by the user.");
		}
		for (int i = 0; i < counter; i++) {
			if (gd.getNextBoolean()) {
				filteredList.add(filesForSelection.get(i));
			}
		}
		return filteredList;
	}

	private class RoiSettings {

		private RoiSettings(final int pos, final int width) {
			this.pos = pos;
			this.width = width;
		}

		private int pos;
		private int width;
	}

	private void updateRoi(final ImagePlus imp, final RoiSettings roi) {
		final ProfilePlot profile = new ProfilePlot(imp);
		final double[] xValues = new double[profile.getProfile().length];
		for (int p = 0; p < profile.getProfile().length; p++) {
			xValues[p] = p;
		}
		final CurveFitter fit = new CurveFitter(xValues, profile.getProfile());
		fit.doFit(CurveFitter.GAUSSIAN);
		final double gaussCentre = fit.getParams()[2];
		final double gaussSigma = fit.getParams()[3];
		final double gaussSigmaWeighted = results.settings.sigmaWeight * gaussSigma / Math.pow(fit.getRSquared(), 2);
		roi.pos = (int) Math.max(roi.pos + Math.round(gaussCentre - gaussSigmaWeighted), 0);
		roi.width = (int) Math.round(2 * gaussSigmaWeighted);
	}

	private void runCharacterisation() {
		final SR_EELS_CharacterisationSettings settings = results.settings;
		final ArrayList<String> images = settings.images;
		for (int i = 0; i < images.size(); i++) {
			final String imageName = settings.images.get(i);
			results.subResults.put(imageName, new SR_EELS_CharacterisationResult());
			final SR_EELS_CharacterisationResult result = results.subResults.get(imageName);
			final SR_EELS_ImageObject image = new SR_EELS_ImageObject(imageName, settings);
			final ImagePlus imp = image.imp;
			result.imp.put(imageName, imp);
			result.width = imp.getWidth();
			result.height = imp.getHeight();
			result.binX = CameraSetup.getFullHeight() / result.width;
			/*
			 * This is necessary if you want to run the characterization on
			 * corrected images. The width can than be larger then the reference
			 * width. This results in a bin of 0.
			 */
			if (result.binX == 0) {
				result.binX = 1;
			}
			result.binY = CameraSetup.getFullWidth() / result.height;
			if (result.binY == 0) {
				result.binY = 1;
			}
			int yPos = (int) (settings.energyBorderLow / result.binY);
			if (settings.path.toString().contains("ZLP")) {
				yPos += image.height / 2;
			}
			final RoiSettings roiSetting = new RoiSettings(0, image.width);
			/*
			 * Creating initial values for the Roi:
			 */
			imp.setRoi(new Rectangle(roiSetting.pos, yPos, roiSetting.width, (int) (settings.stepSize / result.binY)));
			updateRoi(imp, roiSetting);
			while (yPos < image.height - (settings.energyBorderHigh / result.binY)) {
				imp.setRoi(
						new Rectangle(roiSetting.pos, yPos, roiSetting.width, (int) (settings.stepSize / result.binY)));
				SR_EELS_SubImageObject subImage = new SR_EELS_SubImageObject(new Duplicator().run(imp));
				updateRoi(subImage.imp, roiSetting);
				imp.setRoi(
						new Rectangle(roiSetting.pos, yPos, roiSetting.width, (int) (settings.stepSize / result.binY)));
				subImage = new SR_EELS_SubImageObject(new Duplicator().run(imp));
				subImage.xOffset = roiSetting.pos;
				subImage.yOffset = yPos;
				subImage.threshold = settings.threshold;
				result.add(runCharacterisationSub(subImage, settings.useThresholding));
				yPos += settings.stepSize;
			}
		}
		plotResults();
	}

	private static double calcLimit(final ImageProcessor ip) {
		final ImageStatistics statistic = ImageStatistics.getStatistics(ip, Measurements.MEDIAN + Measurements.MEAN,
				null);
		final double limit = (statistic.median + statistic.mean) / 2;
		return limit;
	}

	private SR_EELS_CharacterisationSubResults runCharacterisationSub(final SR_EELS_SubImageObject subImage,
			final boolean useThresholding) {
		final ImagePlus subImp = subImage.imp;
		final SR_EELS_CharacterisationSubResults subResult = new SR_EELS_CharacterisationSubResults();
		if (useThresholding == true) {
			IJ.setAutoThreshold(subImp, subImage.threshold + " dark");
			IJ.run(subImp, "NaN Background", "");
			final int measurements = Measurements.MEAN + Measurements.INTEGRATED_DENSITY + Measurements.CENTER_OF_MASS;
			final ImageStatistics statistic = ImageStatistics.getStatistics(subImp.getProcessor(), measurements, null);
			// double mean = statistic.mean;
			final double specWidth = statistic.area / subImp.getHeight();
			final double xm = statistic.xCenterOfMass;
			final double ym = statistic.yCenterOfMass;
			IJ.run(subImp, "Macro...", "code=[if(isNaN(v)) v=-10000;]");
			IJ.run(subImp, "Find Edges", "");
			subImp.setRoi(new Rectangle((int) Math.max(xm - specWidth, 0), 0, (int) (xm - Math.max(xm - specWidth, 0)),
					subImp.getHeight()));
			IJ.run(subImp, "Find Maxima...", "output=[Point Selection] exclude");
			Rectangle roi = subImp.getRoi().getBounds();
			final int xLeft = roi.x;
			subImp.setRoi(new Rectangle((int) xm, 0, subImp.getWidth() - (int) Math.max(xm - specWidth, 0),
					subImp.getHeight()));
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
		} else {
			final int stepSize = subImp.getHeight();
			IJ.run(subImp, "Bin...", "x=1 y=" + subImp.getHeight() + " bin=Average");
			final double limit = calcLimit(subImp.getProcessor()); // statistic.stdDev
			int left = 0;
			int right = subImp.getWidth();
			boolean searchLeft = true;
			for (int i = 0; i < subImp.getWidth(); i++) {
				if (searchLeft == true) {
					if (subImp.getProcessor().getf(i, 0) > limit) {
						left = i;
						searchLeft = false;
					}
				} else {
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

	private void plotResults() {
		final ArrayList<String> images = results.settings.images;
		ImageStack stack = null;
		for (int i = 0; i < images.size(); i++) {
			final SR_EELS_CharacterisationResult result = results.subResults.get(images.get(i));
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
			final Plot plot = new Plot("Spec of " + images.get(i) + " (" + results.settings.useThresholding + ")",
					"position x", "position y", xValues, yValues);
			plot.add("", xValues, leftValues);
			plot.add("", xValues, rightValues);
			if (!results.settings.useThresholding)
				plot.add("CROSS", xValues, limit);
			plot.setColor(java.awt.Color.RED);
			plot.add("", xValues, widthValues);
			plot.setLimits(0, result.height - 1, 0, result.width - 1);
			if (stack == null) {
				stack = new ImageStack(plot.getProcessor().getWidth(), plot.getProcessor().getHeight());
			}
			stack.addSlice(images.get(i), plot.getProcessor(), i);
			int polyOrder;
			switch (results.settings.polynomialOrder) {
			case 1:
				polyOrder = CurveFitter.STRAIGHT_LINE;
				break;
			case 2:
				polyOrder = CurveFitter.POLY2;
				break;
			case 3:
				polyOrder = CurveFitter.POLY3;
				break;
			case 4:
				polyOrder = CurveFitter.POLY4;
				break;
			case 5:
				polyOrder = CurveFitter.POLY5;
				break;
			case 6:
				polyOrder = CurveFitter.POLY6;
				break;
			case 7:
				polyOrder = CurveFitter.POLY7;
				break;
			case 8:
				polyOrder = CurveFitter.POLY8;
				break;

			default:
				polyOrder = CurveFitter.POLY3;
				results.settings.polynomialOrder = 3;
				break;
			}
			result.leftFit = new CurveFitter(xValues, leftValues);
			result.leftFit.doFit(polyOrder);
			result.centreFit = new CurveFitter(xValues, yValues);
			result.centreFit.doFit(polyOrder);
			result.rightFit = new CurveFitter(xValues, rightValues);
			result.rightFit.doFit(polyOrder);
		}
		final ImagePlus imp = new ImagePlus("Characterisation plots", stack);
		results.plots = imp;
	}

	public void saveResults(final File path, final int type) {
		if ((type & SR_EELS_CharacterisationResults.PLOTS) != 0) {
			path.mkdirs();
			if (config.getBoolean(plotsAsStackKey)) {
				IJ.save(results.plots, new File(path, "plots").getAbsolutePath());
			} else {
				final ImageStack stack = results.plots.getStack();
				for (int i = 1; i <= results.plots.getStackSize(); i++) {
					final ImagePlus imp = new ImagePlus(stack.getSliceLabel(i).split("\\.")[0] + "_graph",
							stack.getProcessor(i));
					IJ.saveAs(imp, "PNG", new File(path, imp.getShortTitle()).getAbsolutePath());
				}
			}
			IJ.run(results.plots, "8-bit Color", "number=16");
			IJ.saveAs(results.plots, "Gif", new File(path, "plots").getAbsolutePath());
		}
		if ((type & SR_EELS_CharacterisationResults.TIFF) != 0) {
			resultsToTiff(path);
		}
		if ((type & SR_EELS_CharacterisationResults.CSV) != 0) {
			resultsToCsv(path);
		}
		if ((type & SR_EELS_CharacterisationResults.JPEG) != 0) {
			resultsToJpeg(path);
		}
	}

	private void resultsToTiff(final File path) {
		final ArrayList<String> images = results.settings.images;
		final int width = 14;
		int height = 0;
		final ArrayList<FloatProcessor> fps = new ArrayList<>();
		for (int i = 0; i < images.size(); i++) {
			final SR_EELS_CharacterisationResult result = results.subResults.get(images.get(i));
			if (height == 0)
				height = result.size();
			final FloatProcessor fp = new FloatProcessor(width, height);
			for (int j = 0; j < result.size(); j++) {
				final float y = (float) result.get(j).y;
				fp.setf(0, j, result.binY * y);
				fp.setf(1, j, (float) (result.binY * result.get(j).yError));
				fp.setf(2, j, (float) (result.binX * result.get(j).x));
				fp.setf(3, j, (float) (result.binX * result.get(j).xError));
				fp.setf(4, j, (float) (result.binX * result.get(j).left));
				fp.setf(5, j, (float) (result.binX * result.get(j).leftError));
				fp.setf(6, j, (float) (result.binX * result.get(j).right));
				fp.setf(7, j, (float) (result.binX * result.get(j).rightError));
				fp.setf(8, j, (float) (result.binX * result.get(j).width));
				fp.setf(9, j, (float) (result.binX * result.get(j).widthError));
				fp.setf(10, j, (float) (result.get(j).limit));
				fp.setf(11, j, (float) (result.binX * result.centreFit.f(y)));
				fp.setf(12, j, (float) (result.binX * result.leftFit.f(y)));
				fp.setf(13, j, (float) (result.binX * result.rightFit.f(y)));
			}
			fps.add(fp);
		}
		final ImageStack stack = new ImageStack(width, height);
		for (int i = 0; i < fps.size(); i++) {
			stack.addSlice(images.get(i), fps.get(i), i);
		}
		final ImagePlus imp = new ImagePlus("Characterisation results", stack);
		path.mkdirs();
		IJ.save(imp, new File(path, "results").getAbsolutePath());
	}

	private void resultsToCsv(final File path) {
		final ArrayList<String> images = results.settings.images;
		path.mkdirs();
		/*
		 * For each image a csv file is created:
		 */
		for (int i = 0; i < images.size(); i++) {
			final String imgName = images.get(i).split("\\.")[0];
			try (CSVWriter writer = new CSVWriter(new FileWriter(new File(path, imgName + ".csv")), ';',
					Character.MIN_VALUE);) {
				final String[] header = { "y-position", "y-error", "x-position", "x-error", "left-position",
						"left-error", "right-position", "right-error", "width", "width-error", "threshold",
						"fitted-x-position", "fitted-left-position", "fitted-right-position" };
				writer.writeNext(header);
				final SR_EELS_CharacterisationResult result = results.subResults.get(images.get(i));
				for (int j = 0; j < result.size(); j++) {
					final double y = result.get(j).y;
					final ArrayList<String> csvLine = new ArrayList<>();
					csvLine.add(String.valueOf(result.binY * y));
					csvLine.add(String.valueOf(result.binY * result.get(j).yError));
					csvLine.add(String.valueOf(result.binX * result.get(j).x));
					csvLine.add(String.valueOf(result.binX * result.get(j).xError));
					csvLine.add(String.valueOf(result.binX * result.get(j).left));
					csvLine.add(String.valueOf(result.binX * result.get(j).leftError));
					csvLine.add(String.valueOf(result.binX * result.get(j).right));
					csvLine.add(String.valueOf(result.binX * result.get(j).rightError));
					csvLine.add(String.valueOf(result.binX * result.get(j).width));
					csvLine.add(String.valueOf(result.binX * result.get(j).widthError));
					csvLine.add(String.valueOf(result.get(j).limit));
					csvLine.add(String.valueOf(result.binX * result.centreFit.f(y)));
					csvLine.add(String.valueOf(result.binX * result.leftFit.f(y)));
					csvLine.add(String.valueOf(result.binX * result.rightFit.f(y)));
					final String[] line = new String[csvLine.size()];
					writer.writeNext(csvLine.toArray(line));
				}
			} catch (final IOException e) {
				IJ.showMessage("Error when writing csv file.", e.getMessage());
			}
		}
	}

	/**
	 * Draw a small cross on a {@link ColorProcessor}. The cross consists of 5
	 * pixels.
	 *
	 * @param cp
	 *            is the {@link ColorProcessor} to draw on.
	 * @param x
	 *            is the x position of the center.
	 * @param y
	 *            is the y position of the center.
	 */
	private void drawCrossOnColorProcessor(final ColorProcessor cp, final int x, final int y) {
		final int[] value = new int[3];
		// center
		cp.getPixel(x, y, value);
		value[1] = 255;
		cp.putPixel(x, y, value);
		// top left
		cp.getPixel(x - 1, y - 1, value);
		value[1] = 255;
		cp.putPixel(x - 1, y - 1, value);
		// top right
		cp.getPixel(x + 1, y - 1, value);
		value[1] = 255;
		cp.putPixel(x + 1, y - 1, value);
		// bottom left
		cp.getPixel(x - 1, y + 1, value);
		value[1] = 255;
		cp.putPixel(x - 1, y + 1, value);
		// bottom right
		cp.getPixel(x + 1, y + 1, value);
		value[1] = 255;
		cp.putPixel(x + 1, y + 1, value);
	}

	/**
	 * For each image that has been characterized, a JPEG version will be
	 * created. The JPEG uses log-scaling of the intensity. The data points and
	 * the resulting fit functions are shown.
	 *
	 * @param path
	 *            The folder that will contain the JPEG images.
	 */
	private void resultsToJpeg(final File path) {
		final ArrayList<String> images = results.settings.images;
		path.mkdirs();
		final Binner binner = new Binner();
		for (int i = 0; i < images.size(); i++) {
			final SR_EELS_CharacterisationResult result = results.subResults.get(images.get(i));
			final ImagePlus jpegImp = result.imp.get(images.get(i));
			jpegImp.killRoi();
			jpegImp.getProcessor().log();
			IJ.run(jpegImp, "Enhance Contrast", "saturated=0.35");
			jpegImp.getProcessor().flipHorizontal();
			jpegImp.setProcessor(jpegImp.getProcessor().rotateLeft());
			ImagePlus jpeg = jpegImp.flatten();
			// Make image darker for better visibility of the overlay.
			jpeg.getProcessor().multiply(1 / 1.3);
			int binJPEG = 1;
			while (Math.max(jpeg.getWidth(), jpeg.getHeight()) > 1024) {
				jpeg = binner.shrink(jpeg, 2, 2, 1, Binner.AVERAGE);
				binJPEG *= 2;
			}
			final ColorProcessor jP = (ColorProcessor) jpeg.getProcessor();
			final int[] value = new int[3];
			int y;
			/*
			 * Draw crosses for each data point that defines the center and the
			 * borders.
			 */
			for (int j = 0; j < result.size(); j++) {
				final int x = (int) Math.round(result.get(j).y / binJPEG);
				final int left = (int) Math.round(result.get(j).left / binJPEG);
				final int center = (int) Math.round(result.get(j).x / binJPEG);
				final int right = (int) Math.round(result.get(j).right / binJPEG);
				drawCrossOnColorProcessor(jP, x, left);
				drawCrossOnColorProcessor(jP, x, center);
				drawCrossOnColorProcessor(jP, x, right);
			}
			/*
			 * Draw red lines to mark center and border of each spectrum.
			 */
			for (int x = 0; x < jpeg.getWidth(); x++) {
				y = (int) Math.round(result.leftFit.f(x * binJPEG) / binJPEG);
				jP.getPixel(x, y, value);
				value[0] = 255;
				jP.putPixel(x, y, value);
				y = (int) Math.round(result.rightFit.f(x * binJPEG) / binJPEG);
				jP.getPixel(x, y, value);
				value[0] = 255;
				jP.putPixel(x, y, value);
				y = (int) Math.round(result.centreFit.f(x * binJPEG) / binJPEG);
				jP.getPixel(x, y, value);
				value[0] = 255;
				jP.putPixel(x, y, value);
			}
			if (results.settings.showJPEG) {
				jpeg.show();
			}
			IJ.saveAs(jpeg, "Jpeg", new File(path, images.get(i).split("\\.")[0]).getAbsolutePath());
		}

	}

	private void saveResults() {
		final File path = new File(results.settings.path, results.settings.toString());
		final int types = SR_EELS_CharacterisationResults.TIFF + SR_EELS_CharacterisationResults.JPEG
				+ SR_EELS_CharacterisationResults.CSV + SR_EELS_CharacterisationResults.PLOTS;
		saveResults(path, types);
	}

	public static void main(final String[] args) {
		System.out.println(SR_EELS_CharacterisationPlugin.plotsAsStackKey);
		EFTEMj_Debug.debug(SR_EELS_CharacterisationPlugin.class);
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

		public SR_EELS_ImageObject(final String imageName, final SR_EELS_CharacterisationSettings settings) {
			this.path = new File(settings.path, imageName).getAbsolutePath();
			this.imp = IJ.openImage(this.path);
			/*
			 * Imported images are rotated left to put the energy dispersive
			 * axis on x. The doRotate... flags refer to the imported images.
			 * For the characterization it's best to have the energy dispersive
			 * axis on y. That is why we rotate these images 90° right. Flip
			 * Horizontally is necessary to refer to the coordinates of the
			 * unrotated image.
			 */
			if (doRotateLeft) {
				imp.getProcessor().flipHorizontal();
			} else if (doRotateRight) {
				imp.getProcessor().flipVertical();
			} else {
				imp.setProcessor(imp.getProcessor().rotateRight());
				imp.getProcessor().flipHorizontal();
			}
			this.width = this.imp.getWidth();
			this.height = this.imp.getHeight();
			int bin = CameraSetup.getFullWidth() / width;
			/*
			 * This is necessary if you want to run the characterization on
			 * corrected images. The width can than be larger then the reference
			 * width. This results in a bin of 0.
			 */
			if (bin == 0) {
				bin = 1;
			}
			final double filterRadius = settings.filterRadius / bin;
			final float threshold = (float) calcLimit(this.imp.getProcessor());
			final RankFilters rmOutliers = new RankFilters();
			rmOutliers.rank(this.imp.getProcessor(), filterRadius, RankFilters.OUTLIERS, RankFilters.BRIGHT_OUTLIERS,
					threshold);
			rmOutliers.rank(this.imp.getProcessor(), filterRadius, RankFilters.OUTLIERS, RankFilters.DARK_OUTLIERS,
					threshold);
			rmOutliers.rank(this.imp.getProcessor(), filterRadius, RankFilters.MEDIAN);
		}
	}

	private class SR_EELS_SubImageObject {

		public String threshold;
		public int yOffset;
		public int xOffset;
		ImagePlus imp;

		public SR_EELS_SubImageObject(final ImagePlus imp) {
			this.imp = imp;
			this.imp.setRoi(new Rectangle(0, 0, this.imp.getWidth(), this.imp.getHeight()));
		}
	}

	public void setRotation(final String mode) {
		if (mode.toLowerCase().equals("left")) {
			doRotateLeft = true;
			doRotateRight = false;
		} else if (mode.toLowerCase().equals("right")) {
			doRotateLeft = false;
			doRotateRight = true;
		} else {
			doRotateLeft = false;
			doRotateRight = false;
		}
	}
}
