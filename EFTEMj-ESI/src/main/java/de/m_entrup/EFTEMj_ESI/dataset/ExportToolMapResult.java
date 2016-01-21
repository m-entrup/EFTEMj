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

import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.tools.ImagePlusTool;

/**
 * All methods of this class are used to save a TIF image.
 */
public class ExportToolMapResult {

	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * An instance of {@link MapResultToImagePlus} to create composite images of
	 * the current results of the calculation.
	 */
	private final MapResultToImagePlus impCreator;

	public ExportToolMapResult() {
		super();
		impCreator = new MapResultToImagePlus();
	}

	public void exportA() {
		if (datasetAPI.getAMap() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getA());
		}
	}

	public void exportBG() {
		if (datasetAPI.getBackgroundImages() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getBG());
		}
	}

	public void exportChi2() {
		if (datasetAPI.getChi2() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getChi2());
		}
	}

	public void exportCoeffOfDet() {
		if (datasetAPI.getCoeffOFDet() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getCoeffOfDet());
		}
	}

	public void exportErrorMap() {
		if (datasetAPI.getErrorMap() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getErrorMap());
		}
	}

	public void exportMap() {
		if (datasetAPI.getMap() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getMap());
		}
	}

	public void exportR() {
		if (datasetAPI.getRMap() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getR());
		}
	}

	public void exportRelBG() {
		if (datasetAPI.getRelBackgroundImages() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getRelBG());
		}
	}

	public void exportSigma2() {
		if (datasetAPI.getSigma2() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getSigma2());
		}
	}

	public void exportSNR(final float dqe) {
		if (datasetAPI.getSNR() != null) {
			ImagePlusTool.saveImagePlus(impCreator.getSNR(dqe));
		}
	}

}
