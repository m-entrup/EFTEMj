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

import de.m_entrup.EFTEMj_ESI.map.PowerLawFitCalculation;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;

/**
 * The {@link DatasetMapInput} class collects all data that is used to start the
 * elemental-map calculation.
 */
public class DatasetMapInput {

	/**
	 * The images that are used for the elemental-map calculation. The array has
	 * to be sorted by energy loss.
	 */
	protected EFTEMImage[] array_InputImages;
	/**
	 * The energy loss that separates pre-edge and post-edge images. An image with
	 * the same energy loss value as edgeELoss will be handled as a post-edge
	 * image.
	 */
	protected float edgeELoss;
	/**
	 * The index of the first post-edge image.
	 */
	protected int edgeIndex;

	/**
	 * The constructor creates an object of {@link DatasetMapInput} that is
	 * necessary to start the MLE calculation.
	 *
	 * @param inputImages A sorted array of {@link EFTEMImage}s.
	 * @param edgeELoss The energy loss where the elemental signal starts.
	 * @param epsilon The exit condition for the MLE calculation.
	 * @throws Exception
	 */
	public DatasetMapInput(final EFTEMImage[] inputImages, final float edgeELoss,
		final float epsilon) throws Exception
	{
		this.array_InputImages = inputImages;
		this.edgeELoss = edgeELoss;
		PowerLawFitCalculation.setEpsilon(epsilon);
		int index = 0;
		while (index < inputImages.length) {
			if (inputImages[index].eLoss >= edgeELoss) {
				break;
			}
			index++;
		}
		edgeIndex = index;
		if (edgeIndex < 2) {
			throw new Exception(PluginMessages.getString("Error.PreEdgeImages"));
		}
		if (inputImages.length - edgeIndex == 0) {
			throw new Exception(PluginMessages.getString("Error.PostEdgeImages"));
		}
	}
}
