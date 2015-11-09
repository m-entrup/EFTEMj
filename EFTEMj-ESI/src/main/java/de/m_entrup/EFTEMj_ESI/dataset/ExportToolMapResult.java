
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
