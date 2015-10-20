
package de.m_entrup.EFTEMj_SR_EELS;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.io.DirectoryChooser;
import ij.plugin.PlugIn;

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
			saveResults(results);
			results.timeStop = new Date().getTime();

			IJ.log("Finished in " + Math.round((results.timeStop -
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
		final ArrayList<String> images = results.settings.images;
		for (int i = 0; i < images.size(); i++) {
			final String imageName = images.get(i);
//			results[imageName] = {};
//			results[imageName].result = new Array();
//			image = new ImageObject(images[i], settings);
//			imp = image.imp;
//			results[imageName].width = imp.getWidth();
//			results[imageName].height = imp.getHeight();
//			yPos = settings.energyBorderLow;
//			xOffset = 0;
//			roiWidth = image.width;
//			mean = new Array();
//			while (yPos < image.height - settings.energyBorderHigh) {
//				imp.setRoi(new Rectangle(xOffset, yPos, roiWidth, settings.stepSize));
//				subImage = new SubImageObject(new Duplicator().run(imp), xOffset, yPos);
//				profile = new ProfilePlot(subImage.imp);
//				xValues = new Array();
//				for (p = 0; p < profile.getProfile().length; p++) {
//					xValues.push(p);
//				}
//				fit = new CurveFitter(xValues, profile.getProfile());
//				fit.doFit(CurveFitter.GAUSSIAN);
//				gaussCentre = fit.getParams()[2];
//				gaussSigma =  fit.getParams()[3];
//				gaussSigmaWeighted = settings.sigmaWeight * gaussSigma / Math.pow(fit.getRSquared(), 2);
//				xOffset = Math.max(xOffset + Math.round(gaussCentre - gaussSigmaWeighted), 0);
//				roiWidth = Math.round(2 * gaussSigmaWeighted);
//				imp.setRoi(new Rectangle(xOffset, yPos, roiWidth, settings.stepSize));
//				subImage = new SubImageObject(new Duplicator().run(imp), xOffset, yPos);
//				subImage.parent = image;
//				subImage.xOffset = xOffset;
//				subImage.yOffset = yPos;
//				subImage.threshold = settings.threshold;
//				results[imageName].result.push(runCharacterisationSub(subImage, settings.useThresholding));
//				yPos += settings.stepSize;
//			}
//			result = results[imageName].result;
//			xValues = new Array();
//			yValues = new Array();
//			leftValues = new Array();
//			rightValues = new Array();
//			widthValues = new Array();
//			limit = new Array();
//			for (j in result) {
//				xValues.push(result[j].y);
//				yValues.push(result[j].x);
//				leftValues.push(result[j].left);
//				rightValues.push(result[j].right);
//				widthValues.push(result[j].right - result[j].left);
//				limit.push(result[j].limit);
//			}
//			plot = new Plot("JavaScript", "Spec of " +imageName + " (" + settings.useThresholding + ")", "position x", "position y", xValues, yValues);
//			plot.add("", xValues, leftValues);
//			plot.add("", xValues, rightValues);
//			if (!settings.useThresholding) plot.add("CROSS", xValues, limit);
//			plot.setColor(java.awt.Color.RED);
//			plot.add("", xValues, widthValues);
//			plot.setLimits(0, imp.getHeight() - 1, 0, imp.getWidth() - 1);
//			plot.show();
//			result.leftFit = new CurveFitter(xValues, leftValues);
//			result.leftFit.doFit(CurveFitter.POLY3);
//			result.centreFit = new CurveFitter(xValues, yValues);
//			result.centreFit.doFit(CurveFitter.POLY3);
//			result.rightFit = new CurveFitter(xValues, rightValues);
//			result.rightFit.doFit(CurveFitter.POLY3);
		}
	}

	private void saveResults(final SR_EELS_CharacterisationResults results) {
		// TODO Auto-generated method stub

	}

	public static void main(final String[] args) {
		EFTEMj_Debug.debug(SR_EELS_Characterisation.class);
	}

	protected class SR_EELS_CharacterisationSettings {

		int stepSize = 64;
		double filterRadius = Math.sqrt(stepSize);
		int energyBorderLow = 2 * stepSize;
		int energyBorderHighw = 2 * stepSize;
		float energyPosition = 0.5f;
		float sigmaWeight = 3f;
		int polynomialOrder = 3;
		boolean useThresholding = true;
		String threshold = "Li";
		String path = "";
		ArrayList<String> images;
	}

	private class SR_EELS_CharacterisationResults {

		SR_EELS_CharacterisationSettings settings;
		long timeStart;
		long timeStop;
	}

	private class InternalError extends Error {

		public InternalError(final String message) {
			super(message);
		}

	}
}
