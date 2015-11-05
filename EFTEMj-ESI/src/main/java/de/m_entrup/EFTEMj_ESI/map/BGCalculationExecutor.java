
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
