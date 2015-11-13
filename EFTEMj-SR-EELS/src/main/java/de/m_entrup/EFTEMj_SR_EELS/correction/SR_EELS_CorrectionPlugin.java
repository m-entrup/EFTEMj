/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2015, Michael Entrup b. Epping <mail@m-entrup.de>
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

package de.m_entrup.EFTEMj_SR_EELS.correction;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.m_entrup.EFTEMj_SR_EELS.characterisation.SR_EELS_CharacterisationPlugin;
import de.m_entrup.EFTEMj_SR_EELS.shared.SR_EELS;
import de.m_entrup.EFTEMj_lib.CameraSetup;
import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import de.m_entrup.EFTEMj_lib.tools.StringManipulator;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import lma.implementations.LMA;

/**
 * <p>
 * This plugin is used to correct SR-EELS images.
 * </p>
 * <p>
 * As it implements {@link ExtendedPlugInFilter} you have to apply it to an
 * image. Additionally there has to be a SR-EELS characterisation data set. Each
 * data set contains the files <code>Borders.txt</code> and
 * <code>Width.txt</code> that are necessary to run the correction. The plugin
 * assumes that the data set is can be found in a sub folder of where the
 * SR-EELS image is stored. If there is more than one characterisation data set,
 * the plugin presents a dialog to choose the preferred data set.
 * </p>
 *
 * @author Michael Entrup b. Epping
 */
public class SR_EELS_CorrectionPlugin implements ExtendedPlugInFilter {

	private static final String FILENAME_RESULTS = SR_EELS.FILENAME_RESULTS;
	/**
	 * The plugin will be aborted.
	 */
	private final int CANCEL = 0;
	/**
	 * The plugin will continue with the next step.
	 */
	private final int OK = 1;
	/**
	 * <code>DOES_32 | NO_CHANGES | FINAL_PROCESSING</code>
	 */
	private final int FLAGS = DOES_32 | NO_CHANGES | FINAL_PROCESSING;
	/**
	 * A {@link String} for the file field of the {@link GenericDialogPlus}.
	 */
	private final String NO_FILE_SELECTED = "No file selected.";
	/**
	 * The path where the characterisation results data set can be found.
	 */
	private String pathResults = NO_FILE_SELECTED;
	/**
	 * <p>
	 * An {@link SR_EELS_FloatProcessor} that contains the image that will be
	 * corrected.
	 * </p>
	 * <p>
	 * The input image will not be changed!
	 * </p>
	 */
	private SR_EELS_FloatProcessor inputProcessor;
	private SR_EELS_FloatProcessor outputProcessor;
	private String title;
	/**
	 * <p>
	 * An {@link ImagePlus} that contains the image that is the result of the
	 * correction.
	 * </p>
	 */
	private ImagePlus outputImage;
	/**
	 * This field indicates the progress. A static method is used to increase the
	 * value by 1. It is necessary to use volatile because different
	 * {@link Thread}s call the related method.
	 */
	private static volatile int progress;
	/**
	 * Number of steps until the correction is finished.
	 */
	private static int progressSteps;

