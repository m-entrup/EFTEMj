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
import ij.ImagePlus;

/**
 * All methods of this class are used to display a {@link ImagePlus} object.
 */
public class DisplayToolMapResult {

	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * An instance of {@link MapResultToImagePlus} to create composite images of
	 * the current results of the calculation.
	 */
	private final MapResultToImagePlus impCreater;

	public DisplayToolMapResult() {
		super();
		impCreater = new MapResultToImagePlus();
	}

	public void showA() {
		if (datasetAPI.getAMap() != null) {
			final ImagePlus imp = impCreater.getA();
			imp.show();
		}
	}

	public void showBG() {
		if (datasetAPI.getBackgroundImages() != null) {
			final ImagePlus imp = impCreater.getBG();
			imp.show();
		}
	}

	public void showChi2() {
		if (datasetAPI.getChi2() != null) {
			final ImagePlus imp = impCreater.getChi2();
			imp.show();
		}
	}

	public void showCoeffOfDet() {
		if (datasetAPI.getCoeffOFDet() != null) {
			final ImagePlus imp = impCreater.getCoeffOfDet();
			imp.show();
		}
	}

	public void showErrorMap() {
		if (datasetAPI.getErrorMap() != null) {
			final ImagePlus imp = impCreater.getErrorMap();
			imp.show();
		}
	}

	public void showMap() {
		if (datasetAPI.getMap() != null) {
			final ImagePlus imp = impCreater.getMap();
			imp.show();
		}
	}

	public void showR() {
		if (datasetAPI.getRMap() != null) {
			final ImagePlus imp = impCreater.getR();
			imp.show();
		}
	}

	public void showRelBG() {
		if (datasetAPI.getRelBackgroundImages() != null) {
			final ImagePlus imp = impCreater.getRelBG();
			imp.show();
		}
	}

	public void showSigma2() {
		if (datasetAPI.getSigma2() != null) {
			final ImagePlus imp = impCreater.getSigma2();
			imp.show();
		}
	}

	public void showSNR(final float dqe) {
		if (datasetAPI.getSNR() != null) {
			final ImagePlus imp = impCreater.getSNR(dqe);
			imp.show();
		}
	}

}
