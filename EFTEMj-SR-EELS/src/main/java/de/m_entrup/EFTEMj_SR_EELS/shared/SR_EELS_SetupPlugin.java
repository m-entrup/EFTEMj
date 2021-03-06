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

package de.m_entrup.EFTEMj_SR_EELS.shared;

import java.awt.Font;
import java.util.Arrays;

import org.apache.commons.configuration.ConfigurationException;

import de.m_entrup.EFTEMj_SR_EELS.characterisation.SR_EELS_CharacterisationPlugin;
import de.m_entrup.EFTEMj_SR_EELS.importer.SR_EELS_ImportPlugin;
import de.m_entrup.EFTEMj_lib.EFTEMj_Configuration;
import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.plugin.PlugIn;

public class SR_EELS_SetupPlugin implements PlugIn {

	private EFTEMj_Configuration config;
	private String defaultFontName;
	private int defaultFontSize;
	private int largerFontSize;

	@Override
	public void run(final String arg0) {
		try {
			config = SR_EELS_ConfigurationManager.getConfiguration();
		} catch (final ConfigurationException e) {
			IJ.error("Failed to load config.", e.toString());
			return;
		}
		final GenericDialogPlus gd = new GenericDialogPlus("Setup EFTEMj SR-EELS", IJ.getInstance());
		defaultFontName = gd.getFont().getName();
		defaultFontSize = gd.getFont().getSize();
		largerFontSize = (int) Math.round(1.2 * defaultFontSize);
		addImportSection(gd);
		addCharacterisationSection(gd);

		gd.showDialog();
		if (gd.wasCanceled())
			return;
		saveImportSection(gd);
		saveCharacterisationSection(gd);
	}

	private void addImportSection(final GenericDialogPlus gd) {
		gd.addMessage("SR-EELS import:", new Font(defaultFontName, Font.BOLD, largerFontSize));

		gd.addMessage("Database (Folder to store imported images)");
		gd.addDirectoryField("", config.getString(SR_EELS_ImportPlugin.databasePathKey));
		final String[] fileTypeArray = config.getStringArray(SR_EELS_ImportPlugin.fileTypesKey);
		String fileTypesToImport = Arrays.toString(fileTypeArray);
		fileTypesToImport = fileTypesToImport.replaceAll("\\[", "");
		fileTypesToImport = fileTypesToImport.replaceAll("\\]", "");
		fileTypesToImport = fileTypesToImport.replaceAll(",", ";");
		gd.addMessage("File types to import (separate different file types by a ;)");
		gd.addStringField("", fileTypesToImport, 25);
		gd.addMessage("Rotate on import");
		final String[] rotationArray = { "no", "Left", "Right" };
		gd.addChoice("", rotationArray, config.getString(SR_EELS_ImportPlugin.rotateOnImportKey));
	}

	private void saveImportSection(final GenericDialogPlus gd) {
		final String databasePath = gd.getNextString();
		final String fileTypesToImport = gd.getNextString();
		final String rotateOnImport = gd.getNextChoice();

		config.setProperty(SR_EELS_ImportPlugin.databasePathKey, databasePath);
		config.setProperty(SR_EELS_ImportPlugin.fileTypesKey, fileTypesToImport.split(";"));
		config.setProperty(SR_EELS_ImportPlugin.rotateOnImportKey, rotateOnImport);
		config.save();
	}

	private void addCharacterisationSection(final GenericDialogPlus gd) {
		gd.addMessage("SR-EELS characterisation:", new Font(defaultFontName, Font.BOLD, largerFontSize));

		gd.addCheckbox("Save plots as stack (.tif)", config.getBoolean(SR_EELS_CharacterisationPlugin.plotsAsStackKey));
	}

	private void saveCharacterisationSection(final GenericDialogPlus gd) {
		final boolean plotsAsStack = gd.getNextBoolean();

		config.setProperty(SR_EELS_CharacterisationPlugin.plotsAsStackKey, plotsAsStack);
		config.save();
	}

	public static void main(final String[] args) {
		EFTEMj_Debug.debug(SR_EELS_SetupPlugin.class);
	}

}
