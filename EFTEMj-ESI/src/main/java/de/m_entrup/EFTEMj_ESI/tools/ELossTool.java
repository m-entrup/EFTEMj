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

package de.m_entrup.EFTEMj_ESI.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;

import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.shared.ESI_ConfigurationManager;
import ij.ImagePlus;
import ij.ImageStack;

/**
 * This class contains static methods that are related to the energy loss of the
 * used images.
 */
public class ELossTool {

	private static String configKeyPrefix = "ESI." + ELossTool.class.getSimpleName() + ".";
	public static String databaseTitlePatternKey = configKeyPrefix + "titlePattern";

	/**
	 * By the use of regular expressions the energy loss is extracted from the
	 * label of a slice.
	 *
	 * @param imp
	 *            The {@link ImagePlus} contains the images.
	 * @param index
	 *            The index of the image where you want to extract the energy
	 *            loss. <code>index</code> starts at 0.
	 * @return The energy loss in eV that has been found. If the label does not
	 *         contain an readable energy loss 0 is returned.
	 */
	public static float eLossFromSliceLabel(final ImagePlus imp, final int index) {
		final ImageStack stack = imp.getStack();
		String label;
		if (index == 0 & stack.getSize() == 1) {
			label = imp.getShortTitle();
		} else {
			label = stack.getShortSliceLabel(index + 1);
		}
		return findELoss(label);
	}

	/**
	 * By the use of regular expressions the energy loss is extracted from the
	 * label of a slice.
	 *
	 * @param imageStack
	 *            The {@link ImageStack} that contains the image at
	 *            <code>(index+1)</code>.
	 * @param index
	 *            The index of the image where you want to extract the energy
	 *            loss. <code>index</code> starts at 0.
	 * @return The energy loss in eV that has been found. If the label does not
	 *         contain an readable energy loss 0 is returned.
	 */
	public static float eLossFromSliceLabel(final ImageStack imageStack, final int index) {
		final String label = imageStack.getShortSliceLabel(index + 1);
		return findELoss(label);
	}

	/**
	 * By the use of regular expressions the energy loss is extracted from the
	 * title of an image.
	 *
	 * @param imp
	 *            The {@link ImagePlus} contains the images.
	 * @param index
	 *            The index of the image where you want to extract the energy
	 *            loss. <code>index</code> starts at 0.
	 * @return The energy loss in eV that has been found. If the label does not
	 *         contain an readable energy loss 0 is returned.
	 */
	public static float eLossFromTitle(final ImagePlus imp, final int index) {
		final String title = imp.getTitle();
		return findELoss(title, index);
	}

	/**
	 * Tries to find the eLoss at the given String.
	 *
	 * @param label
	 *            A String that may contain an eLoss.
	 * @return The eLoss fount at the String, 0 if no eLoss was found.
	 */
	private static float findELoss(final String label) {
		final Matcher matcher1 = Pattern.compile(PluginConstants.PATTERN_ELOSS_LONG).matcher(label);
		if (matcher1.find()) {
			String eLossStr = label.substring(matcher1.start() + 1, matcher1.end() - 3);
			eLossStr = eLossStr.replace(",", ".");
			return stringToFloat(eLossStr);
		}
		final Matcher matcher2 = Pattern.compile(PluginConstants.PATTERN_ELOSS_SHORT).matcher(label);
		if (matcher2.find()) {
			String eLossStr = label.substring(matcher2.start(), matcher2.end() - 2);
			eLossStr = eLossStr.replace(",", ".");
			return stringToFloat(eLossStr);
		}
		return 0;
	}

	/**
	 * Tries to find a pattern that describes the energy losses of a spectrum
	 * imaging stack. The default pattern is defined in EFTEMj-ESI.properties.
	 * The pattern must contain at least 2 groups: start value and increment.
	 * The default pattern contains 3 groups: start, stop and increment.
	 *
	 * @param title
	 *            A String that may contain an eLoss.
	 * @param index
	 *            The index of the image where you want to extract the energy
	 *            loss. <code>index</code> starts at 0.
	 * @return The eLoss fount at the String, 0 if no eLoss was found.
	 */
	private static float findELoss(final String title, final int index) {
		try {
			final CompositeConfiguration config = ESI_ConfigurationManager.getConfiguration();
			final String pattern = config.getString(databaseTitlePatternKey);
			final Matcher matcher = Pattern.compile(pattern).matcher(title);
			if (matcher.find()) {
				if (matcher.groupCount() == 2) {
					final float start = stringToFloat(matcher.group(1));
					final float inc = stringToFloat(matcher.group(2));
					return start - index * inc;
				} else if (matcher.groupCount() == 3) {
					final float start = stringToFloat(matcher.group(1));
					final float stop = stringToFloat(matcher.group(2));
					final float inc = stringToFloat(matcher.group(3));
					if (start > stop) {
						return start - index * inc;
					}
					return start + index * inc;
				} else {
					return 0;
				}
			}
		} catch (final ConfigurationException e) {
			return 0;
		}
		return 0;
	}

	/**
	 * This method is used to convert the energy loss string to a float value.
	 * If there is a ',' it is replaced by a '.'.
	 *
	 * @param eLossStr
	 *            A {@link String} that contains only the energy loss.
	 * @return A float value, 0 if converting fails.
	 */
	private static float stringToFloat(final String eLossStr) {
		eLossStr.replace(',', '.');
		try {
			return new Float(eLossStr);
		} catch (final Exception e) {
			return 0;
		}
	}

}
