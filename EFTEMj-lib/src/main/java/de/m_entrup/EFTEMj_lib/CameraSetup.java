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

package de.m_entrup.EFTEMj_lib;

import org.apache.commons.configuration.ConfigurationException;

import ij.IJ;

/**
 * @author Michael Entrup b. Epping
 */
public class CameraSetup {

	// TODO Change implementation

	private static String configKeyPrefix = "lib." + CameraSetup.class.getSimpleName() + ".";
	public static String heightKey = configKeyPrefix + "height";
	public static String widthKey = configKeyPrefix + "width";

	/**
	 * @return the height of the camera stored at the ImageJ preferences.
	 * @throws ConfigurationException
	 */
	public static int getFullHeight() {
		EFTEMj_Configuration config;
		try {
			config = EFTEMj_ConfigurationManager.getConfiguration();

			int height = config.getInt(heightKey);
			if (height <= 0) {
				height = (int) IJ.getNumber(
						"The height of the used camera is not saved. Please enter the height in Pixel:", 4096);
				config.setProperty(heightKey, height);
				config.save();
			}
			return height;
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return 1;
	}

	/**
	 * @return the width of the camera stored at the ImageJ preferences.
	 * @throws ConfigurationException
	 */
	public static int getFullWidth() {
		EFTEMj_Configuration config;
		try {
			config = EFTEMj_ConfigurationManager.getConfiguration();
			int width = config.getInt(widthKey);
			if (width <= 0) {
				width = (int) IJ
						.getNumber("The width of the used camera is not saved. Please enter the width in Pixel:", 4096);
				config.setProperty(widthKey, width);
				config.save();
			}
			return width;
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

	public static void main(final String[] args) {
		System.out.println("Printing all used configuration keys:");
		System.out.println(CameraSetup.heightKey);
		System.out.println(CameraSetup.widthKey);

	}
}
