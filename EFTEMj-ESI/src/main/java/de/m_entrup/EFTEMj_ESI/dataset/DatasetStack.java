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
	 * @param imp
	 *            The {@link ImageStack} you want to use at calculations.
	 */
	public DatasetStack(final ImagePlus imp) {
		super();
		imageStack = imp.getImageStack();
		eLossArray = new float[imageStack.getSize()];
		try {
			/**
			 * First we try to find the energy loss as part of the slice label.
			 */
			for (int i = 0; i < imageStack.getSize(); i++) {
				eLossArray[i] = ELossTool.eLossFromSliceLabel(imp, i);
			}
		} catch (final NullPointerException e) {
			/**
			 * When no energy loss was found at the slice label, this part is
			 * used to calculate the energy loss from two values at the stack
			 * title: starting energy and energy increment.
			 */
			for (int i = 0; i < imageStack.getSize(); i++) {
				eLossArray[i] = ELossTool.eLossFromTitle(imp, i);
				final String label = imageStack.getSliceLabel(i + 1);
				if (label == null) {
					imageStack.setSliceLabel("[" + eLossArray[i] + "eV]", i + 1);
				} else {
					imageStack.setSliceLabel(label + "_[" + eLossArray[i] + "eV]", i + 1);
				}
			}
			imp.updateAndRepaintWindow();
		}
	}
}
