
package de.m_entrup.EFTEMj_ESI.map;

import javax.swing.SwingWorker;

import de.m_entrup.EFTEMj_ESI.dataset.EFTEMImage;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import ij.IJ;

/**
 * After {@link BGCalculation} has finished the {@link MapCalculationExecutor}
 * can be started.<br>
 * The aim of the {@link MapCalculationExecutor} is to split the calculation of
 * the elemental-map into subtasks. Each task processes 1 image.
 */
public class MapCalculationExecutor extends SwingWorker<Void, Void> {

	/**
	 * For each input image, with an index larger than edgeIndex, a elemental-map
	 * image is calculated.
	 */
	private final EFTEMImage[] array_EFTEMImages;
	/**
	 * The index of the first post-edge image.
	 */
	private final int edgeIndex;

	/**
	 * The constructor transmits the number of subtasks to the
	 * {@link ThreadInterface}.
	 *
	 * @throws Exception
	 */
	public MapCalculationExecutor() throws Exception {
		super();
		array_EFTEMImages = PluginAPI.getInstance().getDatasetAPI()
			.getEFTEMImageArray();
		edgeIndex = PluginAPI.getInstance().getDatasetAPI().getEdgeIndex();
		ThreadInterface.getInstance().configureThreadChecker(
			array_EFTEMImages.length - edgeIndex);
	}

	@Override
	protected Void doInBackground() throws Exception {
		IJ.showStatus("Starting elemental map calculation...");
		for (int i = edgeIndex; i < array_EFTEMImages.length; i++) {
			final MapCalculation mapCalc = new MapCalculation(array_EFTEMImages[i],
				i);
			mapCalc.start();
		}
		return null;
	}

}