	private DataImporter importer;

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(final String arg, final ImagePlus imp) {
		/*
		 * This will be called when the run method has finished.
		 */
		if (arg == "final") {
			outputImage.show();
			return NO_CHANGES | DONE;
		}
		return FLAGS;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(final ImageProcessor ip) {
		/*
		 * Each correction contains of implementations of the abstract classes
		 * CoordinateCorrector and a IntensityCorrector that can be can be combined
		 * as you want.
		 *
		 * By using getFunctionWidth() and getFunctionBorders() the characterisation
		 * results are loaded and an implementation of the Levenberg–Marquardt
		 * algorithm (LMA) is used to fit functions to the discrete values.
		 */
		IJ.showStatus("Preparing correction...");
		importer = new DataImporter(pathResults, false);
		final SR_EELS_Polynomial_2D widthFunction = getFunctionWidth();
		inputProcessor.setWidthFunction(widthFunction);
		final SR_EELS_Polynomial_2D borderFunction = getFunctionBorders();
		inputProcessor.setBorderFunction(borderFunction);
		/*
		 * TODO: Add the used correction methods to the image title.
		 */
		outputProcessor = widthFunction.createOutputImage();
		outputImage = new ImagePlus(title + "_corrected", outputProcessor);
		final CoordinateCorrector coordinateCorrection =
			new FullCoordinateCorrection(inputProcessor, outputProcessor);
		final IntensityCorrector intensityCorrection =
			new SimpleIntensityCorrection(inputProcessor, coordinateCorrection);
		/*
		 * Each line of the image is a step that is visualise by the progress bar of
		 * ImageJ.
		 */
		setupProgress(outputProcessor.getHeight());
		if (EFTEMj_Debug.getDebugLevel() == EFTEMj_Debug.DEBUG_FULL) {
			for (int x2 = 0; x2 < outputProcessor.getHeight(); x2++) {
				for (int x1 = 0; x1 < outputProcessor.getWidth(); x1++) {
					final float intensity = intensityCorrection.getIntensity(x1, x2);
					outputProcessor.setf(x1, x2, intensity);
				}
				updateProgress();
			}
		}
		else {
			/*
			 * The ExecutorService is used to handle the multithreading. see
			 * http://www.vogella.com/tutorials/JavaConcurrency/article.html#threadpools
			 */
			final ExecutorService executorService = Executors.newFixedThreadPool(
				Runtime.getRuntime().availableProcessors());
			for (int x2 = 0; x2 < outputProcessor.getHeight(); x2++) {
				final int x2Temp = x2;
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						for (int x1 = 0; x1 < outputProcessor.getWidth(); x1++) {
							final float intensity = intensityCorrection.getIntensity(x1,
								x2Temp);
							outputProcessor.setf(x1, x2Temp, intensity);
						}
						updateProgress();
					}
				});
			}
			executorService.shutdown();
			try {
				executorService.awaitTermination(30, TimeUnit.MINUTES);
			}
			catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Use this method for batch processing. Values that are set up by the GUI
	 * have to be passed as parameters.
	 *
	 * @param input_image is the image to correct.
	 * @param path2REsults is the text file that contains the characterisation
	 *          results for the width.
	 * @return the corrected image.
	 */
	public ImagePlus correctImage(final ImagePlus input_image,
		final String path2REsults)
	{
		this.inputProcessor = new SR_EELS_FloatProcessor(
			(FloatProcessor) input_image.getProcessor(), CameraSetup.getFullWidth() /
				input_image.getWidth(), CameraSetup.getFullHeight() / input_image
					.getHeight(), input_image.getWidth() / 2, input_image.getHeight() /
						2);
		title = StringManipulator.removeExtensionFromTitle(input_image.getTitle());
		this.pathResults = path2REsults;
		run(null);
		return outputImage;
	}

	/**
	 * <p>
	 * The results of the {@link SR_EELS_CharacterisationPlugin} plugin are
	 * parsed.
	 * </p>
	 * <p>
	 * This function extracts the values that describe the pathway of the borders
	 * of a spectrum.
	 * </p>
	 *
	 * @return a polynomial that fits the given data points
	 */
	private SR_EELS_Polynomial_2D getFunctionBorders() {
		final double[][] vals = new double[3 * importer.size()][3];
		int i = 0;
		for (final float[] point : importer) {
			for (int j = 0; j < 3; j++) {
				// y coordinate of the fit function at the centre of the image/camera ->
				// z value
				vals[i + j][0] = point[2 + 2 * j] - CameraSetup.getFullWidth() / 2;
				// Coordinate on the energy dispersive axis -> x value
				// The same value for top, centre and bottom.
				vals[i + j][1] = point[0] - CameraSetup.getFullHeight() / 2;
				// coordinate on the lateral axis -> y value
				// The indices for centre, top and bottom are 2, 4 and 6.
				vals[i + j][2] = importer.getYInterceptPoint(i / 3)[11 + j] -
					CameraSetup.getFullHeight() / 2;
			}
			i += 3;
		}
		/*
		 * Define the orders of the 2D polynomial.
		 */
		final int m = 3;
		final int n = 2;
		final SR_EELS_Polynomial_2D func = new SR_EELS_Polynomial_2D(m, n);
		final double[] a_fit = new double[(m + 1) * (n + 1)];
		Arrays.fill(a_fit, 1.);
		final LMA lma = new LMA(func, a_fit, vals);
		lma.fit();
		/*
		 * TODO: Output information about the fit using IJ.log
		 */
		return new SR_EELS_Polynomial_2D(m, n, a_fit);
	}

