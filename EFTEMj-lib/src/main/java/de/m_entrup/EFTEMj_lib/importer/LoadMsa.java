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
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 * Get a {@link BufferedReader} and load the energy loss data line by line.
	 * The data is stored in an {@link EnergyLossData} object.
	 *
	 * @throws IOException
	 * @param path is the full path to the msa file to load.
	 */
	public LoadMsa(final String path) throws IOException {
		final BufferedReader br = new BufferedReader(new FileReader(new File(
			path)));
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
