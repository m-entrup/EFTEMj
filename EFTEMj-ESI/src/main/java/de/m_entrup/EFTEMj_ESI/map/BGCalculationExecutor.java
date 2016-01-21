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

package de.m_entrup.EFTEMj_ESI.map;

import javax.swing.SwingWorker;

import de.m_entrup.EFTEMj_ESI.dataset.EFTEMImage;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import ij.IJ;

/**
 * After {@link PowerLawFitCalculation} has finished the
 * {@link BGCalculationExecutor} can be started.<br>
 * The aim of the {@link BGCalculationExecutor} is to split the calculation of
 * the background into subtasks. Each task processes 1 image.
 */
public class BGCalculationExecutor extends SwingWorker<Void, Void> {

	/**
	 * For each input image, a background image is calculated.
	 */
	private final EFTEMImage[] array_EFTEMImages;

	/**
	 * The constructor transmits the number of subtasks to the
	 * {@link ThreadInterface}.
	 *
	 * @throws Exception
	 */
	public BGCalculationExecutor() throws Exception {
		super();
		array_EFTEMImages = PluginAPI.getInstance().getDatasetAPI()
			.getEFTEMImageArray();
		ThreadInterface.getInstance().configureThreadChecker(
			array_EFTEMImages.length);
	}

	@Override
	protected Void doInBackground() throws Exception {
		IJ.showStatus("Starting background calculation...");
		for (int i = 0; i < array_EFTEMImages.length; i++) {
			final BGCalculation bgCalc = new BGCalculation(array_EFTEMImages[i], i);
			bgCalc.start();
		}
		return null;
	}

}
