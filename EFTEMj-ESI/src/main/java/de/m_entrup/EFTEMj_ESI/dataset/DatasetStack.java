
package de.m_entrup.EFTEMj_ESI.dataset;

import de.m_entrup.EFTEMj_ESI.tools.ELossTool;
import ij.ImagePlus;
import ij.ImageStack;

/**
 * This is the main class to handle {@link ImageStack}s at the plugin. An
 * instance of this class is created if the {@link ImagePlus} at
 * {@link DatasetAPI} is a stack.
 */
public class DatasetStack {

	/**
	 * The energy loss of each image of the {@link ImageStack} .
	 */
	protected float[] eLossArray;
	/**
	 * The {@link ImageStack} of the selected {@link ImagePlus}. This
	 * {@link ImageStack} is used at all calculation.
	 */
	protected ImageStack imageStack;

	/**
	 * Creates a {@link DatasetStack} object that is used in calculations.
	 *
	 * @param imageStack The {@link ImageStack} you want to use at calculations.
	 */
	public DatasetStack(final ImageStack imageStack) {
		super();
		this.imageStack = imageStack;
		eLossArray = new float[imageStack.getSize()];
		for (int i = 0; i < imageStack.getSize(); i++) {
			eLossArray[i] = ELossTool.eLossFromTitle(imageStack, i);
		}
	}
}
