
package de.m_entrup.EFTEMj_ESI.map;

import javax.swing.SwingWorker;

import de.m_entrup.EFTEMj_ESI.dataset.EFTEMImage;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import ij.IJ;

/**
 * After {@link MapCalculation} has finished the {@link SNRCalculationExecutor}
 * can be started.<br />
 * The aim of the {@link SNRCalculation} is to split the calculation of the
 * Signal-to-Noise-Ratio (SNR) into subtasks. Each task processes 1 image row.
 */
public class SNRCalculationExecutor extends SwingWorker<Void, Void> {

	/**
	 * For each input image, with an index larger than edgeIndex, a SNR image is
	 * calculated.
	 */
	private final EFTEMImage[] array_EFTEMImages;
	/**
	 * The index of the first post-edge image.
	 */
	private final int edgeIndex;
	/**
	 * The height of the processed images. This corresponds to the number of
	 * subtasks the calculation of one image is split up.
	 */
	private final int height;

	/**
	 * The constructor transmits the number of subtasks to the
	 * {@link ThreadInterface}.
	 *
	 * @throws Exception
	 */
	public SNRCalculationExecutor() throws Exception {
		super();
		height = PluginAPI.getInstance().getDatasetAPI().getHeight();
		array_EFTEMImages = PluginAPI.getInstance().getDatasetAPI()
			.getEFTEMImageArray();
		edgeIndex = PluginAPI.getInstance().getDatasetAPI().getEdgeIndex();
		ThreadInterface.getInstance().configureThreadChecker(
			(array_EFTEMImages.length - edgeIndex) * height);
	}

	@Override
	protected Void doInBackground() throws Exception {
		IJ.showStatus("Starting signal to noise ratio calculation...");
		for (int i = edgeIndex; i < array_EFTEMImages.length; i++) {
			for (int y = 0; y < height; y++) {
				final SNRCalculation snr = new SNRCalculation(y, i);
				snr.start();
			}
		}
		return null;
	}

}
