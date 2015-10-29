
package de.m_entrup.EFTEMj_ESI.plugin;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import de.m_entrup.EFTEMj_ESI.gui.MainMenu;

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
	 * An error that is logged if a can't be calculated. There is a logarithm of a
	 * negative number.
	 */
	public static final short ERROR__A_NOT_POSSIBLE_TO_CALCULATE = 230;
	/**
	 * An error that is logged if the iteration does not converge.
	 */
	public static final short ERROR__CONVERGENCE = 240;
	/**
	 * An error that is logged if an interim result is NaN.
	 */
	public static final short ERROR__NAN = 250;
	/**
	 * No error has occurred.
	 */
	public static final short ERROR__NON = 0;
	/**
	 * An error that is logged if the calculated r is smaller than the given
	 * limit.
	 */
	public static final short ERROR__R_LESS_THAN_LIMIT = 220;
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
	public static final String LINE_SEPARATOR = System.getProperty(
		"line.separator");
	/**
	 * This pattern is used to extract the energy loss from the image title. Long
	 * version with squared brackets.
	 */
	public static final String PATTERN_ELOSS_LONG = "\\[\\d*[.]?[,]?\\d+eV\\]";
	/**
	 * This pattern is used to extract the energy loss from the image title. Short
	 * version.
	 */
	public static final String PATTERN_ELOSS_SHORT = "\\d*[.]?[,]?\\d+eV";
	/**
	 * The default value of the bottom limit of r.
	 */
	public static final float R_LIMIT = 2;
	/**
	 * This value is used to indicate that there was an error at the MLE
	 * calculation. It is the smallest nonezero value multiplied with (-1).
	 */
	public static final float VALUE_CALCULATION_FAILED = -Float.MIN_VALUE;
	/**
	 * This is the current version of the plugin. It is displayed at the title of
	 * the {@link MainMenu}.
	 */
	public static final double VERSION = 0.7;
}
