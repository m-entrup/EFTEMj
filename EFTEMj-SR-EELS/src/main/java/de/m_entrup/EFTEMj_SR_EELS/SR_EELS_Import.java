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

package de.m_entrup.EFTEMj_SR_EELS;

import java.util.ArrayList;

import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import ij.IJ;
import ij.Prefs;
import ij.plugin.PlugIn;

public class SR_EELS_Import implements PlugIn {

	private String databasePath;

	@Override
	public void run(final String arg) {
		/*
		 * Read path to database from IJ_Prefs.txt or ask the user to set the path.
		 */
		IJ.showStatus("Loading path of database from IJ_Prefs.txt.");
		databasePath = Prefs.get(SR_EELS_PrefsKeys.characterisationDatabasePath
			.getValue(), null);
		if (databasePath != null) {
			databasePath = IJ.getDirectory("Set the path to the database...");
			if (databasePath != null) return;
			Prefs.set(SR_EELS_PrefsKeys.characterisationDatabasePath.getValue(),
				databasePath);
			Prefs.savePreferences();
		}
		/*
		 *  Step 1
		 *  Select the folder to import from.
		 */
		String path;
		IJ.showStatus("Loading files for import.");
		if ((path = IJ.getDirectory("Characterisation files...")) == null) return;
		/*
		 *  Step 2
		 *  Show list of files to select from.
		 */
		ArrayList<String> files;
		if ((files = selectFiles(path)) == null) return;
		/*
		 *  Step 3
		 *  Show a dialog to amend the parameters
		 */
		ParameterSet parameters;
		if ((parameters = getParameters(path)) == null) return;
		/*
		 *  Step 4
		 *  Import files to the database.
		 */
		if (parameters.date != null & parameters.SpecMag != null &
			parameters.QSinK7 != null)
		{
			saveFiles(path, files, parameters);
		}
		IJ.showStatus("Finished importing from " + path);
	}

	private ArrayList<String> selectFiles(final String path) {
		// TODO Auto-generated method stub
		return null;
	}

	private ParameterSet getParameters(final String path) {
		// TODO Auto-generated method stub
		return null;
	}

	private void saveFiles(final String path, final ArrayList<String> files,
		final ParameterSet parameters)
	{
		// TODO Auto-generated method stub

	}

	public static void main(final String[] args) {
		EFTEMj_Debug.debug(SR_EELS_Import.class);
	}

	private class ParameterSet {

		String date;
		String QSinK7;
		String comment;
		String SpecMag;
	}
}
