/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2015, Michael Entrup b. Epping
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

package de.m_entrup.EFTEMj_SR_EELS.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;

import de.m_entrup.EFTEMj_SR_EELS.shared.SR_EELS_ConfigurationManager;
import de.m_entrup.EFTEMj_lib.EFTEMj_Configuration;
import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/**
 * A plugin to import SR-EELS files into a folder based database.
 * <p>
 * The folder structure is based on recording parameters (SpecMag, QSinK7 and
 * date). The parameters are extracted from the full path of the selected files
 * by using regular expressions.
 * </p>
 * <p>
 * The imported files are saved as tif. They are rotated 90° left to show energy
 * loss on the x-axis and lateral information on the y-axis.
 * </p>
 *
 * @author Michael Entrup b. Epping
 */
public class SR_EELS_ImportPlugin implements PlugIn {

	/**
	 * Path that contains the folder based database.
	 */
	private String databasePath;
	/**
	 * File types that can be imported. ".dm3" is the default value. It can be
	 * overwritten by an entry in EFTEMj_config.xml.
	 */
	private String[] fileTypeToImport = { ".dm3" };
	private boolean importAsCalibration = false;
	private boolean automaticImport = false;
	private EFTEMj_Configuration config;
	private static String configKeyPrefix = "SR-EELS." +
		SR_EELS_ImportPlugin.class.getSimpleName() + ".";
	public static String databasePathKey = configKeyPrefix + "databasePath";
	public static String fileTypesKey = configKeyPrefix + "fileTypeToImport";
	private static String usedBeforeKey = configKeyPrefix + "usedBefore";
	public static String rotateOnImportKey = configKeyPrefix + "rotateOnImport";

	@Override
	public void run(final String arg) {
		try {
			config = SR_EELS_ConfigurationManager.getConfiguration();
		}
		catch (final ConfigurationException e) {
			IJ.error("Failed to load config.", e.toString());
			return;
		}
		databasePath = config.getString(databasePathKey);
		if (databasePath == null | databasePath.equals("")) {
			IJ.showMessage("No database found...",
				"A database path is not jet defiend.\nRun the \"Setup EFTEMj SR-EELS\" first.");
			return;
		}
		if (!databasePath.endsWith("/") & !databasePath.endsWith("\\")) {
			databasePath += File.separator;
		}
		final boolean firstUse = !config.getBoolean(usedBeforeKey);
		if (firstUse) {
			IJ.showMessage("Your first import...",
				"Select a folder that contains the files you want to import.");
			// ToDo Add a better documentation.
			config.setProperty(usedBeforeKey, true);
			config.save();
		}
		/*
		 * Read files to be imported from IJ_Prefs.txt.
		 */
		IJ.showStatus("Loading supported file types from IJ_Prefs.txt.");

		if (config.getStringArray(fileTypesKey).length > 0) {
			fileTypeToImport = config.getStringArray(fileTypesKey);
		}
		/*
		 * Step 1 Select the folder to import from.
		 */
		String inputPath;
		IJ.showStatus("Loading files for import.");
		if ((inputPath = IJ.getDirectory("Characterisation files...")) == null)
			return;
		/*
		 * Step 2 Show list of files to select from.
		 */
		ArrayList<String> files;
		if ((files = selectFiles(inputPath)) == null) return;
		/*
		 * Step 3 Import calibration or import individual SR-EELS measurements
		 * Show a dialog to amend the parameters
		 */
		if (importAsCalibration) {
			/*
			 * Step 3.1 Show a dialog to amend the parameters
			 */
			ParameterSet parameters;
			if ((parameters = getParameters(inputPath, true)) == null) return;
			/*
			 * Step 3.2 Import files to the database.
			 */
			if (parameters.date != null & parameters.SpecMag != null &
				parameters.QSinK7 != null)
			{
				saveFiles(inputPath, files, parameters);
			}
		}
		else {

			for (final String file : files) {
				/*
				 * Step 3.1 Show a dialog to amend the parameters
				 */
				ParameterSet parameters;
				if ((parameters = getParameters(inputPath + file, false)) == null)
					return;
				/*
				 * Step 3.2 Import files to the database.
				 */
				if (parameters.date != null & parameters.SpecMag != null &
					parameters.QSinK7 != null & parameters.fileName != null)
				{
					saveFile(inputPath, file, parameters);
				}
			}
		}
		IJ.showStatus("Finished importing from " + inputPath);
	}

