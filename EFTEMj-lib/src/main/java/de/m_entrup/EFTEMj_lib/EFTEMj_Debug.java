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
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

/**
 * This class is used for debugging. The main method runs ImageJ and sets the
 * plugin directory. There are additional methods for logging.
 *
 * @author Michael Entrup b. Epping
 */
public class EFTEMj_Debug {

	/**
	 * Disable all debugging.
	 */
	public static final int DEBUG_NONE = 0;
	/**
	 * Reduce the debug logging to the most essential information.
	 */
	public static final int DEBUG_MINIMAL_LOGGING = 31;
	/**
	 * Write all debug logs to a file.
	 */
	public static final int DEBUG_LOGGING = 63;
	/**
	 * Show all debug logs in the ImageJ log window.
	 */
	public static final int DEBUG_IN_APP_LOGGING = 127;
	/**
	 * Display images that have only debugging purpose.
	 */
	public static final int DEBUG_SHOW_IMAGES = 191;
	/**
	 * Don't hide any information.
	 */
	public static final int DEBUG_FULL = 255;
	/**
	 * The current level of debugging.
	 */
	private static int debugLevel = DEBUG_NONE;

	/**
	 * @return The current level of debugging.
	 */
	public static int getDebugLevel() {
		return debugLevel;
	}

	/**
	 * Use the constants defined in {@link EFTEMj_Debug} to set the correct
	 * level of debugging.
	 *
	 * @param debugLevel
	 *            can be a value between 0 (no debugging) and 255 (full
	 *            debugging).
	 */
	public static void setDebugLevel(final int debugLevel) {
		EFTEMj_Debug.debugLevel = debugLevel;
	}

	/**
	 * Start ImageJ.
	 *
	 * @param args
	 *            Not used
	 */
	public static void main(final String[] args) {
		debug(null);
	}

	/**
	 * Start {@link ImageJ} and run a {@link PlugIn}.
	 *
	 * @param caller
	 *            is a class that contains a run method.
	 */
	public static void debug(final Class<?> caller) {
		EFTEMj_Debug.debugLevel = DEBUG_FULL;
		EFTEMj_Debug.debug(caller, debugLevel);
	}

	/**
	 * Start {@link ImageJ} and run a {@link PlugIn}.
	 *
	 * @param caller
	 *            is a class that contains a run method.
	 * @param debugLevel
	 *            defines the used level of debugging.
	 */
	@SuppressWarnings("hiding")
	public static void debug(final Class<?> caller, final int debugLevel) {
		EFTEMj_Debug.debugLevel = debugLevel;
		// start ImageJ
		final ImageJ ij = new ImageJ();
		System.out.println("Testing with " + ij.getInfo());

		if (caller != null) {
			// run the plugin
			IJ.runPlugIn(caller.getName(), "");
		}
	}

	/**
	 * @param text
	 *            is printed according to the set log level.
	 */
	public static void log(final String text) {
		if (EFTEMj_Debug.debugLevel >= DEBUG_IN_APP_LOGGING) {
			IJ.log(text);
		}
	}

	/**
	 * @param imp
	 *            is shown if the log level is high enough.s
	 */
	public static void show(final ImagePlus imp) {
		if (EFTEMj_Debug.debugLevel >= DEBUG_SHOW_IMAGES) {
			imp.show();
		}
	}
}