	/**
	 * <p>
	 * The results of the {@link SR_EELS_CharacterisationPlugin} plugin are
	 * parsed.
	 * </p>
	 * <p>
	 * This function extracts the values that describe the width of a spectrum
	 * depending on its position on the camera.
	 * </p>
	 *
	 * @return a polynomial that fits the given data points
	 */
	private SR_EELS_Polynomial_2D getFunctionWidth() {
		final double[][] vals = new double[importer.size()][3];
		int i = 0;
		for (final float[] point : importer) {
			// The width of the spectrum -> z value
			vals[i][0] = point[8];
			// Coordinate on the energy dispersive axis -> x value
			// The same value for top, centre and bottom.
			vals[i][1] = point[0] - CameraSetup.getFullWidth() / 2;
			// coordinate on the lateral axis -> y value
			// The indices for centre, top and bottom are 2, 4 and 6.
			vals[i][2] = point[2] - CameraSetup.getFullHeight() / 2;
			i++;
		}
		/*
		 * Define the orders of the 2D polynomial.
		 */
		final int m = 2;
		final int n = 2;
		final SR_EELS_Polynomial_2D func = new SR_EELS_Polynomial_2D(m, n);
		final double[] b_fit = new double[(m + 1) * (n + 1)];
		Arrays.fill(b_fit, 1.);
		final LMA lma = new LMA(func, b_fit, vals);
		lma.fit();
		/*
		 * TODO: Output information about the fit using IJ.log
		 */
		return new SR_EELS_Polynomial_2D(m, n, b_fit);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.filter.ExtendedPlugInFilter#showDialog(ij.ImagePlus, java.lang.String,
	 * ij.plugin.filter.PlugInFilterRunner)
	 */
	@Override
	public int showDialog(final ImagePlus imp, final String command,
		final PlugInFilterRunner pfr)
	{
		final String searchPath = IJ.getDirectory("image");
		final LinkedList<String> foundCharacterisationResults =
			new LinkedList<String>();
		findDatasets(searchPath, foundCharacterisationResults, imp.getShortTitle());
		if (foundCharacterisationResults.size() > 1) {
			/*
			 * A dialog is presented to select one of the found files.
			 */
			final GenericDialog gd = new GenericDialog((command != "") ? command
				: "Debugging" + " - Select data set", IJ.getInstance());
			String[] arrayOfFoundResults = new String[foundCharacterisationResults
				.size()];
			arrayOfFoundResults = foundCharacterisationResults.toArray(
				arrayOfFoundResults);
			gd.addRadioButtonGroup(SR_EELS.FILENAME_RESULTS, arrayOfFoundResults,
				foundCharacterisationResults.size(), 1, foundCharacterisationResults
					.get(0));
			gd.setResizable(false);
			gd.showDialog();
			if (gd.wasCanceled()) {
				canceled();
				return NO_CHANGES | DONE;
			}
			if (foundCharacterisationResults.size() > 1) {
				pathResults = gd.getNextRadioButton();
			}
		}
		else {
			/*
			 * If only no file has been found, the parameters dialog is shown.
			 */
			if (foundCharacterisationResults.size() == 0) {

				do {
					if (showParameterDialog(command) == CANCEL) {
						canceled();
						return NO_CHANGES | DONE;
					}
				}
				while (!pathResults.contains(SR_EELS.FILENAME_RESULTS));
			}
			else {
				if (foundCharacterisationResults.size() == 1) {
					pathResults = foundCharacterisationResults.getFirst();
				}
				else {
					canceled();
					return NO_CHANGES | DONE;
				}
			}
		}
		inputProcessor = new SR_EELS_FloatProcessor((FloatProcessor) imp
			.getProcessor(), CameraSetup.getFullWidth() / imp.getWidth(), CameraSetup
				.getFullHeight() / imp.getHeight(), imp.getWidth() / 2, imp
					.getHeight() / 2);
		title = StringManipulator.removeExtensionFromTitle(imp.getTitle());
		return FLAGS;
	}

	/**
	 * Searches the given folder for a data set file. Recursion is used to search
	 * in sub-folders.
	 *
	 * @param searchPath the folder to search in.
	 * @param found a {@link Vector} that stores all found file paths.
	 * @param filename is the full name of the file we search for.
	 */
	private void findDatasets(final String searchPath,
		final LinkedList<String> found, final String filename)
	{
		final Pattern patternDate = Pattern.compile("(\\d{8})");
		final Matcher matcher = patternDate.matcher(filename);
		matcher.find();
		final String date = matcher.group(1);
		final String[] entries = new File(searchPath).list();
		for (final String entrie : entries) {
			if (new File(searchPath + entrie).isDirectory() & entrie.contains(date)) {
				final String[] subEntries = new File(searchPath + entrie).list();
				for (final String subEntrie : subEntries) {
					if (new File(searchPath + entrie + File.separator + subEntrie)
						.isDirectory())
					{
						found.add(searchPath + entrie + File.separator + subEntrie +
							File.separator + FILENAME_RESULTS);
					}
				}
			}
		}
	}

