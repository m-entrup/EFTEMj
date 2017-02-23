/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2015, Michael Entrup b. Epping
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.m_entrup.EFTEMj_ESI.plugin;

import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * {@link PluginConstants} contains only constants (static final fields) that
 * can be used to configure the plugin.
 */
public class PluginConstants {

	/**
	 * The default shift that is used at the drift correction.
	 */
	public static final int DRIFT__DEFAULT_DELTA = 20;
	/**
	 * The default value for the exit condition of the power law fit.
	 */
	public static final float EPSILON = 1e-6f;
	/**
	 * An error that is logged if a can't be calculated. There is a logarithm of
	 * a negative number.
	 */
	public static final short ERROR__A_NOT_POSSIBLE_TO_CALCULATE = 230;
	/**
	 * An error that is logged if the iteration does not converge.
	 */
	public static final short ERROR__CONVERGENCE = 220;
	/**
	 * An error that is logged if an interim result is NaN.
	 */
	public static final short ERROR__NAN = 210;
	/**
	 * No error has occurred.
	 */
	public static final short ERROR__NON = 0;
	/**
	 * If the signal at the current pixel of any image is less than zero, no
	 * background fit is performed.
	 */
	public static final short ERROR__SIGNAL_LESS_THAN_ZERO = 200;
	/**
	 * The gap between {@link BorderLayout} components.
	 */
	public static final int LAYOUT__BORDER_LAYOUT_GAP = 5;
	/**
	 * Border thickness of the ContentPane.
	 */
	public static final int LAYOUT__BORDER_THICKNESS = 5;
	/**
	 * The gap between {@link GridLayout} components.
	 */
	public static final int LAYOUT__GAP = 5;
	/**
	 * The system specific line separator.
	 */
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	/**
	 * This pattern is used to extract the energy loss from the image title.
	 * Long version with squared brackets.
	 */
	public static final String PATTERN_ELOSS_LONG = "\\[\\d*[.]?[,]?\\d+eV\\]";
	/**
	 * This pattern is used to extract the energy loss from the image title.
	 * Short version.
	 */
	public static final String PATTERN_ELOSS_SHORT = "\\d*[.]?[,]?\\d+eV";
	/**
	 * This value is used to indicate that there was an error at the MLE
	 * calculation. It is the smallest nonezero value multiplied with (-1).
	 */
	public static final float VALUE_CALCULATION_FAILED = Float.NaN;
}
