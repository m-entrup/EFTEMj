
package de.m_entrup.EFTEMj_ESI.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import ij.ImagePlus;
import ij.ImageStack;

/**
 * This class contains static methods that are related to the energy loss of the
 * used images.
 */
public class ELossTool {

	/**
	 * By the use of regular expressions the energy loss is extracted from the
	 * title of an image.
	 *
	 * @param imp The {@link ImagePlus} contains the images.
	 * @param index The index of the image where you want to extract the energy
	 *          loss. <code>index</code> starts at 0.
	 * @return The energy loss in eV that has been found. If the label does not
	 *         contain an readable energy loss 0 is returned.
	 */
	public static float eLossFromTitle(final ImagePlus imp, final int index) {
		final ImageStack stack = imp.getStack();
		String label;
		if (index == 0 & stack.getSize() == 1) {
			label = imp.getShortTitle();
		}
		else {
			label = stack.getShortSliceLabel(index + 1);
		}
		return findELoss(label);
	}

	/**
	 * By the use of regular expressions the energy loss is extracted from the
	 * title of an image.
	 *
	 * @param imageStack The {@link ImageStack} that contains the image at
	 *          <code>(index+1)</code>.
	 * @param index The index of the image where you want to extract the energy
	 *          loss. <code>index</code> starts at 0.
	 * @return The energy loss in eV that has been found. If the label does not
	 *         contain an readable energy loss 0 is returned.
	 */
	public static float eLossFromTitle(final ImageStack imageStack,
		final int index)
	{
		final String label = imageStack.getShortSliceLabel(index + 1);
		return findELoss(label);
	}

	/**
	 * Tries to find the eLoss at the given String.
	 *
	 * @param label A String that may contain an eLoss.
	 * @return The eLoss fount at the String, 0 if no eLoss was found.
	 */
	private static float findELoss(final String label) {
		final Matcher matcher1 = Pattern.compile(PluginConstants.PATTERN_ELOSS_LONG)
			.matcher(label);
		if (matcher1.find()) {
			String eLossStr = label.substring(matcher1.start() + 1, matcher1.end() -
				3);
			eLossStr = eLossStr.replace(",", ".");
			return stringToFloat(eLossStr);
		}
		else {
			final Matcher matcher2 = Pattern.compile(
				PluginConstants.PATTERN_ELOSS_SHORT).matcher(label);
			if (matcher2.find()) {
				String eLossStr = label.substring(matcher2.start(), matcher2.end() - 2);
				eLossStr = eLossStr.replace(",", ".");
				return stringToFloat(eLossStr);
			}
			else {
				return 0;
			}
		}
	}

	/**
	 * This method is used to convert the energy loss string to a float value. If
	 * there is a ',' it is replaced by a '.'.
	 *
	 * @param eLossStr A {@link String} that contains only the energy loss.
	 * @return A float value, 0 if converting fails.
	 */
	private static float stringToFloat(final String eLossStr) {
		eLossStr.replace(',', '.');
		try {
			return new Float(eLossStr);
		}
		catch (final Exception e) {
			return 0;
		}
	}

}