	/**
	 * @param dialogTitle
	 * @return The constant <code>OK</code> or <code>CANCEL</code>.
	 */
	private int showParameterDialog(final String dialogTitle) {
		final GenericDialogPlus gd = new GenericDialogPlus((dialogTitle != "")
			? dialogTitle : "Debugging" + " - set parameters", IJ.getInstance());
		gd.addFileField(SR_EELS.FILENAME_RESULTS, pathResults);
		// TODO Add drop down menu for correction method.
		gd.setResizable(false);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return CANCEL;
		}
		pathResults = gd.getNextString();
		return OK;
	}

	/**
	 * Cancel the plugin and show a status message.
	 */
	private void canceled() {
		IJ.showStatus("SR-EELS correction has been canceled.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.filter.ExtendedPlugInFilter#setNPasses(int)
	 */
	@Override
	public void setNPasses(final int nPasses) {
		// This method is not used.
	}

	private static void setupProgress(final int fullProgress) {
		progressSteps = fullProgress;
		progress = 0;
	}

	private static void updateProgress() {
		progress++;
		IJ.showProgress(progress, progressSteps);
	}

	/**
	 * <p>
	 * This main method is used for testing. It starts ImageJ, loads a test image
	 * and starts the plugin.
	 * </p>
	 * <p>
	 * User interaction is necessary, as the plugin uses a GUI.
	 * </p>
	 * <p>
	 * <a href=
	 * "https://github.com/imagej/minimal-ij1-plugin/blob/master/src/main/java/Process_Pixels.java"
	 * >see minimal-ij1-plugin on GitHub</a>
	 * </p>
	 *
	 * @param args
	 */
	public static void main(final String[] args) {
		EFTEMj_Debug.setDebugLevel(EFTEMj_Debug.DEBUG_FULL);
		/*
		 * start ImageJ
		 */
		new ImageJ();

		String baseFolder = "C:/Temp/";
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf(
			"aix") > 0)
		{
			final String userHome = System.getProperty("user.home");
			baseFolder = userHome + "/Downloads/";
		}

		/*
		 * Check if the test image is available. Otherwise prompt a message with the
		 * download link.
		 */
		final File testImage = new File(baseFolder +
			"20140106 SM125 -20%/20140106_SR-EELS_TestImage_small.tif");
		if (!testImage.exists()) {
			final String url = "http://eftemj.entrup.com.de/SR-EELS_TestImage.zip";
			/*
			 * IJ.showMessage("Test image not found", "<html>" + "Please download the file" + "<br />" + "<a href='" +
			 * url + "'>SR-EELS_TestImage.zip</a> from" + "<br/>" + url + "<br />" + "and extract it to 'C:\\temp\\'." +
			 * "</html>");
			 */
			final GenericDialog gd = new GenericDialog("Test image not found");
			gd.addMessage(
				"Please download the file 'SR-EELS_TestImage.zip' and extract it to '" +
					baseFolder + "'.");
			gd.addMessage(
				"Copy the following link, or click Ok to open it with your default browser.");
			gd.addStringField("", url, url.length());
			gd.showDialog();
			if (gd.wasOKed()) {
				try {
					final URI link = new URI(url);
					final Desktop desktop = Desktop.getDesktop();
					desktop.browse(link);
				}
				catch (final Exception exc) {
					IJ.showMessage("An Exception occured", exc.getMessage());
					return;
				}
			}
			return;
		}
		/*
		 * open the test image
		 */
		final ImagePlus image = IJ.openImage(baseFolder +
			"20140106 SM125 -20%/20140106_SR-EELS_TestImage_small.tif");
		image.show();

		/*
		 * run the plugin
		 */
		final Class<?> clazz = SR_EELS_CorrectionPlugin.class;
		IJ.runPlugIn(clazz.getName(), "");
	}

	/**
	 * <p>
	 * This class is used to load a data file that contains a data set for the fit
	 * of a 2D polynomial. For each y-value there is are pairs of x-values that is
	 * stored at a 2D array.
	 * </p>
	 * <p>
	 * The data file must contain one data point at each line. Each data point
	 * contains of x1, x2 and y separated by whitespace. Lines that contain a '#'
	 * are regarded as comments.
	 * </p>
	 * <p>
	 * The Plugin {@link SR_EELS_CharacterisationPlugin} creates files that can be
	 * processed by this class.
	 * </p>
	 *
	 * @author Michael Entrup b. Epping
	 */
	@SuppressWarnings("serial")
	private static class DataImporter extends ArrayList<float[]> {

		private final int slices;

		public DataImporter(final String resultsFilePath,
			final boolean readWeights)
		{
			super();
			final ImageStack file = IJ.openImage(resultsFilePath).getImageStack();
			slices = file.getSize();
			for (int i = 1; i <= file.getSize(); i++) {
				for (int y = 0; y < file.getHeight(); y++) {
					final float[] point = new float[14];
					for (int x = 0; x < file.getWidth(); x++) {
						point[x] = file.getProcessor(i).getf(x, y);
					}
					this.add(point);
				}
			}
		}

		public float[] getYInterceptPoint(final int index) {
			final int height = size() / slices;
			final int currentSlice = (int) Math.floor(index / height);
			final int interceptINdex = currentSlice * height + height / 2;
			return get(interceptINdex);
		}
	}

}
