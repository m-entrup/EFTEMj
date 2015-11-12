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

import ij.IJ;
import ij.Prefs;

/**
 * @author Michael Entrup b. Epping
 */
public class CameraSetup {

	/**
	 * @return the height of the camera stored at the ImageJ preferences.
	 */
	public static int getFullHeight() {
		int height = (int) Prefs.get(PrefsKeys.cameraHeight.getValue(), -1);
		if (height == -1) {
			height = (int) IJ.getNumber(
				"The height of the used camera is not saved. Please enter the height in Pixel:",
				4096);
			Prefs.set(PrefsKeys.cameraHeight.getValue(), height);
			Prefs.savePreferences();
		}
		return height;
	}

	/**
	 * @return the width of the camera stored at the ImageJ preferences.
	 */
	public static int getFullWidth() {
		int width = (int) Prefs.get(PrefsKeys.cameraWidth.getValue(), -1);
		if (width == -1) {
			width = (int) IJ.getNumber(
				"The width of the used camera is not saved. Please enter the width in Pixel:",
				4096);
			Prefs.set(PrefsKeys.cameraWidth.getValue(), width);
			Prefs.savePreferences();
		}
		return width;
	}
}
