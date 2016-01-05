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

package de.m_entrup.EFTEMj_lib.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import de.m_entrup.EFTEMj_lib.data.EnergyLossData;
import de.m_entrup.EFTEMj_lib.data.EnergyLossPoint;

/**
 * <p>
 * This class is designed to load a msa file and make available the energy loss
 * and the counts as float arrays.
 * </p>
 * <p>
 * Additionally the data is stored in a {@link EnergyLossData} object, to allow
 * more complex operations on the loaded data.
 * </p>
 *
 * @author Michael Entrup b. Epping
 */
public class LoadMsa {

	/**
	 * Object to store the imported data.
	 */
	private final EnergyLossData eels;
	/**
	 * Name of the file data is imported from.
	 */
	private String fileName;

	/**
	 * Get a {@link BufferedReader} and load the energy loss data line by line.
	 * The data is stored in an {@link EnergyLossData} object.
	 *
	 * @throws IOException
	 */
	public LoadMsa() throws IOException {
		final BufferedReader br = getReader();
		if (br == null) throw new IOException(
			"The user has canceld the file selection.");
		String s = null;
		eels = new EnergyLossData();
		while ((s = br.readLine()) != null) {
			if (!s.startsWith("#")) {
				final String[] items = s.split(",");
				eels.add(new EnergyLossPoint(Double.parseDouble(items[0]), Double
					.parseDouble(items[1])));
			}
			else {
				String extract = extractMatch("#XUNITS\\s*:\\s*(\\S+)", s);
				if (extract != null) {
					eels.setXUnit(extract);
					continue;
				}
				extract = extractMatch("#YUNITS\\s*:\\s*(\\S+)", s);
				if (extract != null) {
					eels.setYUnit(extract);
					continue;
				}
			}
		}
		br.close();
	}

	private String extractMatch(final String pattern, final String input) {
		final Pattern pat = Pattern.compile(pattern);
		final Matcher match = pat.matcher(input);
		if (!match.find()) return null;
		final String s = match.group(1);
		return s;
	}

	/**
	 * @return the name of the file data is imported from.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return a {@link BufferedReader} that is created on a user selected file.
	 * @throws FileNotFoundException
	 */
	private BufferedReader getReader() throws FileNotFoundException {
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new MsaFilter());
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			fileName = fc.getSelectedFile().getName();
			return new BufferedReader(new FileReader(fc.getSelectedFile()));
		}
		else {
			fileName = "no file";
			return null;
		}
	}

	/**
	 * @return an array with energy loss values imported from the selected file.
	 */
	public float[] getEnergyArray() {
		return eels.getEnergyArray();
	}

	/**
	 * @return an array with counts imported from the selected file.
	 */
	public float[] getCountArray() {
		return eels.getCountArray();
	}

	/**
	 * @return the energy loss object to use it for further processing.
	 */
	public EnergyLossData getEnergyLossData() {
		return eels;
	}

	/**
	 * A {@link FileFilter} used by a {@link JFileChooser} to show only
	 * directories and msa files.
	 *
	 * @author Michael Entrup b. Epping
	 */
	private class MsaFilter extends FileFilter {

		/* (non-Javadoc)
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return "File format for electron energy loss spectra.";
		}

		/* (non-Javadoc)
		 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(final File f) {
			if (f.isDirectory()) return true;
			if (f.getName().toLowerCase().endsWith(".msa")) return true;
			return false;
		}
	}

	/**
	 * @return the x-unit specified in the meta data of the selected file.
	 */
	public String getXUnit() {
		return eels.getXUnit();
	}

	/**
	 * @return the y-unit specified in the meta data of the selected file.
	 */
	public String getYUnit() {
		return eels.getYUnit();
	}
}