	private ArrayList<String> selectFiles(final String path) {
		final File folder = new File(path);
		final String[] list = folder.list();
		final GenericDialog gd = new GenericDialog("Select files");
		gd.addMessage("Files:");
		final boolean[] shown = new boolean[list.length];
		Arrays.fill(shown, false);
		int counter = 0;
		for (int i = 0; i < list.length; i++) {
			if (new File(path + list[i]).isFile() & checkFileType(list[i])) {
				counter++;
				shown[i] = true;
				if (!list[i].contains("-exclude")) {
					gd.addCheckbox(list[i], true);
				}
				else {
					gd.addCheckbox(list[i], false);
				}
			}
		}
		if (counter < 1) {
			final StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(fileTypeToImport[0]);
			for (int i = 1; i < fileTypeToImport.length; i++) {
				strBuilder.append(";" + fileTypeToImport[i]);
			}
			IJ.showMessage("Script aborted", "There are no files to import in\n" +
				path + "\nSupported file types:\n" + strBuilder.toString());
			return null;
		}
		gd.addMessage("Options:");
		gd.addCheckbox("Import as calibration", false);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return null;
		}
		final ArrayList<String> files = new ArrayList<String>();
		for (int i = 0; i < list.length; i++) {
			if (shown[i] == true) {
				if (gd.getNextBoolean()) {
					files.add(list[i]);
				}
			}
		}
		importAsCalibration = gd.getNextBoolean();
		return files;
	}

	private boolean checkFileType(final String string) {
		boolean isType = false;
		for (final String type : fileTypeToImport) {
			if (string.endsWith(type)) isType = true;
		}
		return isType;
	}

	private ParameterSet getParameters(final String path,
		final boolean importCalibration)
	{
		final Pattern patternDate = Pattern.compile("(\\d{8})");
		final Pattern patternSM = Pattern.compile("(?:SM|SpecMag)(\\d{2,3})");
		final Pattern patternQSinK7 = Pattern.compile(
			"QSinK7\\s?[\\s|=]\\s?(-?\\+?\\d{1,3})%?");
		final Pattern patternQSinK7Alternative = Pattern.compile(
			"(-?\\+?\\d{1,3})%");
		final ParameterSet parameters = new ParameterSet();
		Matcher matcher = patternDate.matcher(path);
		/*
		 * The while loop is used to find the last match of the given RegExp.
		 * Index 0 of the array is the complete match. All following indices
		 * reference the groups.
		 */
		while (matcher.find()) {
			parameters.date = matcher.group();
		}
		matcher = patternSM.matcher(path);
		while (matcher.find()) {
			parameters.SpecMag = matcher.group(1);
		}
		matcher = patternQSinK7.matcher(path);
		while (matcher.find()) {
			parameters.QSinK7 = matcher.group(1);
		}
		if (parameters.QSinK7 == "") {
			matcher = patternQSinK7Alternative.matcher(path);
			while (matcher.find()) {
				parameters.QSinK7 = matcher.group(1);
			}
		}
		if (importAsCalibration == false) {
			parameters.fileName = parameters.date + "_" + new File(path).getName()
				.replaceFirst("\\.\\w+$", "");
		}
		final GenericDialog gd = new GenericDialog("Set parameters");
		gd.addStringField("date:", parameters.date, 8);
		gd.addStringField("SpecMag:", parameters.SpecMag, 8);
		gd.addStringField("QSinK7:", parameters.QSinK7, 8);
		if (importCalibration) {
			gd.addStringField("comment:", parameters.comment, 24);
		}
		else {
			gd.addStringField("file name:", parameters.fileName, parameters.fileName
				.length() + 8);
			gd.addCheckbox("automatic import", automaticImport);
		}
		if (automaticImport == false | importAsCalibration == true) {
			gd.showDialog();
			if (gd.wasCanceled()) {
				return null;
			}
			parameters.date = gd.getNextString();
			parameters.SpecMag = gd.getNextString();
			parameters.QSinK7 = gd.getNextString();
			if (importCalibration) {
				parameters.comment = gd.getNextString();
			}
			else {
				parameters.fileName = gd.getNextString();
				automaticImport = gd.getNextBoolean();
			}
			/*
			 * We replace space by underscore to easily recognize the complete
			 * comment. This is useful when further processing is done.
			 */
			parameters.comment = parameters.comment.replace(" ", "_");
		}
		return parameters;
	}

	private void saveFiles(final String inputPath, final ArrayList<String> files,
		final ParameterSet parameters)
	{
		String output = databasePath + "SM" + parameters.SpecMag + "/" +
			parameters.QSinK7 + "/" + parameters.date;
		if (parameters.comment.length() > 0) {
			output += " " + parameters.comment;
		}
		output += "/";
		final File folder = new File(output);
		if (folder.exists()) {
			IJ.showMessage("Script aborted", "This data set already exists\n" + folder
				.toString().replace(databasePath, ""));
			return;
		}
		folder.mkdirs();
		for (int index = 0; index < files.size();) {
			final ImagePlus imp = IJ.openImage(inputPath + files.get(index));
			final String rotation = config.getString(rotateOnImportKey).toLowerCase();
			if (rotation.equals("left")) {
				IJ.run(imp, "Rotate 90 Degrees Left", "");
			}
			else if (rotation.equals("right")) {
				IJ.run(imp, "Rotate 90 Degrees Right", "");
			}
			IJ.save(imp, output + "Cal_" + pad(++index, 2) + ".tif");
			imp.close();
		}
	}

	private void saveFile(final String inputPath, final String file,
		final ParameterSet parameters)
	{
		final String output = databasePath + "SM" + parameters.SpecMag + "/" +
			parameters.QSinK7 + "/";
		final File folder = new File(output);
		folder.mkdirs();
		if (new File(output + parameters.fileName + ".tif").exists()) {
			IJ.log("Skipping " + file + " - file already exists in database.");
			return;
		}
		final ImagePlus imp = IJ.openImage(inputPath + file);
		IJ.run(imp, "Rotate 90 Degrees Left", "");
		IJ.save(imp, output + parameters.fileName + ".tif");
		imp.close();
	}

	/**
	 * Adding zeroes in front of the {@link Integer} to match the given length.
	 *
	 * @param number to convert.
	 * @param length of the created {@link String}.
	 * @return a {@link String} that matches the given length.
	 */
	private String pad(final int number, final int length) {
		String s = number + "";
		while (s.length() < length)
			s = "0" + s;
		return s;
	}

	public static void main(final String[] args) {
		System.out.println("Printing all used configuration keys:");
		System.out.println(SR_EELS_ImportPlugin.databasePathKey);
		System.out.println(SR_EELS_ImportPlugin.fileTypesKey);
		System.out.println(SR_EELS_ImportPlugin.usedBeforeKey);
		System.out.println(SR_EELS_ImportPlugin.rotateOnImportKey);
		System.out.println("");
		EFTEMj_Debug.debug(SR_EELS_ImportPlugin.class);
	}

	private class ParameterSet {

		String SpecMag = "";
		String QSinK7 = "";
		String date = "";
		String comment = "";
		String fileName = "";
	}
}
